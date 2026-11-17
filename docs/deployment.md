# Deployment & Build Guide

This document covers building, signing, permissions, and distributing the **Kura** Android app.

---

## 1. App Identity & SDK Targets

Configured in `app/build.gradle.kts` and `app/src/main/res/values/strings.xml`:

| Setting | Value |
| :--- | :--- |
| **App Name** | `Kura` |
| **Application ID** | `com.aistudio.kuraledger.qxmtkp` |
| **Package Namespace** | `com.example` |
| **minSdk** | `24` (Android 7.0) |
| **targetSdk / compileSdk** | `36` |
| **Version** | `1.0` (`versionCode = 1`) |

---

## 2. Building the App

### In AI Studio
- **Preview**: Run in the AI Studio Streaming Android Emulator directly in your browser.
- **APK / AAB Export**: Open the AI Studio **Settings** menu and choose **Generate APK** (or **AAB**) to download an installable package for an Android phone.
- **Project Export**: Use **Export as ZIP** or **Push to GitHub** to inspect or release from Android Studio.

### Local Gradle CLI
Run standard Gradle commands from the workspace root (requires `ANDROID_SDK_ROOT` configured):

```bash
# Build debug APK (app/build/outputs/apk/debug/app-debug.apk)
gradle :app:assembleDebug

# Run JVM & Robolectric unit tests
gradle :app:testDebugUnitTest

# Build release APK or Android App Bundle (requires release keystore env vars)
gradle :app:assembleRelease
gradle :app:bundleRelease
```

For release signing via environment variables (`app/build.gradle.kts`):
- `KEYSTORE_PATH` (defaults to `${rootDir}/my-upload-key.jks`)
- `STORE_PASSWORD`
- `KEY_PASSWORD`
- Key alias: `upload`

---

## 3. Android Manifest & Permissions

Configured in `app/src/main/AndroidManifest.xml`:

### Declared Permissions
- `android.permission.READ_CONTACTS` — Requested at runtime only when the user taps **Choose from my phonebook** to import multiple contacts. If denied, the app falls back gracefully to Android's zero-permission `ActivityResultContracts.PickContact` picker or links to App Settings.
- `android.permission.INTERNET` & `android.permission.ACCESS_NETWORK_STATE` — Standard install-time permissions for opening external WhatsApp/HTTPS reminder links (`https://wa.me/...`).

### Package Visibility (`<queries>`)
- Packages: `com.whatsapp`, `com.whatsapp.w4b` (WhatsApp and WhatsApp Business)
- Intents: `ACTION_VIEW` (`https` scheme) and `ACTION_DIAL` (click-to-call customer/supplier numbers)

### Launcher Icon & Assets
- Adaptive icon configured via `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`, backed by `drawable/ic_launcher_background.xml` (Saffron gradient) and `drawable/ic_launcher_foreground.xml` (Kura open ledger book vector), plus rasterized `mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}` PNGs.
- Custom font bundled locally at `app/src/main/res/font/manrope.ttf`.
- Shop/chop owner welcome illustration bundled at `app/src/main/res/drawable/img_shop_owner_welcome.jpg`.

---

## 4. Google Play Policy Checklist

- **Least-Privilege Permissions**: No broad storage permissions (`READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`) are declared or requested.
- **Runtime Consent**: `READ_CONTACTS` uses `ActivityResultContracts.RequestPermission()` with a clear explanation and a non-blocking single-contact fallback if declined.
- **Offline Data Disclosure**: Ledger data is stored locally on the device (`kura_ledger.db` and `kura_local_prefs`); phonebook data is only read upon explicit user action to populate contacts inside Kura.
- **No Dynamic Code Loading**: All code and assets are statically compiled into the APK/AAB.
