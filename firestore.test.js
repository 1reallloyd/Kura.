const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write profiles, contacts, or transactions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("contacts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("transactions").get());

  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Alice",
      currency: "GH₵",
      countryCode: "233",
      onboarded: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated user can create and partially update own profile, and extra/spoofed fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      name: "Kwame Ansah",
      biz: "Kwame Provisions",
      phone: "0244123456",
      loc: "Madina market",
      currency: "GH₵",
      countryCode: "233",
      onboarded: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      biz: "Kwame Enterprise",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt modification
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Cross-user isolation blocks Bob from reading or writing Alice's data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("contacts").doc("c_1").set({
      userId: ALICE_UID,
      name: "Ama Serwaa",
      phone: "0244123456",
      loc: "Accra",
      type: "customer",
      deleted: false,
      createdAtMillis: Date.now() - 5000,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("contacts").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("contacts").doc("c_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("contacts").doc("c_1").update({
      name: "Hijacked",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Ledger contact lifecycle: create, edit, soft-delete, and reject @DocumentId 'id' field", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const contactsCol = aliceDb.collection("users").doc(ALICE_UID).collection("contacts");

  // Valid create (no 'id' property)
  await assertSucceeds(
    contactsCol.doc("contact_1").set({
      userId: ALICE_UID,
      name: "Kojo Mensah",
      phone: "0200111222",
      loc: "",
      type: "customer",
      deleted: false,
      createdAtMillis: Date.now() - 5000,
      createdAt: created,
      updatedAt: created,
    })
  );

  // List query succeeds on empty/populated subcollection
  await assertSucceeds(contactsCol.get());

  // Soft delete succeeds
  await assertSucceeds(
    contactsCol.doc("contact_1").update({
      deleted: true,
      deletedAtMillis: Date.now() - 1000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject creating doc with 'id' key inside payload
  await assertFails(
    contactsCol.doc("contact_2").set({
      id: "contact_2",
      userId: ALICE_UID,
      name: "Bad Contact",
      phone: "",
      loc: "",
      type: "customer",
      deleted: false,
      createdAtMillis: Date.now() - 5000,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Reject invalid contact type
  await assertFails(
    contactsCol.doc("contact_3").set({
      userId: ALICE_UID,
      name: "Bad Type",
      phone: "",
      loc: "",
      type: "vip",
      deleted: false,
      createdAtMillis: Date.now() - 5000,
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("Ledger transaction lifecycle: create, void, correction entry, and immutability of amountCents", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);
  const txCol = aliceDb.collection("users").doc(ALICE_UID).collection("transactions");

  // Valid create
  await assertSucceeds(
    txCol.doc("tx_1").set({
      userId: ALICE_UID,
      contactId: "contact_1",
      amountCents: 42000,
      note: "Rice and oil",
      tsMillis: Date.now() - 5000,
      voided: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Tampering with amountCents on update must fail
  await assertFails(
    txCol.doc("tx_1").update({
      amountCents: 100,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Voiding transaction succeeds
  await assertSucceeds(
    txCol.doc("tx_1").update({
      voided: true,
      voidedAtMillis: Date.now() - 1000,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Creating correction transaction with fixOfTxId succeeds
  await assertSucceeds(
    txCol.doc("tx_2").set({
      userId: ALICE_UID,
      contactId: "contact_1",
      amountCents: 45000,
      note: "Rice and oil (corrected)",
      tsMillis: Date.now() - 5000,
      voided: false,
      fixOfTxId: "tx_1",
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Zero amount must fail
  await assertFails(
    txCol.doc("tx_zero").set({
      userId: ALICE_UID,
      contactId: "contact_1",
      amountCents: 0,
      note: "Invalid zero",
      tsMillis: Date.now() - 1000,
      voided: false,
      createdAt: created,
      updatedAt: created,
    })
  );
});
