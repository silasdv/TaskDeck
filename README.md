# TaskDeck

An experimental Android app switcher and task manager by **SilasDV**, version **0.8.0-prototype**. Built with substantial AI assistance through Codex (vibecoded). This is a local debug-signed device-test build; the new flow still requires physical Retroid Pocket 6 acceptance.

## Everyday use

**Switch apps** is for choosing and opening apps. It uses the available width for apps and has no destructive X/Y/R1 actions. **Manage tasks** adds closing, protection and a RAM panel. Change modes using the toolbar or **L1**; the destination shows its button badge. The last mode is remembered. New installations start in Switch apps.

- D-pad/left stick selects. **A** opens; **B** exits.
- In Manage tasks: **X** closes the selected app's reported windows; **Y** closes eligible apps; **R1** closes other eligible apps, keeping the previous app. The single action row follows A–B–X–Y–R1.
- Select/long-press in Manage tasks opens protection/window/restart options. In Switch apps it opens a window picker when the selected app has several windows.
- **Fit all** is the default. View offers exactly four presets: Fit all, Grid, List and Cards. There is no custom layout editor. Explicit preset choices are preserved. Fit all scrolls if the minimum readable size cannot fit the full set.
- Sort offers Recent, RAM size and Alphabetical in an outlined popup. Locked apps sort last in Manage tasks; Switch apps follows the selected sorting order without that protection grouping.
- App windows group into one card by default, with a count and one package-wide RAM sample. Disable Group app windows in Settings > Appearance for individual task cards. A grouped X targets all represented windows; separate cards target one window.
- Compact toolbar: Refresh, Sort, View, mode selection and Settings. No brand heading, Connect shortcut or Diagnostics screen uses deck space. Buttons have smaller visual surfaces while retaining 48dp touch targets.

Closing is immediate, with no confirmation dialog. Use disposable app state for testing; unsaved game progress is not saved automatically.

## Installation and first launch

Android 10+ and **Shizuku 13+** are required. Root is not required. Android 11+ can start Shizuku with wireless debugging; Android 10 needs its documented computer setup. Firmware permissions vary.

