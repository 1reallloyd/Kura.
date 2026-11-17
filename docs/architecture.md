# Kura Technical Architecture

Kura follows an **MVVM** architecture with reactive Kotlin Flows, a single-source-of-truth **Room SQLite** database for ledger records, and **Jetpack DataStore Preferences** for profile metadata, onboarding flags, and passcode security.

---

## 1. High-Level Layers

```text
MainActivity (Compose Root + Navigation State Machine)
  ├── SplashScreen
  ├── ShopOwnerWelcomeScreen
  ├── OnboardingScreen
  └── KuraLedgerSession
        ├── ProfileSetupScreen / ContactsImportScreen / PinScreen
        ├── MainLedgerScreen (Customers, Summary, Settings + Modal Sheets)
        └── ContactDetailScreen
              │
              ▼
        KuraViewModel (StateFlow<UiState<KuraLedgerData>>)
              │
      ┌───────┴────────────────┐
      ▼                        ▼
KuraRepository         PinPreferencesStore
      │                (DataStore: profile, onboarding,
      ▼                 welcomeStory, salted SHA-256 passcode)
KuraDatabase (Room)
  └── KuraDao (contacts & transactions)
```

---

## 2. Navigation & App Lifecycle Flow

`MainActivity` manages screen routing via reactive DataStore and ViewModel states without requiring an external navigation dependency:

1. **Splash**: `showSplash` displays `SplashScreen` for 1.7s (or until tapped).
2. **Welcome Story**: If `welcomeStorySeenFlow` is `false`, shows `ShopOwnerWelcomeScreen` (with `R.drawable.img_shop_owner_welcome`).
3. **Onboarding Pager**: If `onboardingSeenFlow` is `false`, shows the 3-page `OnboardingScreen`.
4. **Setup Steps (3-Step Flow)**:
   - If `profile == null || !profile.onboarded` -> `ProfileSetupScreen` (`Step 1 of 3`).
   - Transitions to `OnboardingSetupStep.CONTACTS` -> `ContactsImportScreen` (`Step 2 of 3`).
   - Transitions to `OnboardingSetupStep.PIN` -> `PinScreen(PinMode.SET)` (`Step 3 of 3`).
5. **Passcode Gate**:
   - If `pinLockState.hasPin && !pinUnlocked` -> `PinScreen(PinMode.LOCK)`.
   - A `LifecycleEventObserver` records `ON_STOP` timestamps and clears `pinUnlocked` (and active modal sheets) on `ON_START` if backgrounded for more than **30,000 ms**.
6. **Main Workspace**:
   - Switches between `MainLedgerScreen` (with responsive `NavigationBar` < 720dp / `NavigationRail` >= 720dp) and `ContactDetailScreen`, plus modal bottom sheets (`ActiveSheet`).

---

## 3. Data Storage & Schema

### Room Database (`kura_ledger.db`, version 1)
Defined in `app/src/main/java/com/example/data/local/KuraDatabase.kt`:

1. **`contacts` table (`ContactEntity`)**:
   - `id: String` (Primary Key, 16-char UUID prefix)
   - `name: String` (max 60 chars)
   - `phone: String` (max 30 chars)
   - `loc: String` (max 80 chars)
   - `type: String` (`"customer"` or `"supplier"`)
   - `deleted: Boolean` (soft delete flag)
   - `deletedAtMillis: Long?`
   - `createdAtMillis: Long`

2. **`transactions` table (`TransactionEntity`, indexed on `contactId`)**:
   - `id: String` (Primary Key)
   - `contactId: String`
   - `amountCents: Long` (integer minor units to avoid floating-point rounding drift)
   - `note: String` (max 120 chars)
   - `tsMillis: Long`
   - `voided: Boolean`
   - `voidedAtMillis: Long?`
   - `fixOfTxId: String?` (links replacement entry to the voided original)

### Balance Sign Convention
All amounts are stored in integer cents/pesewas (`amountCents`):
- **Positive (`balanceCents > 0`)**: Contact owes you.
  - Customer *Gave credit* (`+`) increases balance; *Got payment* (`-`) decreases balance.
  - Supplier *I paid* (`+`) reduces debt or creates credit; *Bought on credit* (`-`) increases what you owe.
- **Negative (`balanceCents < 0`)**: You owe the contact.
- **Zero (`balanceCents == 0`)**: Settled.
- Voided transactions (`voided == true`) are excluded from balances and totals while remaining visible with strikethrough in contact history.

### Transactional Safety (`KuraDao`)
- `correctTransaction`: Wrapped in `@Transaction` to atomically void the original entry and insert the replacement entry linked via `fixOfTxId`.
- `replaceAll` / `eraseAll`: Wrapped in `@Transaction` so sample data loading, backup restoration, and full erasure never leave partial state.

---

## 4. Passcode Security (`PinPreferencesStore`)

Stored in `kura_local_prefs` via Jetpack DataStore Preferences:
- Passcodes are never stored in plaintext.
- Setting a passcode generates a random 16-byte `SecureRandom` salt and stores `SHA-256(salt + passcode)`.
- `verifyPin(candidate)` enforces brute-force protection:
  - `< 5` consecutive failures -> returns `PinVerifyResult.WrongPin(triesLeft)`.
  - `>= 5` consecutive failures -> applies exponential lockout `30s * min(2^(tries - 5), 20)` and returns `PinVerifyResult.Locked(waitSeconds)`.
  - Successful entry resets `tries` and `lockUntilMs` to `0`.

---

## 5. Phonebook Integration (`PhonebookReader`)

- **Batch Multi-Select**: Queries `ContactsContract.CommonDataKinds.Phone.CONTENT_URI` on `Dispatchers.IO`, deduplicating entries by `(name, last 9 digits)` (or `contactId|name|number` for short numbers).
- **Single-Contact System Picker**: Resolves `ActivityResultContracts.PickContact` URIs through `ContactsContract.Contacts` and queries matching phone numbers without requiring `READ_CONTACTS`.
- **Import Deduplication**: `KuraRepository.importContacts` compares normalized last-9-digit phone suffixes against all existing contacts (active and deleted) and reports `ImportResult(added, duplicates, skippedNoName)`.

---

## 6. Backup JSON v2 Format

Exported and restored by `KuraRepository.exportBackupJson` and `restoreFromBackupJson`:

```json
{
  "v": 2,
  "user": "Kwame Ansah",
  "biz": "Kwame's Provisions",
  "phone": "0241234567",
  "loc": "Madina market",
  "cur": "GH₵",
  "cc": "233",
  "list": [
    {
      "id": "...",
      "name": "Ama Serwaa",
      "phone": "0244123456",
      "loc": "",
      "type": "customer",
      "created": 1731800000000,
      "tx": [
        {
          "id": "...",
          "amt": 42000,
          "note": "Rice and oil",
          "ts": 1731800000000
        }
      ]
    }
  ],
  "trash": []
}
```

- `v` must equal `2` and `list` must be present for `restoreFromBackupJson` to accept the payload.
- Optional transaction keys `"void"` (voided timestamp) and `"fix"` (`fixOfTxId`) preserve audit history across backups.
