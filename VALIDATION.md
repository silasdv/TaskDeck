# Validation — 0.2.0-prototype

29 October 2026. Local debug build; not published. User testing of 0.1 identified controller-selection and UX failures. The redesigned 0.2 build has not been executed on physical RP6 hardware here.

- assembleDebug: passed.
- testDebugUnitTest: **19 tests passed**, 0 failures.
- lintDebug: 0 errors, 12 warnings, mainly English UI strings and existing dependency/backup guidance.
- Signature: same local debug signing key as 0.1; verify with apksigner. This is not a production release key.
- No INTERNET permission; POST_NOTIFICATIONS and Shizuku API permission only.

Checks cover explicit D-pad selection/opening without native ListView focus, immediate X removal and selection of a neighbor, left-stick selection/stopping, Select/long-press protection and unlocking, Y exclusions, multi-key recording/saving, chord event balance/device separation and baseline backend identity/policy restrictions. At simulated 800×360dp landscape, more than two cards are visible and card contents fit.

UI tests use fake task IPC. They do not verify Android's actual task service or PSS values. Per-app PSS, Wave's discovery on Retroid firmware and rear-button reporting require hardware acceptance.

Hidden API calls remain isolated to the Shizuku shell/root UserService, with a documented narrow lint exception. The old Quick Settings Intent overload is restricted to pre-34 devices, also documented. Protection is rechecked in the backend; user locks are passed before list reads/removals. Missing HOME/input-method discovery fails closed.

No task screenshots or private text are collected. PSS samples and task metadata are transient; only explicit configuration, including chosen protected package names and shortcut key/scan identifiers, is stored.
