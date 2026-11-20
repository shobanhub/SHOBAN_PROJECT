# SHOBAN PROJECT — Complete Android APK Development Prompt

**Project reference:** SHOBAN PROJECT  
**Core application:** Secure Streamer  
**Project ID:** ASTRA-7K-DHARMA  
**Package currently identified:** `com.secureqrstreamer.app`  
**Primary platform:** Native Android application  
**Current implementation base:** Kotlin + Android XML (verify against the latest uploaded project before editing)

---

## MASTER INSTRUCTION

Act as a senior Android architect, Kotlin developer, application-security engineer, and QA engineer. Continue development of the existing **SHOBAN PROJECT** Android application from its supplied Android Studio source ZIP.

The goal is to evolve the existing **Secure Streamer** into a secure QR-based media-sharing and memory-experience platform with:

1. Secure encrypted photo/video hosting and recovery.
2. QR-based gallery identification and PIN-gated access.
3. Secure media viewing with temporary-file cleanup and screen-capture protections.
4. Gallery and media management.
5. Physical-photo registration and registered-photo recognition.
6. An AR Photo Portal.
7. An immersive 3D Virtual Gallery with an animated avatar and guided tour.
8. A separate 3D Animation Gallery.

**This is an incremental development project, not a greenfield rewrite.** Preserve existing working code and user data. Do not replace the app architecture, encryption, authentication, cloud storage, UI framework, or navigation without first inspecting the actual source and explaining why a change is necessary.

## 1. REQUIRED WORKING METHOD

Before changing any code:

1. Open and inspect the entire latest Android Studio project ZIP.
2. Inventory the source tree, package names, Gradle wrapper and plugin versions, dependencies, build variants, manifest, permissions, activities, layouts, resources, assets, and existing tests.
3. Trace the actual Host, Upload, QR, PIN, Recovery, Decryption, and Gallery Viewer code paths.
4. Read the existing encryption, key-wrapping, manifest, and cache-cleanup implementations.
5. Identify the exact source version being modified. Do not mix files from older project ZIPs with newer patches.
6. Produce a short inspection report and file-by-file implementation plan.
7. Make one small, reversible stage of changes at a time.
8. Build after each meaningful stage. Run unit/instrumentation tests where available.
9. Provide the complete updated Android Studio project source and the generated APK only after the build succeeds.
10. Report actual test results. Never say a build, test, or feature passed unless it was genuinely run or the user supplied the result.

If a Gradle build cannot run because dependencies or the Gradle distribution cannot be downloaded, state that clearly. Do not report a successful build based only on static inspection.

## 2. CURRENT PROJECT FOUNDATION — PRESERVE AND VERIFY

The previously inspected project used:

- Kotlin source.
- Android XML layouts.
- `MainActivity`.
- `GalleryActivity` (older/offline visual prototype).
- `GalleryViewerActivity` (actual recovered-media viewer introduced during Stage 1).
- Google Identity authorization.
- Google Drive API integration.
- Google ML Kit barcode/QR scanning.
- Android document/media picker.
- Background executor for Drive and media work.
- `GalleryKeyCrypto`.
- `RecoveryKeyProvisioning`.
- `GalleryManifestStore`.
- `MediaEncryptor` references in recovered project history.
- `portal_gallery.html` prototype.

Previously identified package: `com.secureqrstreamer.app`.

A prior inspection reported minimum SDK 26 and compile/target SDK 37. **Treat all versions and dependencies as provisional until confirmed in the latest supplied project.** Do not upgrade Gradle, Android Gradle Plugin, Kotlin, or SDK levels as an incidental cleanup.

### Existing Secure Streamer functions

Preserve and verify the current implementation of:

- Host sign-in and Google Drive authorization.
- Drive folder creation and saved folder identifier.
- Photo/video selection.
- Gallery ID generation.
- Gallery encryption key generation.
- Media encryption.
- Encrypted media upload.
- Gallery manifest save/update.
- QR payload generation and scanning.
- Gallery PIN setup and verification.
- Recovery-secret generation and display.
- Manifest lookup and retrieval.
- Gallery-key unwrap/recovery.
- Encrypted media download and decryption.
- Actual image display and video playback.

Do not remove the older prototype files until all references are understood and the replacement flow is verified.

## 3. CURRENT USER-TESTED CHECKPOINT

The following results were reported by the user during physical Android phone testing. Treat them as **user-reported device test results**, not as a substitute for automated tests or a full security audit.

