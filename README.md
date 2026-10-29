# TaskDeck — controller task switcher prototype

An experimental Android task switcher by **SilasDV**. Working name, version **0.2.0-prototype**. Built with substantial AI assistance through Codex (vibecoded). This is a device-test build; the redesigned interface still needs Retroid Pocket 6 testing.

## Included

- A Shizuku UserService that reads real recent tasks, brings an existing task forward and removes an eligible task.
- Horizontal app cards with explicit D-pad/left-stick selection independent of Android touch focus. A opens, X closes one task, Y closes eligible tasks, B exits.
- Closing is immediate: no confirmation dialogs. Unsaved work or game progress may be lost.
- Long-press a card or press Select to open its protection popup. A toggles the lock; B dismisses. User protection is persistent and enforced in the backend.
- Separate first-run setup and connection screens, plus Light, Dark and OLED themes.
- Printed-A calibration for standard/swapped face-button reporting.
- A Quick Settings tile, notification shortcut and optional recorded multi-button controller shortcut, with visible Android names and scan codes.
- Available/total RAM, best-effort per-app PSS samples, and optional RAM-change result notifications, without RAM-boost claims.
- Explicit backend errors and protected tasks rather than pretending an action succeeded.

## Requirements and installation

Android 10+; **Shizuku 13+**. Android 11+ allows Shizuku startup through wireless debugging without a PC. On Android 10, use Shizuku's documented computer-based setup. Root is not required for the proposed shell backend, but firmware permissions can differ.

1. Install the TaskDeck prototype APK. This is a local debug-signed test build; permit APK installation from the app you use to open it.
2. Install Shizuku from its [official download page](https://shizuku.rikka.app/download/).
3. Start Shizuku following its [official setup guide](https://shizuku.rikka.app/guide/setup/). Without root, start it again after a device reboot.
4. Open TaskDeck's first-run setup, then **Connect Shizuku > Authorize / Connect**. Approve Shizuku's access prompt.
5. Check the connection status, then finish setup to enter the app deck. Subsequent launches open the deck directly. An empty list does not establish compatibility.
6. Test with a browser and another non-critical app before trying a game with unsaved progress.

## Opening without the launcher

**Quick Settings:** edit Android's Quick Settings tiles and add **TaskDeck**. Tapping it directly opens the switcher. This does not require TaskDeck's Accessibility service.

**Notification:** Setup > Show notification shortcut, then grant notification permission if asked. Tapping the notification opens the switcher directly. Notification availability also depends on Android notification settings.

**Controller:** Setup > Record button combination. Hold the desired buttons together, release them, verify the displayed names/scan codes, then save with **Start** or the on-screen button. Up to four digital buttons are supported. Enable the optional Accessibility service after reading the inline disclosure. Use the same setup page to disable the shortcut. Previous single-button bindings are disabled on upgrade and must be recorded again.

Partial combinations pass through. The service intercepts the completing key's down/up pair only when the configured buttons are held on one device without an extra tracked key. The first key can already reach a game before the chord completes. This cannot undo a game's response or predict whether you intended to add another button afterwards; choose an unused full combination. Held/repeated events do not repeatedly open the deck. A matching key code with a different scan code is treated as a different button.

If both rear buttons report the same key code AND scan code, they cannot be distinguished here; map them to different digital outputs in Retroid's settings. Guide/Home or analog-only inputs may not reach the key filter. RP6 firmware routing requires hardware testing.

If A/B are reversed, choose Setup > Calibrate printed A. It infers the common AB/XY pair swap, not arbitrary button permutations. In the deck, left/right select cards, up selects toolbar controls, down returns to cards. Select opens the lock popup.

## What closing means

X and Y **remove recent tasks**, similar to dismissing cards in Android Overview. They do not implement package-wide force-stop, do not guarantee that all background services terminate and do not save emulator state. Unsaved work may be lost. Y operates on the eligible tasks shown when requested, not every Android process.

The privileged backend rechecks task ID, expected package and protection policy immediately before removal. HOME launchers, Wave Launcher discovered by its launcher label, input methods, TaskDeck, PadBoard and Shizuku are protected. Most system apps remain protected, but **com.android.settings is explicitly closable** unless the user locks it. Other-user and foreign-top-activity tasks remain excluded. If HOME/input-method discovery fails, removal fails closed. User-selected package locks are saved and synchronized to the privileged backend before removal. There is no arbitrary shell-command interface.

## RAM measurements

The backend attempts to aggregate proportional set size (PSS) for reported processes belonging to each app. Values are approximate, can be cached by Android and are shared across multiple task cards for the same app. Shared-package processes are marked shared. Missing/partial/denied samples are not shown as zero; an app absent from the process snapshot is labeled accordingly.

After closing tasks, the deck reports total available RAM and its signed change after a short delay. This is not proof of RAM reclaimed by those tasks: Android caches and unrelated processes also affect it. Optional Android notifications require notification permission. No promised performance gain or universal per-app measurement is implied.

## Current limits

- User testing of 0.1 confirmed task enumeration and exposed broken controller selection. The 0.2 selection fix, new UI, macro chords and PSS sampling still need hardware acceptance. Hidden APIs can be rejected by unsupported firmware.
- Tested build target: API 35. The first intended hardware test is the RP6's installed firmware, with API level reported by the backend.
- No native swipe-up/Quickstep replacement, live screenshots, package force-stop, per-controller profiles or automatic emulator savestates.
- Global hotkeys require explicitly enabled Accessibility and can conflict with other key-filtering services or reserved firmware keys. Only digital shortcut keys are supported.
- Controller calibration covers A/B and X/Y swapping. PlayStation-specific presentation is not included in this initial prototype.
- Setup/connection/recording are dedicated screens. The app's only contextual popup is the requested lock control. Android permission and Shizuku dialogs remain system controlled.
- This application must not be advertised as a RAM booster. Android manages cached memory itself.

## Privacy

No Internet permission, advertising, analytics, typed-text logging or screen-content retrieval. Task/process samples are held in memory and not written as a usage history. Preferences store configuration: shortcut button identifiers, calibration, theme, setup state, notification choices and explicitly protected package names. Accessibility handles only the opt-in chord and discards accessibility events. Shizuku is a separate application with its own privileges.

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
