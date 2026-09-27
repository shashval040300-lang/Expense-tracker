# Pocket Ledger

Pocket Ledger is a simple, fully local Android expense tracker. It stores transactions on the device with Room (SQLite) and does not require an account, backend, or internet connection to use.

## Features

- Dashboard with balance, income, expenses, and this month's spending
- Add income and expense transactions with category, payment method, and note
- Transaction history with a delete option and confirmation
- Spending breakdown by category
- Dark interface and custom app icon

## Download and install

Open the repository's **Releases** page and download `app-debug.apk` from the latest release. On your Android phone, open the downloaded APK and allow installation from that source if Android asks. The app requires Android 8.0 (API 26) or later.

Each published version is built from this repository by GitHub Actions. To publish a new downloadable APK, push a version tag such as `v1.0.1`:

```bash
git tag v1.0.1
git push origin v1.0.1
```

## Build in Android Studio

1. Open this repository's folder in Android Studio.
2. Allow Gradle to sync and install any requested Android SDK components.
3. Select an emulator or connected Android device and press **Run**.

To build an APK locally, run `gradlew.bat assembleDebug` on Windows or `./gradlew assembleDebug` on macOS/Linux. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Stack

- Kotlin and Jetpack Compose
- Room database (SQLite)
- MVVM-style ViewModel and repository
- Minimum Android version: Android 8.0 (API 26)

All transaction data stays in the app's private storage on the device. Uninstalling the app or clearing its storage removes that data. Backup and restore are not implemented yet.