| Functionality | Current observed result |
|---|---|
| App launches on Android phone | User tested |
| Local recovery crypto self-test | Passed |
| Gallery PIN can be saved | Passed |
| Valid QR → PIN → recovery → actual gallery route | Passed after Stage 1.2 integration |
| Correct PIN | Accepted |
| Incorrect PIN | Rejected with “Incorrect PIN” |
| Invalid QR | Rejected with expected invalid-gallery message |
| Incorrect Recovery Secret | Rejected |
| Actual image recovery/decryption/display | Passed |
| Actual video recovery/decryption/playback | Passed |
| Close Gallery cleanup | Passed on phone |
| Background cleanup | Passed on phone |
| Screenshot privacy protection | Passed on phone |
| Screen recording did not show original gallery content | Passed on the tested phone after the screen-capture patch |

### Still requiring validation

- Force-stop and process-death cleanup.
- Restart/relaunch cleanup of stale decrypted files.
- Screen-capture behavior on additional Android versions and devices.
- Recovery failures, no-network cases, missing manifests, missing media, and partial-download cases.
- Larger galleries, mixed media, long videos, and memory pressure.
- Build reproducibility from a clean checkout.
- A complete security and privacy review.

Do not claim that flash-storage blocks are securely erased. App-level deletion does not guarantee physical secure erasure. Do not claim protection against rooted/compromised devices, privileged capture, external cameras, or every possible recording tool.

## 4. EXISTING DATA AND SECURITY MODEL

### 4.1 QR payload

The current prototype QR format is:

`SQR1:<gallery-id>`

The QR must contain only a public gallery identifier. Never put a PIN, recovery secret, encryption key, access token, or private credential in a QR code.

Validate the format and identifier before doing any Drive or gallery work. Reject malformed, unknown, or unsupported QR payloads safely.

### 4.2 PIN

The previously supplied `MainActivity.kt` implementation stored a salted PIN verifier in device-local `SharedPreferences` using PBKDF2-HMAC-SHA256 (120,000 iterations in the inspected source).

Important behavior:

- The current PIN is device-local.
- A PIN set on an emulator does not automatically exist on a physical phone.
- A PIN set on one phone does not automatically verify on another phone.
- PIN verification is a local access gate; it is not the Gallery encryption key and is not a substitute for recovery authorization.
- Do not silently change this behavior or describe the current PIN as a cross-device account credential.
- If cross-device/owner-managed PINs are later required, design a separate secure shared authorization mechanism and threat model before implementation.

### 4.3 Recovery Secret and encryption

The recovery secret is separate from the PIN. It is used with the existing recovery/key-wrapping implementation to recover the gallery encryption key.

The source previously showed AES-GCM media decryption and a wrapped gallery key stored in the Drive manifest. The local self-test validated wrap/unwrap symmetry only; it did not validate a real Drive gallery.

Rules:

- Inspect `GalleryKeyCrypto` and `RecoveryKeyProvisioning` before modifying them.
- Preserve existing compatible gallery data.
- Never log, display in logs, or transmit the recovery secret to analytics.
- Never store secrets in QR payloads or plain manifest fields.
- Clear sensitive byte arrays where practical.
- Do not invent a new key format or rotate keys without a migration plan.
- Wrong secrets must fail safely without corrupting the gallery or deleting cloud originals.
- Distinguish key-recovery errors from media-download and media-decryption errors.

### 4.4 Google Drive

Google Drive is the existing cloud-storage foundation. Inspect the existing OAuth scopes and Drive API usage before changing them.

The inspected recovery flow looked up a manifest with a name similar to:

`SQR_manifest_<gallery-id>.json`

It validated the manifest Gallery ID, unwrapped the gallery key, downloaded encrypted media, decrypted files, and opened the recovered-media viewer.

Do not introduce another backend or cloud provider unless there is a documented technical requirement and an approved migration/design plan.

## 5. STAGE 1 — SECURE STREAMER FOUNDATION

The goal is a reliable, user-understandable end-to-end flow:

### Host flow

1. Open app.
2. Authenticate with Google.
3. Connect to the correct Drive account.
4. Create or select a gallery.
5. Generate a Gallery ID and gallery key.
6. Generate a recovery secret and show it once with a clear save warning.
7. Select photos/videos.
8. Encrypt the media using the existing compatible encryption implementation.
9. Upload encrypted media to Drive.
10. Save/update the manifest.
11. Display the QR payload `SQR1:<gallery-id>`.
12. Provide a way to save/share the QR without exposing secrets.

### Viewer/recovery flow

