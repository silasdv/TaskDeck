# Changelog

## 0.6.1-prototype

- Removed synthetic cards created from installed launcher packages. Only real Android recent/running tasks appear; installation alone no longer creates a launcher entry.
- Frontend also rejects legacy synthetic rows, including rows with a memory sample. An unlocked launcher that is closed disappears when Android no longer reports its task.
- Existing lock/unlock/restart behavior remains available for actual launcher tasks. Background processes without a task stay in background information.
- VersionCode 7 and UserService implementation version 7 force the changed backend to load on upgrade; AIDL protocol is unchanged.

## 0.6.0-prototype

- Reordered the single-row actions to A, B, X, Y, R1.
- Added persistent Cards, List, Grid and Fit all modes. Grid defaults to multiple rows and uses more screen width; Fit all scales tiles to the reported task count. Dense modes show available RAM compactly instead of reserving a wide sidebar. Selected names remain visible in the status line.
- D-pad/stick navigation follows grid rows/columns or list rows, with Up from the first row entering tools. Cards keep horizontal navigation.
- Right-aligned Next/Finish in setup, labeled rectangular L1 Previous / R1 Next badges and matching shoulder shortcuts. Repeated key-downs do not skip multiple steps.
- Bulk exit/restart publishes pending RAM feedback while still foreground. Added quiet high-importance RAM result notification channel, Test RAM feedback, runtime permission request/status and an in-memory last result. Existing notification channel blocks and RAM-feedback preference remain respected. Hardware delivery is still an acceptance test.
- VersionCode 6; privileged backend API remains version 5 because no backend protocol changes were needed. Shizuku stays required.

## 0.5.0-prototype

- One visible row of five controller actions at 640dp landscape; removed the duplicated bottom legend. Narrow portrait keeps a single horizontally scrollable row.
- All discovered HOME launchers and the recognized Wave frontend default to protected, with persistent per-package unlock/relock choices. Unlocked launcher close actions request a launcher-scoped force-stop through Shizuku.
- Long-press/Select offers Restart launcher, usable once while retaining protection. D-pad/stick chooses popup actions; Confirm executes. Keyboard/core protection remains enforced by the backend.
- Moved RAM feedback to an Application-owned handler. A standard Android toast reports measured RAM change and available RAM even after the switcher Activity finishes/destroys, without POST_NOTIFICATIONS. Rapid close actions coalesce. Optional notification-shade results still respect permission/channel settings; disabling feedback suppresses both.
- Idle synthetic launcher shortcuts are not counted as running closable apps. Android may reopen its default HOME launcher after a stop.
- Shizuku remains the privileged connection method. UserService/API version 5, versionCode 5, no added permissions or direct-root/embedded-ADB mode.

## 0.4.0-prototype

- Fixed empty/all-protected deck entry: controller focus starts on Refresh, horizontal toolbar navigation no longer resets to nowhere, and Down cannot enter a nonexistent card. Shortcut entry restores controller mode after previous touch use.
- HOME and Wave are visible protected launcher cards, including a launcher shortcut when Android omits HOME from recents. Other infrastructure remains in the background summary. Opening a launcher uses HOME or its launch intent rather than startActivityFromRecents on a HOME task.
- Combine recent and running task IDs where supported; this can reveal Settings/other excluded-recents activities. A process without its own task is not presented as a closable task.
- Small right-hand RAM summary explains system/cache usage and lists the top three protected/taskless app memory samples plus the remaining count. Missing samples are n/a, not zero.
- Neutral menu icons, proper Setup gear, SNES palette for circular face-button badges and a rectangular R1 badge. R1 is labeled “Close other apps”; action labels describe their outcome.
- Two-column landscape setup, connection and recording screens without scrolling; Back is separated from the title. Portrait retains scrolling for accessibility.
- UserService version 4, versionCode 4; no new permissions.

## 0.3.0-prototype

- Separate touch/controller modes: first tap activates; explicit rounded controller focus on setup controls and sort choices.
- Four setup screens with Next/Previous and actual connection, saved mapping, Accessibility and notification status. Controller navigation/Confirm/Start remain usable during macro recording.
- Notification shortcut can be hidden again; permission/channel blocking is visible. RAM notifications have one stateful enable/disable action.
- Sort by Recent, RAM or Alphabetical. Manual locks always sort last; mandatory protected infrastructure is hidden. Empty or entirely locked decks show “Nothing to close”.
- New openings select the first visible card. Refreshes retain the current selection where possible.
- Y closes eligible tasks and exits to HOME; R1 closes others and returns to the app identified on entry, retaining all of that package’s tasks. Both handle empty lists. A setting keeps the deck open instead; operation errors remain visible.
- Removed the blanket system-app and foreign-top-activity exclusions. Targeted launcher, IME, core-service and manual protection still apply. This changes TaskDeck policy, not Android’s system gesture provider.
- Colored scalable action vectors, compact landscape cards and wrapped narrow-screen controls.
- UserService version 3 loads the changed backend on upgrade. No new permissions.

## 0.2.0-prototype

- Replaced native list focus with explicit app-card selection for D-pad and left-stick input. A opens the selected task, X closes it immediately, Y closes eligible tasks without a confirmation dialog.
- Horizontal cards, compact landscape layout and distinct Light/Dark/OLED palettes.
- First-run setup, connection and controller-recording screens replace setup dialogs.
- Long-press or Select opens a protection popup. A locks/unlocks the app, B dismisses. Persistent user locks are checked by the privileged backend and shown with vector lock icons.
- HOME launchers and Wave Launcher are protected. Android Settings is closable unless manually locked.
- Multi-button shortcut recording shows held/captured names and scan codes. Save with Start after releasing the buttons. Partial combinations pass through, complete chords trigger once; legacy single-button shortcuts are disabled on upgrade.
- Best-effort per-app PSS, explicit unavailable/shared states and optional notifications showing the measured available-RAM delta after closing. No guaranteed RAM-reclamation or performance claim.
- Shizuku UserService version increased so existing connections load the new backend.

## 0.1.0-prototype

Initial local Shizuku task reader, switcher and task remover with a Quick Settings tile and single-key shortcut. User feedback exposed broken controller selection, cramped vertical layout and intrusive setup/close dialogs; these are the main 0.2 changes.
