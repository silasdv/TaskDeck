# TaskDeck — controller task switcher prototype

An experimental Android task switcher by **SilasDV**. Working name, version **0.6.0-prototype**. Built with substantial AI assistance through Codex (vibecoded). This is a device-test build; the changed interface still needs Retroid Pocket 6 testing.

## Included

- A Shizuku UserService that reads real recent tasks, brings an existing task forward and removes an eligible task.
- Persistent Cards, List, Grid (default) and Fit all views with explicit D-pad/left-stick selection independent of Android touch focus. A opens, X closes one task, Y closes eligible tasks and exits to HOME, R1 closes other eligible tasks while retaining the previous app, B exits.
- Closing is immediate: no confirmation dialogs. Unsaved work or game progress may be lost.
- Long-press a card or press Select to open its protection popup. A toggles the lock; B dismisses. User protection is persistent and enforced in the backend. Launchers default to locked but can be unlocked. Their popup also offers an explicit one-off restart that keeps the saved lock.
- A four-screen setup wizard using two columns without scrolling in landscape, labeled L1 Previous / R1 Next or Finish controls with Next aligned right, a separate connection screen, live status and Light, Dark and OLED themes. Touch activates on the first tap; controller navigation uses explicit selection.
- Printed-A calibration for standard/swapped face-button reporting.
- A Quick Settings tile, notification shortcut and optional recorded multi-button controller shortcut, with visible Android names and scan codes.
- Available/total RAM, best-effort per-app PSS samples, and optional application-owned RAM-change popup feedback, including after the menu exits, without RAM-boost claims.
- Recent, descending RAM and alphabetical sorting. User-locked apps are always last; mandatory infrastructure is summarized separately; all discovered HOME launchers and Wave remain visible as launcher cards, locked by default. Inline “Nothing to close” state.
- Optional stay-open behavior after bulk closing; by default bulk actions return to HOME or the retained app, even for an empty list. Errors keep the menu open for inspection.
- Neutral menu vectors, a gear for Setup, SNES-colored circular A/B/X/Y badges and a rectangular R1 badge; clear “Open app”, “Close app”, “Close all & exit” and “Close other apps” labels. A single row of all five actions follows A–B–X–Y–R1; the duplicated explanatory legend is removed. Scalable action icons and explicit backend errors rather than pretending an action succeeded.

## Requirements and installation

Android 10+; **Shizuku 13+**. Android 11+ allows Shizuku startup through wireless debugging without a PC. On Android 10, use Shizuku's documented computer-based setup. Root is not required for the proposed shell backend, but firmware permissions can differ.

