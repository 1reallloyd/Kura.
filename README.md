# Kura. — Simple Credit Ledger for Shop & Chop Owners

**Kura** is an offline-first Android credit ledger app built with Kotlin and Jetpack Compose for shopkeepers, chop bar owners, provision stores, and market traders to track **who owes them** and **who they owe**.

All ledger records live locally on the device in a Room SQLite database, protected by an optional 4-digit passcode lock, with direct Android phonebook import and one-tap WhatsApp payment reminders.

---

## Documentation Index

| Document | Audience | Contents |
| :--- | :--- | :--- |
| [User Guide](docs/user-guide.md) | Shop & chop owners, testers | Onboarding flow, passcode lock, phonebook import, credit/payment entries, WhatsApp reminders, backup & restore |
| [Architecture](docs/architecture.md) | Android developers | Screen flow, MVVM state management, Room SQLite schema, DataStore passcode security, backup JSON v2 spec |
| [Deployment & Permissions](docs/deployment.md) | Release managers | Build configuration, APK/AAB export, Android permissions, Play Store compliance |

---

## Key Features

- **Illustrated Shop-Owner Welcome & Onboarding**: Immediately after the animated splash screen, Kura introduces the app with a shop/chop-owner welcome screen followed by a 3-slide feature walkthrough and guided 3-step setup.
- **4-Digit Passcode Lock**: Salted SHA-256 passcode hashing stored in Jetpack DataStore Preferences with exponential lockout after 5 failed attempts and automatic re-lock after 30 seconds in the background.
- **Direct Phonebook Contact Import**: Uses `READ_CONTACTS` to open the phonebook directly—supporting multi-select import (`ContactsContract.CommonDataKinds.Phone`) or single-contact picking (`ActivityResultContracts.PickContact`) with automatic duplicate phone detection.
- **Customer & Supplier Credit Tracking**:
  - **Customers**: Log *Gave credit* (`+`) and *Got payment* (`-`).
  - **Suppliers**: Log *Bought on credit* (`-`) and *I paid* (`+`).
  - **Correction & Void Audit Trail**: Void mistaken entries or replace them atomically (`fixOfTxId`) without losing history.
- **Live Balances & Overdue Alerts**: Real-time net balance hero card, receivables vs. payables ratio bar, 7-day overdue customer alerts, and a 7-day Summary tab with top debtors.
- **One-Tap WhatsApp Reminders**: Pre-fills a polite reminder message with the customer's balance and business name, opening WhatsApp or copying the text to clipboard.
- **JSON Backup & Restore + Trash Recovery**: Soft-delete contacts into *Recently deleted* for safe restoration, or export/restore full ledger backups as portable v2 JSON text.

---

## Tech Stack

- **Language & UI**: Kotlin, Jetpack Compose, Material Design 3, adaptive layout (`NavigationBar` on phones, `NavigationRail` on >= 720dp screens), Manrope typography.
- **Local Persistence**: Room SQLite (`kura_ledger.db`) + Jetpack DataStore Preferences (`kura_local_prefs`).
- **Concurrency & State**: Kotlin Coroutines, `StateFlow`, `collectAsStateWithLifecycle`.
- **Android Integration**: `ContactsContract`, `ActivityResultContracts.RequestPermission`, `ActivityResultContracts.PickContact`, WhatsApp/Browser & Dialer `Intent` queries.

---

## Project Layout

```text
.
├── README.md                  # Project overview (excluded from APK)
├── docs/                      # App documentation (excluded from APK)
│   ├── user-guide.md
│   ├── architecture.md
│   └── deployment.md
├── app/
│   ├── build.gradle.kts       # App dependencies, SDK & signing config
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/      # Models, Room DB, PhonebookReader, DataStore, Repository
│       │   │   └── ui/        # Theme, Components, Screens, ViewModel
│       │   └── res/           # Custom adaptive icon, Manrope font, illustrations, strings
│       └── test/              # Robolectric & JVM unit tests
├── build.gradle.kts
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

> **Note**: Placing `README.md` and `docs/` at the repository root keeps all documentation completely separate from `app/src/main/`, so it adds **0 bytes** to compiled `.apk` / `.aab` builds and never affects runtime behavior.
