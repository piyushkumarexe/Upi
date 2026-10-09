# Pace Pay

A lightweight Android UPI hand-off app built with Kotlin and Jetpack Compose. The focus is a calm, quick-to-use interface: QR scan, UPI-ID payment, a clear review step, local activity history, a personal payment QR, and light/dark themes.

> **Important:** Pace Pay is a working client-side prototype, not a bank, wallet, PSP or UPI issuer. It does not create or hold a balance, resolve phone numbers, or process money itself. A payment is handed to an installed UPI app using the standard `upi://pay` intent. The user must review and authorize it in that app. Payment status is shown as complete only when a provider returns a success response; otherwise, verify in the provider/bank app before retrying. Production direct UPI processing requires an approved PSP/bank integration, backend controls, security review, and applicable NPCI/regulatory approvals.

## What works

- Scan a UPI payment QR using Google Play services Code Scanner and prefill the recipient, amount and note.
- Enter a UPI ID and amount, validate the details, review them, and open the Android UPI app chooser.
- Handle a returned UPI status when a provider supplies one; keep unconfirmed outcomes clearly marked.
- Keep up to 100 payment-request records locally on the device.
- Create a shareable UPI QR for your own UPI ID.
- Switch between warm light and deep dark themes. The preference and optional saved UPI ID stay on-device.

Pace Pay never asks for or stores a UPI PIN, bank password, or card details. The QR scanner is provided by Google Play services and may not be available on devices without it.

## Build locally

Requirements: JDK 17 and Android SDK Platform 35 with Android Build Tools 35.0.0. The checked-in Gradle wrapper downloads Gradle 8.9 on first use.

```bash
./gradlew --no-daemon :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions APK build

`.github/workflows/android.yml` builds the debug APK on every push, pull request, or manual run. Open the **Build Android APK** workflow run and download the `pace-pay-debug-apk` artifact. This is a debug-signed APK for testing/sideloading, not a Play Store release. A production release should use a protected signing key supplied through GitHub Actions secrets and a separately configured release build.
