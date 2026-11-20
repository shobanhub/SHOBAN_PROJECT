# SHOBAN PROJECT — Android starter and immersive previews

Kotlin/XML Android app for Secure Streamer, package `com.secureqrstreamer.app`, project ID `ASTRA-7K-DHARMA`.

## Implemented in this commit

- **3D Virtual Gallery demo:** native OpenGL ES 2 scene with a procedural gallery room, four media-frame placeholders, orbit-by-drag, and a simple animated low-poly avatar. Tour controls move the avatar between the demo frames.
- **AR Photo Portal preview:** ARCore runtime/install and camera-permission handling, ARCore camera background, tracking status, plane-finding configuration, secure window, and a QR/3D fallback message.
- The AR screen is a **camera/tracking preview only**: it does not yet recognize registered photos, place a persistent world-anchored portal, or open recovered private media. The frames in the 3D gallery are generated placeholders, not user/cloud media.
- Existing PIN and cryptographic starter features remain separate. Google Drive, QR camera scanning, media upload/recovery, and registered-photo recognition are not implemented.

## Build and run

Requirements: Android Studio, JDK 17, Android SDK 37, a compatible Android device/emulator, and network access to resolve Gradle dependencies. AR requires an ARCore-supported device and Google Play Services for AR; the 3D gallery requires GLES 2.0.

The repository currently has no Gradle wrapper scripts/JAR. With a local compatible Gradle installation:

```sh
gradle :app:testDebugUnitTest :app:assembleDebug
```

Or open the repository in Android Studio and sync/build the `app` module. Expected APK: `app/build/outputs/apk/debug/app-debug.apk`. **No build or device test was run as part of this source change.**

## UX and security notes

- Both immersive screens apply Android `FLAG_SECURE` as an ordinary screen-capture deterrent, not a guarantee against privileged capture, compromised/rooted devices, or external cameras.
- AR camera frames are displayed in memory and are not written to storage by this preview. Camera permission can be denied; the regular QR and 3D entry points remain available.
- Background/session lifecycle and AR support depend on device and Google Play Services for AR. Use the displayed fallback if AR is unavailable.
- No keystore, cloud credential, private media, or signing secret is included.

## Follow-up before production

1. Add ARCore plane hit testing and a world-anchored portal renderer; test tracking loss/recovery on supported physical devices.
2. Replace procedural gallery frames with authorized, bounded-memory media loading and connect tour ordering to a real gallery model.
3. Implement secure media/Drive flow and registered-photo matching before granting access to a gallery.
4. Test supported/unsupported AR devices, camera denial, rotations, background/foreground, performance, memory, heat, and capture behavior.
