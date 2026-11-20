# SHOBAN PROJECT — Android starter

A new Kotlin + Android XML project for **Secure Streamer**, package `com.secureqrstreamer.app`, project ID `ASTRA-7K-DHARMA`.

## Current scope

This is a greenfield starter scaffold, not the previously tested Stage 1.3 application. It contains a home screen, a local device PIN verifier, QR payload validation, an AES-GCM gallery-key wrap/unwrap primitive with a local self-test, and a protected placeholder gallery screen. No Drive account integration, media upload/recovery, QR camera scanner, or production-ready gallery flow is implemented. No credentials or signing keys are included.

## Requirements

- Android Studio with JDK 17
- Android SDK platform 37 and a compatible build-tools version
- Gradle 8.13 distribution (the wrapper scripts/JAR are not included yet)

## Build

Open this repository in Android Studio and sync Gradle, or from a machine with Gradle 8.13 installed:

```sh
gradle :app:testDebugUnitTest :app:assembleDebug
```

The expected debug APK path after a successful build is `app/build/outputs/apk/debug/app-debug.apk`. This project has not been built or tested in this environment.

## Security notes

- PIN is a device-local verifier only and is not the gallery encryption key.
- `GalleryKeyCrypto` is a primitive demonstration and does not define or migrate any existing gallery format.
- The starter app does not upload data or authenticate to Google Drive.
- The placeholder gallery applies `FLAG_SECURE`; this does not guarantee protection from privileged capture, rooted devices, or external cameras.
- App-level file deletion cannot guarantee physical flash-memory erasure.

## Next implementation stages

1. Add deterministic tests for PIN derivation and crypto failure cases.
2. Define and review a versioned manifest/encryption format before storing media.
3. Integrate Google authorization/Drive only after credentials, scopes, account ownership, and recovery requirements are approved.
4. Implement media picker, streaming encryption/upload, QR generation/scanning, and lifecycle cleanup as separate testable stages.
5. Defer recognition, AR, 3D gallery, avatar, and animation-gallery work until the secure media foundation is verified.
