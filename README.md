# TaskDeck — controller task switcher prototype

An experimental Android task switcher by **SilasDV**. Working name, version **0.1.0-prototype**. Built with substantial AI assistance through Codex (vibecoded). This is a device-test build, not a claim of verified Retroid Pocket 6 compatibility.

## Included

- A Shizuku UserService that reads real recent tasks, brings an existing task forward and removes an eligible task.
- D-pad/left-stick selection; A opens, X requests closing one task, Y requests closing eligible tasks, B exits.
- Confirmation dialogs for closing tasks, including controller confirmation/cancellation.
- Printed-A calibration for standard/swapped face-button reporting.
- A Quick Settings tile, optional notification shortcut and optional learned global controller shortcut.
- Available/total RAM display, without RAM-boost claims.
- Explicit backend errors and protected tasks rather than pretending an action succeeded.

## Requirements and installation

Android 10+; **Shizuku 13+**. Android 11+ allows Shizuku startup through wireless debugging without a PC. On Android 10, use Shizuku's documented computer-based setup. Root is not required for the proposed shell backend, but firmware permissions can differ.

1. Install the TaskDeck prototype APK. This is a local debug-signed test build; permit APK installation from the app you use to open it.
2. Install Shizuku from its [official download page](https://shizuku.rikka.app/download/).
3. Start Shizuku following its [official setup guide](https://shizuku.rikka.app/guide/setup/). Without root, start it again after a device reboot.
4. Open TaskDeck and choose **Connect**. Approve Shizuku's access prompt.
5. Check that the status shows backend UID **2000** (shell) or **0** (root), Android API and actual tasks. An empty list does not establish compatibility.
6. Test with a browser and another non-critical app before trying a game with unsaved progress.

## Opening without the launcher

**Quick Settings:** edit Android's Quick Settings tiles and add **TaskDeck**. Tapping it directly opens the switcher. This does not require TaskDeck's Accessibility service.

**Notification:** Setup > Show notification shortcut, then grant notification permission if asked. Tapping the notification opens the switcher directly. Notification availability also depends on Android notification settings.

**Controller:** Setup > Learn controller shortcut, then press a spare controller button or a device macro mapped to F1–F12. Enable Setup > Enable shortcut accessibility service after reading its separate disclosure. That selected button is reserved for TaskDeck while enabled; normal keys pass through. Use Setup > Disable controller shortcut to release it. Some Guide/Home buttons never reach apps. The RP6 macro editor's available outputs and key routing require hardware testing.

If A/B are reversed, choose Setup > Calibrate confirm button and press printed A. It infers the common AB/XY pair swap, not arbitrary button permutations. The dedicated shortcut must not reuse a button you need to navigate games or TaskDeck; prefer a spare macro key.

## What closing means

X and Y **remove recent tasks**, similar to dismissing cards in Android Overview. They do not implement package-wide force-stop, do not guarantee that all background services terminate and do not save emulator state. Unsaved work may be lost. Y operates on the eligible tasks shown when requested, not every Android process.

The privileged backend rechecks task ID, expected package and protection policy immediately before removal. System apps, another user's tasks, the current home app, every discovered input method, TaskDeck, PadBoard and Shizuku are protected. If input-method discovery returns no results, removal is disabled. Tasks hosting a foreign top activity are also protected. There is no arbitrary shell-command interface.

## Current limits

- No physical RP6 task-switching/closing result has been confirmed yet. The backend uses hidden Android APIs with runtime reflection; unsupported firmware can reject the calls.
- Tested build target: API 35. The first intended hardware test is the RP6's installed firmware, with API level reported by the backend.
- No native swipe-up/Quickstep replacement, live screenshots, package force-stop, per-controller profiles or automatic emulator savestates.
- Global hotkeys require explicitly enabled Accessibility and can conflict with other key-filtering services or reserved firmware keys. Only digital shortcut keys are supported.
- Controller calibration covers A/B and X/Y swapping. PlayStation-specific presentation is not included in this initial prototype.
- Setup dialog controls are controller navigable. System permission and Shizuku dialogs remain controlled by Android/the external app.
- This application must not be advertised as a RAM booster. Android manages cached memory itself.

## Privacy

No Internet permission, advertising, analytics, typed-text logging or screen-content retrieval. Task names/packages are held in memory for the visible list and are not written as a usage history. Preferences store only shortcut/calibration settings. Accessibility handles only the opt-in shortcut and discards accessibility events. Shizuku is a separate application with its own privileges.

## Build

JDK 17, Android SDK 35 / Build Tools 35.0.0, Gradle 8.11.1. Set ANDROID_HOME or local.properties.

```sh
./gradlew assembleDebug testDebugUnitTest lintDebug
```

Windows: use gradlew.bat. A local debug key is created in ignored `.local/`. Do not distribute that key or build caches in public source archives.

## Device acceptance test

See [DEVICE-TEST.md](DEVICE-TEST.md). Automated checks do not simulate Shizuku's actual shell identity or prove Retroid system-service behavior.

## License and dependencies

MIT. Shizuku API/provider 13.1.5 are used under their MIT license; see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). Android framework APIs/AIDL are platform-provided. No emulator or other app source has been copied into the implementation.