1. Open QR scanner.
2. Scan and validate `SQR1:<gallery-id>`.
3. Ask for the locally configured PIN.
4. Reject incorrect PIN without opening any gallery.
5. On successful PIN verification, request the separate recovery secret.
6. Authenticate to the Google account that can access the gallery.
7. Find and load the correct manifest.
8. Validate the manifest Gallery ID.
9. Unwrap the gallery key with the supplied recovery secret.
10. Download encrypted media to a temporary encrypted-download location.
11. Decrypt media into protected app-private temporary storage.
12. Validate each result and display supported photos/videos in `GalleryViewerActivity`.
13. Present clear per-item errors without displaying partial/corrupt files.
14. On exit/background/session termination, clean up the recovered session according to the defined lifecycle policy.

Never route the successful QR flow back to the old `GalleryActivity` offline prototype. The tested QR flow must open the actual recovered-media viewer.

### Viewer requirements

- Image thumbnails or a clear list/grid.
- Full-size image preview.
- Video playback with controls.
- Media order preserved from the manifest.
- Loading, empty, retry, and error states.
- Graceful handling of unsupported/corrupt files.
- Accessible labels and usable screen layouts.
- No network or decryption work on the main UI thread.
- Lifecycle-safe video player release and cleanup.

## 6. STAGE 1 SECURITY HARDENING

Continue validating and hardening the Stage 1.3 work:

- Keep recovered plaintext files under app-private storage only.
- Use unique per-session/per-gallery temporary directories.
- Validate canonical paths before deleting files; never recursively delete an arbitrary path.
- Delete partial output after a failed decrypt.
- Delete encrypted download temp files in `finally` blocks.
- Clean the previous session before starting a new one.
- Clean recovered files when the user closes the gallery.
- Clean recovered files when the app backgrounds or the gallery session is terminated, according to the chosen lifecycle design.
- On app start, remove stale recovered-session folders left by force-stop, crash, or process death.
- Test cleanup after force-stop, crash/restart, rotation, and repeated recovery.
- Apply `FLAG_SECURE` to all sensitive gallery windows, including image-preview and video-player dialogs/windows.
- Test screenshot and screen-recording behavior on supported Android versions. Explain that `FLAG_SECURE` is a platform-level deterrent for ordinary capture paths, not a guarantee against all capture methods.
- Stop/release video playback before deleting the underlying file.
- Avoid keeping gallery content in notifications, recents thumbnails, logs, external cache, shared storage, or backups.
- Review backup rules and data-extraction rules so sensitive preferences/cache are not unintentionally backed up.
- Never claim secure physical erasure of flash memory.

Do not silently change the tested user experience. Explain any new re-authentication behavior caused by background cleanup.

## 7. STAGE 2 — GALLERY AND MEDIA MANAGEMENT

After Stage 1 is validated, implement owner-side gallery management without breaking existing galleries.

Required features:

- Create gallery.
- Rename/edit gallery metadata.
- Add/upload photos and videos.
- Replace media.
- Delete media.
- Reorder media.
- View gallery contents and upload status.
- Gallery QR display/export/share.
- Gallery list and gallery detail screen.
- Upload progress, retry, and partial-failure handling.
- Confirm destructive actions.
- Keep the manifest and Drive media references consistent.
- Handle orphaned files safely; do not delete cloud originals until the intended deletion is confirmed and recoverable behavior is defined.

Suggested metadata fields, subject to actual schema review:

- Gallery ID, owner identifier, title, description, creation date, privacy/access configuration.
- Media item ID, Drive file ID, encrypted filename, original filename, MIME type, IV/nonce metadata, ordering, caption, description, creation date.
- Gallery configuration and version.

Design a versioned manifest migration strategy before changing its schema.

## 8. STAGE 3 — PHYSICAL PHOTO REGISTRATION

Create an owner workflow to register a physical printed photograph and associate it with a gallery.

Registration should capture:

- Photo ID.
- Gallery ID.
- Owner ID.
- Reference photograph.
- Optional QR identifier.
- Recognition features/metadata.
- Registration date and active/inactive status.

Workflow:

1. Owner selects a gallery.
2. Owner captures or selects a reference photo.
3. App guides the owner to capture a clear, well-lit image.
4. App registers the reference and associates it with the gallery.
5. Owner can optionally generate/print a QR code as a reliable fallback.
6. Owner can review, replace, disable, or delete the registration.

Do not promise arbitrary-photo recognition. Recognition is limited to photos that have been registered and can be matched with adequate confidence.

## 9. STAGE 4 — REGISTERED PHOTO RECOGNITION

Evaluate suitable on-device recognition approaches after inspecting the actual Android stack.