1. Install the APK over the previous TaskDeck prototype without uninstalling to retain preferences. It uses the same local debug signing key, not a production key.
2. Install Shizuku from its [official download page](https://shizuku.rikka.app/download/) and follow the [official setup guide](https://shizuku.rikka.app/guide/setup/). Pairing is not the same as starting Shizuku. Without root, start it again after reboot.
3. First launch walks through eight focused screens: connection, confirm calibration, open-menu shortcut, direct-switch shortcut, shortcut-service access, appearance, closing behavior and notifications. Controller steps are separate; optional steps can be skipped with Next. L1 goes back; R1 goes forward.
4. Finish saves setup completion and draws a brief completion checkmark before opening the deck. Later launches go directly to the last deck mode. **Settings is a category list, not a repeated setup wizard.**
5. Test switching and closing with two noncritical apps before using a game with unsaved state.

## Settings

Settings has Connection, Controller, Appearance, Closing behavior and Notifications categories, with an adjacent content pane in landscape. Subpages return to Settings after setup. Connection/reconnect tools live in Connection; there is no permanent Connect button on the deck.

**Stay open after clearing** is a stable-label toggle. Highlighted **On** keeps Manage tasks open after bulk clearing. Outlined **Off** returns HOME for Close all, or to the retained app for Close other apps. Errors and unverified results keep the deck open. Empty bulk actions still follow the chosen exit behavior.

RAM feedback and the notification shortcut use the same On/Off convention. Notifications also contains Test feedback and Android permission settings. Android controls actual heads-up/banner visibility. Connection > Last action names confirmed, failed and unverified outcomes for every attempted app/window. Reports are held only in memory.

The full interaction plan is in [USER-FLOW.md](USER-FLOW.md).

## Shortcuts and entry points

**Quick Settings:** add the TaskDeck tile in Android's tile editor. **Notification:** enable Notification shortcut in Settings > Notifications, with Android notification permission if required.

**Controller:** Settings > Controller > Configure shortcuts records two independent digital chords: one opens the deck, the other directly switches to the previous distinct app without opening TaskDeck. Each is optional and independently enabled. Enable TaskDeck's Accessibility service to use them. It handles key events only; it does not retrieve screen contents or typed text.

Hold up to four desired buttons together, release them, inspect Android key names/scan codes and save with Start or Confirm. D-pad, Confirm, Back and Start remain reserved during recording. Equal or subset/superset conflicts between the two chords cannot be saved. Partial combinations can reach the current game before the completing key is intercepted; choose unused combinations. Held events do not repeatedly trigger. Different controllers/scan codes are kept separate by the chord recognizer.

If both rear buttons report identical key AND scan codes, assign different digital outputs in Retroid settings. Guide/Home and analog-only keys may be intercepted by firmware. Printed-A calibration infers the common AB/XY swap, not arbitrary button permutations. Direct switching depends on firmware task ordering; HOME, split-screen and dock remain acceptance cases.

## RAM charts and feedback

All displayed memory uses **decimal MB: 1 MB = 1,000,000 bytes**. Android PSS input is KiB and is converted, not relabeled. Unknown samples are unavailable, never zero.

Manage tasks shows used/available system RAM and a system usage bar. App bars compare approximate package PSS. Visible sidebar entries adapt to the available height. **RAM details** exposes every reported entry through paginated charts, without a fixed three-entry cutoff. Values and text descriptions accompany colors; app PSS is not a decomposition of total system RAM and should not be added to match system usage. Android, services, shared pages and cache account for other usage.

Task listing completes independently of RAM sampling. A slow or unavailable sample does not disable opening/closing. Old samples are discarded after refresh or catalog changes. Ordinary task removal is not evidence that an app process stopped.

Feedback reports signed available-RAM change and current available MB. It does not prove that a specific amount was reclaimed or that game performance improved. An Application-owned handler coalesces feedback; bulk exit publishes before leaving the Activity. It requests a toast and, when permitted, a quiet high-importance `ram_results` notification. This survives Activity exit, not Android killing/force-stopping the entire app. Channel blocks, notification permission, DND and gaming firmware control presentation. Test feedback does not pretend to close apps.

## Closing, protection and limits

Ordinary closing removes real recent/running tasks, similarly to Android Overview. It does not force-stop all app services. Required keyboards, TaskDeck, PadBoard, Shizuku and core services stay protected and are excluded from closable cards. Ordinary system apps such as Settings can be dismissed unless user-locked. HOME launchers and the recognized Wave frontend start locked but may be unlocked. Installed-but-unopened launchers never generate synthetic cards. Background-only apps are information, not task cards.

Launcher-only close requests a package force-stop and removal of original task records. Restart is an explicit one-off action retaining its saved lock. The backend rechecks package, current user and required-service protection. Launcher stop verifies original PIDs/tasks; restart checks a new nonempty PID set. Android may automatically reopen its default HOME launcher. Task removal is checked against a fresh list; unresolved/denied samples are reported as unverified. Missing protection discovery fails closed.

Requests have deadlines, late responses are ignored and a stalled connection gets one automatic reconnect attempt. Destructive actions are **never automatically replayed**: Android may already have accepted them before a timeout. Errors/partial results remain available in Last action. There is no arbitrary shell-command endpoint.

TaskDeck does not replace native swipe-up/Quickstep, obtain live task screenshots, perform emulator savestates or offer general-purpose package force-stop. It is not a RAM booster. Native gesture replacement is separate system integration; this app uses Shizuku and optional key/tile/notification entry points.

## Privacy and build

No INTERNET permission, ads, analytics, screen-content retrieval or typed-text logging. Task/PSS snapshots, results and feedback diagnostics remain in memory. Preferences persist explicit configuration such as locks, unlocked launchers, theme, view/mode, shortcuts, calibration and closing/notification choices.

Build with JDK 17, Android SDK 35 / Build Tools 35.0.0 and Gradle 8.11.1:

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Windows uses gradlew.bat. Set ANDROID_HOME or local.properties. The debug key is in ignored `.local/`; source archives exclude that key and build caches. Application versionCode is 9; UserService implementation remains 8, with unchanged privileged protocol from 0.7.

See [VALIDATION.md](VALIDATION.md) for automated results and [DEVICE-TEST.md](DEVICE-TEST.md) for outstanding hardware checks. UI tests use fake IPC; actual RP6 Shizuku behavior, controller routing and visible RAM banners are not proven by them.
