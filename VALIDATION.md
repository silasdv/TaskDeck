# Validation — 0.3.0-prototype

29 October 2026. Local debug build; not published. User testing of 0.2 identified touch/focus, setup-flow and overly broad protection problems. The changed 0.3 build has not been executed on physical RP6 hardware here.

- assembleDebug: passed.
- testDebugUnitTest: **29 tests passed**, 0 failures, 0 errors, 0 skipped.
- lintDebug: 0 errors, 12 warnings, mainly English UI strings and existing dependency/backup guidance.
- Signature: apksigner verification passed; same local debug signing key as 0.1/0.2. Certificate SHA-256: `07ac7a0eb96089084280494e4a768d86cf0994f78f4c0df8a2e67877c01a06c1`. This is not a production release key.
- No INTERNET permission; POST_NOTIFICATIONS and Shizuku API permission only.

Checks cover explicit D-pad selection/opening without native ListView focus, immediate X removal and neighbor selection, left-stick selection/stopping, Select/long-press protection and unlocking, Y exclusions, multi-key recording/saving, chord event balance/device separation and backend identity/policy restrictions. Added coverage verifies real down/up touch activation on the first tap, switching back to explicit controller focus, reserved D-pad/Confirm during recording and Confirm saving, Show/Hide notification state, sort-popup D-pad/Confirm, all three sorting orders with unknown RAM last, mandatory filtering and locked-last grouping, first selection on a new opening, Close others preserving the entry app regardless of selected card, empty bulk exit to HOME and the stay-open preference. At simulated 800×360dp landscape, more than two ordinary cards are visible and card contents fit.

UI tests use fake task IPC. They do not verify Android's actual task service or PSS values. Per-app PSS, Wave discovery, rear-button reporting, running-task order/return detection, closing ordinary system-app tasks and notification delivery after exit require hardware acceptance. Native Quickstep/swipe-up replacement is not implemented.

Hidden API calls remain isolated to the Shizuku shell/root UserService, with a documented narrow lint exception. The old Quick Settings Intent overload is restricted to pre-34 devices, also documented. Protection is rechecked in the backend; user locks are passed before list reads/removals. Missing HOME/input-method discovery fails closed.

No task screenshots or private text are collected. PSS samples and task metadata are transient; only explicit configuration, including chosen protected package names and shortcut key/scan identifiers, is stored.
