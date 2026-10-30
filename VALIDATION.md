# Validation — 1.1.1

30 October 2026. versionName 1.1.1, versionCode 111; UserService protocol remains 8. Same release key as 1.0/1.1.

- testDebugUnitTest: **108 passed**, zero failures/errors/skips.
- lintRelease: **0 errors, 36 warnings** (English-only strings, physical corner alignment/programmatic views and existing private-API/dependency/accessibility/backup/drawing guidance).
- assembleRelease and bundleRelease: passed.

Added regression coverage checks large alphabetical app rows, search filtering, L1/R1 pages/return, touch-search to controller-results transition, asynchronous loading preserving the search view/query/focus, Mapping Right to Labels and Left back, centered/max-size magnified icons inside their bubbles, fully visible focused end items across overflowing corner lists, separate Current text (including a readable badge/name layout in dense Fit) and matching full-height/padded toolbar surfaces. Previous regressions remain covered. An existing minimum-font test now locates the title inside its badge row, preserving the original size assertion.

All checks use Robolectric/fake IPC. Physical RP6 touch drag, firmware keys/origin, real Shizuku operations, system font/insets and Android notifications remain device acceptance cases. No absolute compatibility or performance claim is made.

---

# Historical validation — 1.1

29 October 2026. Release versionName 1.1, versionCode 110; UserService implementation/protocol remains 8. Same dedicated release key as 1.0, supporting normal upgrades.

- testDebugUnitTest: **101 passed**, zero failures/errors/skips.
- lintRelease: **0 errors, 36 warnings**. Warnings include English-only strings, physical corner alignment, programmatic view constructors and existing private-API/dependency/backup/accessibility/drawing guidance.
- assembleRelease and bundleRelease: passed.
- Release signing retains certificate SHA-256 `7c4fee9313f84dc00aa55ddd58bbc3277901a3e3dcabf97e3506b3ebeabbd386` (RSA 3072). Private signing material remains excluded.

New simulated checks cover favorite persistence/order/corrupt preferences, bounds and theme resolution; Current identity across selection/reopening; D-pad/stick RAM access; live permission indicators; completion delay/Confirm; pinning without protection/task synthesis; launch of closed favorites; controller reorder/removal; Thumb corners geometry, size and focus visibility; first-touch activation after controller; circular Settings, compact hints, Dock pages and installed-app picker return paths. Existing task, controller, IPC, protection, RAM-scale and feedback regressions remain covered.

These are Robolectric/fake-backend checks, not execution on physical RP6 hardware or pixel-level visual review. Native touch drag/drop/cancellation and overflow, firmware origin/keys, real Shizuku APIs, Android notifications, font scaling/insets and all dock styles require DEVICE-TEST.md acceptance. No universal compatibility, reclaimed-memory or performance claim is made.

---

# Historical validation — 1.0

29 October 2026. Release versionName 1.0, versionCode 100. Non-debuggable APK and App Bundle signed with the dedicated TaskDeck release key; Shizuku UserService implementation remains 8. Earlier 0.x builds use the debug key and require uninstalling before this release.

- testDebugUnitTest: **86 passed**, no failures/errors/skips.
- lintRelease: **0 errors, 24 warnings**, including English strings and existing private-API/dependency/backup guidance.
- assembleRelease and bundleRelease: passed.
- APK signature verified; certificate SHA-256 `7c4fee9313f84dc00aa55ddd58bbc3277901a3e3dcabf97e3506b3ebeabbd386`, RSA 3072. Private material stays in ignored .local and is excluded from source/history/assets.
- Manifest version 1.0/code 100, debuggable absent/default false, POST_NOTIFICATIONS and Shizuku API permission only; no INTERNET permission.

New coverage verifies first-card selection even with only a protected launcher, immediate reset on reopening, four preset views without a mode switcher, icon-only Settings, the compact action row and smaller retain-current label without an extra action icon. A known 8 GiB memory fixture checks a half-used system bar at 500/1000 and a 50 MiB app at 6/1000 of total RAM, rather than 1000/1000 of the largest app. Available/free RAM labels are absent.

Controller tests cover conflict-swapping assignments, translated custom face bindings across profiles, calibration/reset, PlayStation vector label entries, actual custom-confirm navigation and app opening, and reserved D-pad during capture. Settings tests cover merged Appearance behavior, connection checkmark, correct feedback icon, About version/SilasDV/Ko-fi intent, and all categories/subpages fitting 640×360dp and 800×360dp landscape. Existing task/protection, windows, PID verification, deadlines, late results, chords and feedback-lifecycle tests remain.

These are simulated UI/fake-IPC checks, not a pixel-perfect review or execution of the final release on a physical RP6. Real Shizuku task APIs, launcher behavior, firmware key reporting, font scaling/insets and visible Android notification banners remain hardware acceptance cases in DEVICE-TEST.md. No universal RAM reclamation or game-performance claim is made.

---

# Historical validation — 0.8.0-prototype