Requirements:

- Match camera input against registered photos.
- Use confidence thresholds.
- Handle low light, glare/reflections, motion blur, perspective, rotation, partial obstruction, similar photos, and poor camera focus.
- Reject low-confidence matches.
- Never open a private gallery just because a weak visual match occurs.
- Offer QR fallback or user-guided re-scan.
- Keep recognition data and any reference image protected.
- Consider on-device processing first for privacy.
- Document false-positive/false-negative behavior and device requirements.

## 10. STAGE 5 — AR PHOTO PORTAL

Evaluate ARCore or another supported Android AR option only after codebase and device compatibility review.

Desired flow:

1. Recognize a registered physical photo or scan its QR.
2. Resolve the gallery identity.
3. Complete authorization.
4. Track/locate the photo/frame where supported.
5. Render a portal transition aligned with the physical photo where technically feasible.
6. Use visual effects such as glow, light, particles, ripple, depth, and portal opening.
7. Transition into the gallery experience.

Requirements:

- Camera permission handling.
- Supported/unsupported device handling.
- AR lifecycle and session management.
- Tracking-loss recovery.
- Lighting guidance.
- Performance/heat/battery controls.
- Non-AR fallback for unsupported devices.

AR must not be required for the standard QR gallery or 3D mode.

## 11. STAGE 6 — 3D VIRTUAL GALLERY

Create a phone-based immersive 3D environment; do not require a VR headset.

Potential environments:

- Gallery hall.
- Home/memory room.
- Garden.
- Owner-customized environment.

The virtual gallery should display:

- Photos.
- Videos.
- Audio.
- Captions and descriptions.
- Dates and related memories.
- Optional memory objects.

Requirements:

- Full-screen 3D mode.
- Manual navigation/exploration.
- Configurable media layout and ordering.
- Lazy loading and asset caching.
- Back/exit and recovery states.
- Accessible fallback to the standard 2D gallery.
- Graceful handling of missing or oversized assets.

Choose the rendering technology only after documenting the tradeoffs. Do not add Unity automatically. Evaluate native Android/GL-based or other appropriate renderers; if Unity is recommended, explain APK size, build pipeline, lifecycle, integration, licensing, and maintenance implications before adopting it.

## 12. STAGE 7 — AVATAR AND GUIDED MEMORY TOUR

Avatar capabilities:

- Enter the gallery.
- Walk and navigate.
- Stop and turn toward a selected photo/video.
- Idle and interaction animations.
- Follow a configured path.
- Respect configurable speed and movement style.

Automated tour:

1. Avatar enters.
2. Walks to the first item.
3. Stops and faces the item.
4. Displays its media and associated memory.
5. Proceeds to the next configured item.
6. Repeats until the tour ends.

User controls:

- Play.
- Pause.
- Resume.
- Replay.
- Skip.
- Next/Previous.
- Select an item.
- Switch to manual exploration.
- Exit.

Make the tour optional; manual gallery exploration must remain available.

## 13. STAGE 8 — SEPARATE 3D ANIMATION GALLERY

This is a distinct product module, not just an effect inside the memory gallery.

Required capabilities:

- 3D model library.
- Model preview.
- Interactive rotation.
- Zoom.
- Animation playback and pause.
- Collections/categories.
- Model details.
- QR sharing and privacy-aware access.
- In-app viewing.
- Optional browser viewing only when technically justified.

Evaluate GLB/glTF support after choosing the renderer. Validate file size, memory use, model safety, texture sizes, animation compatibility, and asset-loading behavior.

## 14. OWNER MANAGEMENT AND CONFIGURATION

Provide owner controls for:

- Gallery creation and editing.
- Media upload, replacement, removal, and ordering.
- Physical-photo registration.
- QR generation.
- Gallery environment/layout.
- Avatar selection and movement settings.
- Guided-tour sequence and timing.
- 3D animation collections.
- Access and privacy settings.
- Preview/test gallery.

Permission changes must be enforced by the actual access-control layer, not only by hiding UI buttons.

## 15. PROPOSED MODULE BOUNDARIES

Use these as conceptual boundaries, not as a mandate to reorganize every file immediately. Fit them to the existing codebase after inspection.

- `core/security`: secrets, session handling, permission checks.
- `core/crypto`: existing media encryption and key recovery.
- `cloud/drive`: Google Drive authorization, upload, download, manifest operations.
- `gallery`: gallery models, viewer, media management.
- `qr`: QR scanner, generator, payload parser, resolution.
- `photoportal`: photo registration and recognition.
- `ar`: AR session, tracking, portal effect.
- `virtualgallery`: 3D environment, avatar, navigation, tour.
- `animationgallery`: independent 3D model/animation library.
- `ui`: screens, navigation, shared UI components.