1. Install the TaskDeck prototype APK. This is a local debug-signed test build; permit APK installation from the app you use to open it.
2. Install Shizuku from its [official download page](https://shizuku.rikka.app/download/).
3. Start Shizuku following its [official setup guide](https://shizuku.rikka.app/guide/setup/). Without root, start it again after a device reboot.
4. Open TaskDeck's first-run setup, then **Connect Shizuku > Authorize / Connect**. Approve Shizuku's access prompt.
5. Check the connection status, then finish setup to enter the app deck. Subsequent launches open the deck directly. An empty list does not establish compatibility. When no tasks are closable, controller focus starts on Refresh. Protected launcher cards remain accessible with Down; a truly empty deck stays in the toolbar.
6. Test with a browser and another non-critical app before trying a game with unsaved progress.

## Opening without the launcher

**Quick Settings:** edit Android's Quick Settings tiles and add **TaskDeck**. Tapping it directly opens the switcher. This does not require TaskDeck's Accessibility service.

**Notification:** Setup > step 4 > Show notification shortcut, then grant notification permission if asked. Tapping the notification opens the switcher directly. The same control changes to Hide notification shortcut and removes it. Notification availability also depends on Android notification settings and channel permission; the setup shows actual visibility.

**Controller:** Setup > step 2 > Record button combination. Hold the desired buttons together, release them, verify the displayed names/scan codes, then save with **Start** or the on-screen button. Up to four digital buttons are supported. D-pad, calibrated Confirm, Back and Start are reserved for navigation/cancel/save while recording; they are not added to the chord. After releasing a recorded chord, Confirm can save it, or use Start. Enable the optional Accessibility service after reading the inline disclosure. Use the same setup page to disable the shortcut. Previous single-button bindings are disabled on upgrade and must be recorded again.

Partial combinations pass through. The service intercepts the completing key's down/up pair only when the configured buttons are held on one device without an extra tracked key. The first key can already reach a game before the chord completes. This cannot undo a game's response or predict whether you intended to add another button afterwards; choose an unused full combination. Held/repeated events do not repeatedly open the deck. A matching key code with a different scan code is treated as a different button.

If both rear buttons report the same key code AND scan code, they cannot be distinguished here; map them to different digital outputs in Retroid's settings. Guide/Home or analog-only inputs may not reach the key filter. RP6 firmware routing requires hardware testing.

If A/B are reversed, choose Setup > step 2 > Calibrate printed A. It infers the common AB/XY pair swap, not arbitrary button permutations. In the deck, left/right select cards, up selects toolbar controls, down returns to cards. Select opens the lock popup.

## App views

Choose **View** in the top toolbar: Cards, List, Grid or Fit all. Grid is the default and shows multiple rows; List uses compact rows and full-width names. Dense views collapse the wide RAM sidebar while retaining available-memory information in the header; Cards retains detailed background-memory information. The sidebar is also shown when nothing is closable. Fit all adapts columns and tile height to fit the reported task set without scrolling; large sets shrink labels/icons and the selected name remains in the status line. There is no promise that arbitrarily large sets remain comfortably readable at once. Grid/List scroll to the selected app if necessary. D-pad/stick up/down moves between rows, left/right between columns; Up from the first row enters the toolbar.

## What closing means

For ordinary apps, X and Y **remove tasks**, similar to dismissing cards in Android Overview. Those actions do not implement package-wide force-stop, do not guarantee that background services terminate and do not save emulator state. **Launcher-only exception:** explicitly unlocked launcher close actions use Shizuku to request a package force-stop; the explicit Restart launcher action stops then opens that launcher, while retaining its saved protection. Unsaved work may be lost. Y operates on the eligible tasks shown when requested, not every Android process.

The privileged backend rechecks task ID, expected package and protection policy immediately before removal. Input methods, TaskDeck, PadBoard, Shizuku and core services remain protected. HOME launchers and the recognized Wave frontend start locked; their protection is user-configurable and persisted per package. There is **no blanket system-app restriction**: ordinary system-app tasks, including Settings, can be dismissed unless the user locks them. Android/SystemUI and other-user tasks remain excluded. Tasks with a foreign top activity are no longer blocked solely for that reason. HOME launchers and Wave are visible as launchable cards, including an explicitly marked launcher shortcut when there is no recent task. Other protected services and apps without a dismissible task appear in a small background-memory summary. If HOME/input-method discovery fails, removal fails closed. User-selected package locks are saved and synchronized to the privileged backend before removal. There is no arbitrary shell-command interface. Launcher stop requests are restricted to discovered launcher packages for the current Android user, recheck core/keyboard protection and require either an unlocked package or an explicit restart operation. Android can reopen its default HOME launcher automatically, particularly when returning HOME; it cannot be promised to remain stopped.

Close others identifies the previous non-TaskDeck task on entry using a best-effort running-task query, with recent-order fallback on unsupported firmware. It retains all tasks of that package, independently of the selected card or sort order. HOME is returned to as HOME. Split-screen, unusual firmware task ordering, and cross-display focus remain device acceptance cases. Finishing TaskDeck means closing its UI, not killing its shortcut service.

Launcher stop calls use the platform [IActivityManager.forceStopPackage](https://github.com/aosp-mirror/platform_frameworks_base/blob/android13-release/core/java/android/app/IActivityManager.aidl), isolated in the Shizuku UserService. A one-off restart is not a change to saved protection.

## Platform limits and research

Android’s [ActivityTaskManagerService](https://android.googlesource.com/platform/frameworks/base/+/571c1f9bf000/services/core/java/com/android/server/wm/ActivityTaskManagerService.java) checks REMOVE_TASKS before task removal; it does not categorically reject every system package. TaskDeck’s former blanket rule was its own conservative restriction, now removed. The remaining explicit protections are app policy, not a claim that Android cannot dismiss those cards.

Replacing the native swipe-up Quickstep provider is a separate system-integration project. [QuickSwitch](https://github.com/skittles9823/QuickSwitch) requires Magisk/KernelSU/APatch and a compatible launcher. This Shizuku build offers its own controller/notification/tile entry and does not replace the system gesture or require rooting the RP6.

The task reader combines recent and running task IDs where the firmware permits it, so activities excluded from recents can still appear. Ordinary Settings tasks remain closable. An app process without its own task cannot be dismissed as a task; it is summarized instead. Unsupported running-task APIs fall back to recents.

## RAM measurements

The right sidebar shows used/available system RAM, explains that Android/services/caches still use memory, and lists up to three protected or taskless app processes with approximate PSS (or n/a), followed by the remaining entry count. It is a compact sample, not an exhaustive process manager; figures cannot simply be summed to equal system RAM usage. Background app metadata and PSS remain transient.

The backend attempts to aggregate proportional set size (PSS) for reported processes belonging to each app. Values are approximate, can be cached by Android and are shared across multiple task cards for the same app. Shared-package processes are marked shared. Missing/partial/denied samples are not shown as zero; an app absent from the process snapshot is labeled accordingly.

Single-app close feedback is coalesced by an Application-owned handler. Before bulk exit or launcher restart, pending feedback is flushed while TaskDeck is still foreground, then the Activity finishes. This avoids relying solely on delayed background toast delivery. Text toasts need no notification permission. When permitted, a separate high-importance, quiet `ram_results` channel posts a 20-second result notification eligible for heads-up display. Existing user-disabled `ram`/result channels remain respected. Android, Do Not Disturb and gaming firmware still control banner presentation.

Setup step 4 includes **Test RAM feedback**, **Permissions**, notification permission status and the last result held in memory. Test requests POST_NOTIFICATIONS when needed and still offers toast fallback if denied; it does not pretend an app was closed. Test output can be inspected without closing anything. RAM feedback remains independently switchable. The Activity-destruction tests verify wiring; they do not demonstrate banner delivery on RP6 firmware or survival after Android kills/force-stops the entire TaskDeck process.

The deck reports total available RAM and its signed change after that short delay. This is not proof of RAM reclaimed by those tasks: Android caches and unrelated processes also affect it. Optional notification-shade entries require notification permission; the result popup does not. No promised performance gain or universal per-app measurement is implied.

## Current limits

- User feedback on 0.5 requested denser views, reordered actions, shoulder-based setup navigation and further RAM feedback fixes. Actual notification/banner delivery on RP6 firmware remains unverified; use Test RAM feedback and report permission/game-mode state if it remains invisible. Hidden APIs can be rejected by unsupported firmware.
- Tested build target: API 35. The first intended hardware test is the RP6's installed firmware, with API level reported by the backend.
- No native swipe-up/Quickstep replacement, live screenshots, general-purpose app force-stop, per-controller profiles or automatic emulator savestates. Force-stop is narrowly limited to launcher closing/restarting.
- Global hotkeys require explicitly enabled Accessibility and can conflict with other key-filtering services or reserved firmware keys. Only digital shortcut keys are supported.
- Controller calibration covers A/B and X/Y swapping. PlayStation-specific presentation is not included in this initial prototype.
- Setup/connection/recording are dedicated screens. Contextual popups are limited to the requested lock control and sorting choices. Closing does not prompt. Android permission and Shizuku dialogs remain system controlled.
- This application must not be advertised as a RAM booster. Android manages cached memory itself.

## Privacy

No Internet permission, advertising, analytics, typed-text logging or screen-content retrieval. Task/process samples are held in memory and not written as a usage history. Preferences store configuration: shortcut button identifiers, calibration, theme, setup state, sort/view/exit choices, notification choices and explicitly protected package names and unlocked launcher packages. Accessibility handles only the opt-in chord and discards accessibility events. Shizuku is a separate application with its own privileges.

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