29 October 2026. Local debug-signed test build. Application versionCode 9; UserService implementation/protocol stays 8. No publication and no physical RP6 execution of this version.

- assembleDebug: passed.
- testDebugUnitTest: **78 passed**, 0 failures, 0 errors, 0 skipped.
- lintDebug: **0 errors, 24 warnings**, including English UI strings and existing private-API/dependency/backup guidance.
- APK signature: verified with the same debug certificate as previous builds: `07ac7a0eb96089084280494e4a768d86cf0994f78f4c0df8a2e67877c01a06c1`.
- Permissions remain POST_NOTIFICATIONS and Shizuku API only; no INTERNET permission.

Added coverage checks decimal MB conversion from bytes and Android PSS KiB; strict non-destructive Switch apps versus Manage tasks; fresh Fit default and exactly four view presets; category Settings, stable selected On/outlined Off state and controller navigation across panes/theme rows; first-run eight-step completion and animated-check view followed by a direct subsequent launch; connection gating before optional controller steps; and gamepad D-pad/center navigation during calibration.

All eight wizard screens and Connect, recorder, calibration, shortcut settings, category Settings, RAM and Results panes fit simulated 640×360dp and 800×360dp landscape. Tests check RAM charts show more than three entries on a details page and navigate further pages, toolbar/footer geometry and rounded-control input state. The first RAM layout exposed a chart overflow; bounded font-aware chart rows and pagination corrected it. Existing task/protection, grouped windows, timeouts, late replies, direct-switch chords, RAM publication and backend identity regressions remain covered.

The automated UI tests use fake IPC and simulated geometry. They are not a pixel-level visual review or proof of actual Shizuku operations, firmware controller reporting, real PSS, Android notification banner delivery, system font scaling or RP6 insets. Verify those on hardware using DEVICE-TEST.md. App PSS does not decompose total used RAM, task removal does not guarantee process termination, and no absolute performance/reclaimed-memory claims are made.

USER-FLOW.md records the interaction concept. Settings replaces the repeat wizard; there is no Diagnostics screen, permanent Connect toolbar button or custom layout editor. Installed-but-unopened launchers still never create cards. Reports/samples stay in memory and source archives exclude signing keys/caches.

---

# Historical validation — 0.7.0-prototype

29 October 2026. Local debug build, not published. VersionCode and UserService implementation 8; AIDL adds memory sampling, direct switching and restart verification at IDs 6–8. Shizuku remains required.

- assembleDebug: passed.
- testDebugUnitTest: **68 passed**, 0 failures, 0 errors, 0 skipped.
- lintDebug: **0 errors, 21 warnings**, including English strings, private API access and existing dependency/backup guidance.
- APK: verified with the same local debug certificate documented below; POST_NOTIFICATIONS and Shizuku API permission only, no INTERNET permission.

New tests cover grouped windows without duplicated RAM, grouped removal, window picking, separate-window mode, named partial/failed/unverified results, blocked RAM while opening still works, RAM timeout without global recovery, persistent density/minimum font controls and controller focus across rebuilds. Fit all respects minimum label size even with 200 fake tasks. New Layout, Shortcuts, Diagnostics and Results pages join the 640×360dp and 800×360dp landscape fit checks.

Deadline tests check exactly-once completion, ignored late replies, worker replacement and no destructive replay. A blocked binding hook checks that setup remains responsive and recovery is bounded. Added endpoints reject ordinary app identity. Shortcut tests cover direct switching without opening the deck and equal/subset conflicts. PID-set tests check old-process disappearance, new nonempty PIDs and unresolved original launcher tasks.

RAM feedback tests validate publication before exit, Application-owned delivery after Activity destruction, permission-denied toast fallback and diagnostic state. They do not demonstrate physical banner visibility. The UI tests use fake IPC, not the real Retroid task service.

**Hardware still required:** real Shizuku task removal, launcher stop/task cleanup/new PID detection, HOME auto-reopening, previous-app ordering, rear-button routing, actual PSS, Shizuku restart recovery, firmware insets and RAM banner after menu exit. No physical RP6 run of 0.7 occurred here. A timeout cannot undo work Android already accepted; uncertain outcomes are reported and destructive actions never auto-repeat. Ordinary task removal does not prove process termination. Feedback survives Activity exit, not an OS kill/force-stop of the whole TaskDeck process. See DEVICE-TEST.md.

No screenshots, typed contents or usage history are collected. Samples, operation reports and diagnostics stay in memory; only explicit configuration is saved. Source archives exclude signing keys and caches.

## Historical validation: 0.6.1 and earlier

29 October 2026. Local debug build; not published. User testing of 0.5 requested denser app views, reordered actions, right-aligned shoulder-based setup navigation and another RAM-feedback correction. The changed 0.6 build has not been executed on physical RP6 hardware here. Shizuku remains required; no direct-root or embedded-ADB mode was added.

