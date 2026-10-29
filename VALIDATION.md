# Validation — 0.1.0-prototype

29 October 2026. Local debug build; no publication and no confirmed RP6 hardware execution.

- assembleDebug: passed.
- testDebugUnitTest: 8 tests passed, 0 failures.
- lintDebug: passed, 0 errors and 12 warnings. Remaining warnings include English-only UI strings, older annotation dependency and backup/attribute guidance.
- APK signature: verified, debug certificate CN=TaskDeck Debug, O=SilasDV. SHA-256 certificate fingerprint: 07ac7a0eb96089084280494e4a768d86cf0994f78f4c0df8a2e67877c01a06c1.
- Manifest permissions checked: POST_NOTIFICATIONS and Shizuku API_V23 only; no INTERNET permission.
- MIT third-party notice bundled in APK assets.

Tests cover removal exclusions, fail-closed behavior when the backend runs as an ordinary app, shortcut opt-in and repeat/release handling, and controller cancellation of close-all without sending removal requests. The UI test uses fake IPC and is not a test of real task service permissions.

Two narrow lint exceptions are documented in source: hidden APIs only in the Shizuku shell/root UserService (where Shizuku documents unrestricted non-SDK access), and the legacy Quick Settings Intent overload only on OS versions below API 34. Neither exception proves device compatibility.

UserService avoids ContentResolver. It discovers input-method packages through PackageManager and protects them all; removal fails closed if that discovery returns no input methods.

Not tested: actual shell/root task enumeration and switching, task removal on Retroid firmware, global shortcut delivery by RP6 hardware, notification/tile launch on the device, dock/external-display operation, and behavior after reboot. Follow DEVICE-TEST.md.