Do not move or rename source files without a clear reason and verified imports/build.

## 16. PERFORMANCE, RELIABILITY, AND UX

- Keep network, file, crypto, and model-loading work off the main thread.
- Use bounded memory and image downsampling.
- Stream large files where feasible.
- Provide progress and cancellation.
- Avoid loading all high-resolution photos or 3D assets simultaneously.
- Release video/3D/AR resources when leaving their screens.
- Handle app process death and configuration changes.
- Support low-memory and unsupported-device fallbacks.
- Provide clear, human-readable error messages.
- Avoid leaking secrets or private media in logs/crash reports.

## 17. REQUIRED TEST PLAN

### Secure Streamer and QR
- Correct QR.
- Invalid QR.
- Malformed identifier.
- Unknown gallery.
- Correct PIN.
- Incorrect PIN.
- No PIN configured.
- Correct recovery secret.
- Incorrect or empty recovery secret.
- Google account with access.
- Google account without access.
- Cancelled authorization.
- Network unavailable.

### Media and manifest
- One image.
- Multiple images.
- One video.
- Mixed images/videos.
- Large image.
- Long/large video.
- Unsupported MIME type.
- Missing Drive media.
- Missing/corrupt manifest.
- Manifest Gallery ID mismatch.
- Partial download.
- Decryption failure.
- Retry after failure.
- Upload interrupted.
- Manifest update failure.

### Temporary-media and capture security
- Close Gallery cleanup.
- Background cleanup.
- Force-stop cleanup.
- Crash/process-death cleanup.
- Restart cleanup.
- Stale session cleanup.
- Screenshot behavior.
- Screen recording behavior.
- Dialog/window capture behavior.
- Video stops before file deletion.
- No plaintext files in shared/external storage.
- Backup/log/recents review.

### AR, recognition, and 3D
Test only after those modules are implemented:
- Registered and unregistered photos.
- Similar-looking photos.
- Low light, glare, angle, blur, occlusion.
- Unsupported AR device.
- Camera permission denied.
- Tracking lost/restored.
- Portal transition and fallback.
- 3D model loading failures.
- Avatar movement and interactions.
- Tour pause/resume/replay/skip.
- Video and memory presentation.
- Performance, heat, battery, memory.

Record device model, Android version, app build/version, steps, expected result, actual result, and pass/fail for each test.

## 18. APK BUILD AND DELIVERY REQUIREMENTS

When implementation is ready:

1. Build a clean Debug APK for internal testing.
2. Run the agreed test suite on emulator and physical device.
3. Fix all build errors and critical runtime issues.
4. Build a Release APK only after validation.
5. Use a user-controlled signing keystore. Never invent, expose, or hard-code keystore passwords or private signing keys.
6. Provide reproducible build instructions and version information.
7. Provide the APK, complete Android Studio source project, and a change log.
8. State clearly whether the delivered APK is debug or release-signed.
9. Include known limitations and test evidence.
10. Do not call a prototype, source patch, or unverified ZIP a completed production APK.

## 19. DEFINITION OF DONE

The project is not complete merely because it compiles.

Secure Streamer Stage 1 is complete when the existing app can reliably create a gallery, encrypt and upload media, generate a QR, authorize and recover the correct gallery, reject invalid QR/PIN/recovery secrets, decrypt and display images, play videos, and clean up temporary plaintext according to the verified lifecycle policy.

The broader SHOBAN PROJECT is complete only when the gallery-management, photo-registration/recognition, AR portal, 3D virtual gallery, avatar/tour, and separate 3D animation gallery modules are implemented, tested, documented, and delivered with a reproducible APK build.

## 20. FIRST TASK WHEN USING THIS PROMPT

Do not immediately start adding AR or 3D features.

Start by asking for or opening the **latest complete Android Studio project ZIP**, including the most recently tested Stage 1.3/1.3.1 changes. Compare it with the previous source, identify the current exact baseline, inspect force-stop/restart cleanup, and produce a concise Stage 1 completion report and next-stage plan.

Only then begin the next implementation task.

---

**Project identity to retain:**  
**SHOBAN PROJECT** — Secure Streamer — `ASTRA-7K-DHARMA`

**Core development rule:** Preserve the working secure-media foundation, make small auditable changes, build and test each stage, and never claim an APK or security feature is complete without evidence.