- assembleDebug: passed.
- testDebugUnitTest: **48 tests passed**, 0 failures, 0 errors, 0 skipped.
- lintDebug: 0 errors, 17 warnings, mainly English UI strings, hidden task-service access warnings and existing dependency/backup guidance.
- Signature: apksigner verification passed; same local debug signing key as 0.1/0.2. Certificate SHA-256: `07ac7a0eb96089084280494e4a768d86cf0994f78f4c0df8a2e67877c01a06c1`. This is not a production release key.
- No INTERNET permission; POST_NOTIFICATIONS and Shizuku API permission only.

Checks cover explicit D-pad selection/opening without native ListView focus, immediate X removal and neighbor selection, left-stick selection/stopping, Select/long-press protection and unlocking, Y exclusions, multi-key recording/saving, chord event balance/device separation and backend identity/policy restrictions. Added coverage verifies real down/up touch activation on the first tap, switching back to explicit controller focus, reserved D-pad/Confirm during recording and Confirm saving, Show/Hide notification state, sort-popup D-pad/Confirm, all three sorting orders with unknown RAM last, mandatory filtering and locked-last grouping, first selection on a new opening, Close others preserving the entry app regardless of selected card, empty bulk exit to HOME and the stay-open preference. At simulated 800×360dp landscape, more than two ordinary cards are visible and card contents fit.

UI tests use fake task IPC. They do not verify Android's actual task service or PSS values. Per-app PSS, Wave discovery, rear-button reporting, running-task order/return detection, closing ordinary system-app tasks, actual launcher force-stop/restart and visible toast delivery after exit require hardware acceptance. Native Quickstep/swipe-up replacement is not implemented.

0.4 regression coverage adds shortcut entry after touch into an all-protected deck with Refresh already selected, immediate Confirm refresh, horizontal toolbar navigation without Up, a truly empty deck refusing to enter nonexistent cards, a protected launcher still accessible with Down, launcher opening via HOME rather than the recents API, visible launcher/hidden-service sorting, and background-memory explanation with a nonzero sample. All four setup steps plus Connect, Chord and Confirm screens fit their content panes at both simulated 800×360dp and 640×360dp landscape with no ScrollView. The existing landscape test still verifies more than two cards and no clipped card content. Larger fonts, firmware insets and physical stick behavior remain hardware acceptance cases.

Hidden API calls remain isolated to the Shizuku shell/root UserService, with a documented narrow lint exception. The old Quick Settings Intent overload is restricted to pre-34 devices, also documented. Protection is rechecked in the backend; user locks are passed before list reads/removals. Missing HOME/input-method discovery fails closed.

0.5 adds five regression cases: launcher default lock plus unlock/relock/close, explicit restart retaining its saved lock, RAM toast after Activity finish/destroy with POST_NOTIFICATIONS denied, feedback disable/coalescing behavior, and all five footer actions fitting a single visible row at 640×360dp with no legend below. Normal app identity is also rejected by the new launcher-stop endpoint. These tests validate lifecycle/control wiring, not root/shell execution on Retroid. Launcher stop requests are confined to discovered launcher packages and the current Android user; required keyboard/core protection is rechecked. Synthetic idle launchers are excluded from bulk close eligibility. The Application-owned handler survives Activity destruction, not an OS process kill or force-stop of TaskDeck.

No task screenshots or private text are collected. PSS samples and task metadata are transient; only explicit configuration, including chosen protected package names and shortcut key/scan identifiers, is stored.

0.6 regression coverage adds: A–B–X–Y–R1 footer order; right-edge Next and L1/R1 setup navigation including finish without accidentally closing apps; denser Grid visibility and row movement/opening; twenty tiles fully within Fit all's viewport; List selection and view-menu persistence; immediate foreground feedback before bulk exit without a delayed duplicate; and high-importance test notification, last-result display and no clipped setup content at 640×360dp. The existing R1 test now proves feedback is published before openTask switches to the previous app. The initial layout failures (fixed-height grid rows and five stacked feedback controls) were corrected with viewport-sized rows and a compact Permissions/Test row. Settings remain non-scrolling in landscape at both tested sizes.

The RAM change fixes publication order and adds observability; it does not establish physical banner/toast delivery on RP6. Android notification permission, channel settings, DND/game-mode behavior and system process lifetime remain device checks. The result channel is high importance and quiet, and an explicitly disabled legacy RAM channel is still respected. Last-result text is held only in memory. Backend protocol/UserService stays version 5; application versionCode is 6.

0.6.1 removes the backend loop that generated launcher rows from installed package metadata. Every emitted task card now originates in the real Android recent/running task list; background-only packages remain background information. The frontend rejects synthetic rows even if they carry a nonzero memory sample, covered by a new regression test. The launcher unlock/close UI test also verifies the card disappears after removal. Implementation/UserService version 7 reloads the changed backend and application versionCode is 7; AIDL protocol is unchanged. Physical launcher task removal and Android HOME auto-restart still need RP6 verification.
