# 1.1.6

- Reset Tasks → dock entry to the leftmost item, regardless of the previously visited right corner; retain RAM → right-corner routing.
- Expand accent selection to sixteen larger swatches in complete responsive rows, with distinct stored-selection/focus rings and theme reset.
- Measure menu labels and compound content with generous horizontal padding; allow wrapping/height instead of narrow caps, ellipsis or font shrinking.
- Clarify required/optional Setup steps, offer inline A/B/X/Y or PlayStation labels before calibration and use a two-by-two theme grid.
- Improve common recovery summaries and consolidate the current interaction guide; record the Nielsen heuristic and Setup audit.

# Changelog

## 1.1.5

- RAM Details Down selects the right Thumb corner; Up returns from that corner.
- Accent Colors inline in Appearance: color-only dots, no rectangular backgrounds, with selection/focus rings and accessible names.
- Responsive picker icons stay visible in short tiles. Last action page controls align left/right.
- Shared content-based button widths per menu pane; additional theme-button horizontal padding.
- VersionCode 115, same release key. Physical RP6 acceptance remains open.

## 1.1.4

- Bounded picker ImageViews, matching App RAM frame, centered Current badge/icon group and a labeled empty-dock action with bounded focus.
- Individual app boxes in Thumb corners, maintaining independent scrolling and reorder support.
- Round accent-color palette under Appearance; twelve colors plus original-theme reset. Semantic Current green stays independent.
- Outlined page-button hints and separate remapping across picker, Last action, RAM and windows. Last action starts on an enabled control.
- Explicit toolbar/RAM/right-dock controller paths. Settings Back returns from content to categories before closing.
- VersionCode 114; same release key. Hardware acceptance remains required.

## 1.1.3

- Consistent 8dp main border spacing across toolbar/tasks, cards, RAM sidebar and dock.
- Face badges increased to 20dp while preserving text and button/background dimensions; shoulder badge unchanged.
- Dedicated System RAM background card with its own usage chart; app estimates stay separate. Matching portrait summary surface.
- VersionCode 113; same release key.

## 1.1.2

- Four app tiles per picker page with search and L1/R1 paging; Back remains a separate action.
- Original-size bottom dock preview with distinct installed apps and sample-count plus/minus controls.
- Current badge above icon/name; shared green Current dock fill/outline/dot. Symmetric sparse-dock padding and no empty corner shell.
- Confirm enters Settings content; removed dock heading and idle selection/task-count labels. Dock sizing reserves space from the task area. Fit all/Recent stay the defaults for unset choices.
- VersionCode 112, same release key; physical RP6 acceptance remains required.

## 1.1.1

- Alphabetical full-width app picker with large icons, plain names, selected favorite state, search and L1/R1 paging/back.
- Thumb corners default when unset; live dock style/theme/size preview. Centered icons magnify inside reserved bubbles rather than enlarging clipped containers.
- Separate colored Current badge, increased header and RAM-control padding, full-height backgrounds matching Settings.
- Deterministic horizontal form-pane navigation, including Mapping to Labels; search can return to results with controller input.
- VersionCode 111, existing release key retained. Physical-device drag, firmware and visual acceptance remain required.

## 1.1

- Quick Access favorites dock with Floating, Compact, Shelf and Thumb corners styles. Thumb corners groups shortcuts at the bottom left/right with larger targets and independent overflow scrolling.
- Pin with card Select/long-press or choose installed apps in Dock settings. Touch drag reorders; controller Select provides move/remove actions. Favorites do not create task cards or change protection.
- Dock size (60–160%), theme, visibility, magnification and running indicators. Automatic app theme follows Android. Current marks the reported app used before TaskDeck.
- Moved mapped controller hints to the top right beside Settings; reduced button text/badges, redrew Refresh and made Settings backgrounds/focus truly circular.
- RAM details reachable with D-pad/left stick. Setup completion holds longer and supports Confirm to continue; permission indicators verify live access rather than saved settings.
- VersionCode 110, same dedicated release key as 1.0. Real RP6 drag-and-drop, firmware origin reporting and notification behavior remain hardware acceptance cases.

## 1.0

- One main deck; removed the separate Switch apps mode and mode tabs. Each opening selects the first app card, including a protected card; only an empty deck focuses Refresh.
- Smaller main controls, icon-only Settings, and Close all but current with a smaller mapped shoulder label and no decorative action icon.
- System and app diagrams share the total physical RAM denominator. All figures remain decimal MB; free/available RAM numbers are removed, including feedback notifications.
- Green connected checkmark, corrected feedback-test bell, closing behavior merged into Appearance, and About with version, SilasDV, vibecoded information, GitHub and Ko-fi.
- PadBoard-style controller mapping: A/B/X/Y or vector PlayStation labels, swapped variants, confirm calibration, six individual actions and conflict-swapping assignments. Mapped Confirm/Back also operate Settings. Reset keeps the label profile.
- Dedicated private release key, non-debuggable APK/App Bundle, versionCode 100, public-source installation/privacy/contribution/releasing documentation and Ko-fi funding metadata. Earlier 0.x debug-signed APKs require uninstalling before this release.
- Shizuku remains required. Latest RP6 firmware behavior and visible Android banners remain hardware acceptance cases.

## 0.8.0-prototype

- Split Switch apps from Manage tasks. Switch apps has selection/opening only; Manage tasks adds close/protect/RAM tools. Toolbar and labeled L1 change mode; last mode is remembered.
- Eight focused first-launch steps separate connection, confirm calibration, each chord, shortcut-service access, appearance, closing and notifications. Optional steps can be skipped. Finish draws a completion checkmark; subsequent launches open the deck.
- Replaced the repeated setup wizard with category-list Settings. Removed the deck Connect button and Diagnostics destination; connection/notification tools remain under their settings categories. Controller categories open on navigation; left/right follows panes and horizontal option rows.
- Fit all is the default, with Cards/List/Grid/Fit all as the only presets. Removed the layout editor; no custom layouts. Kept grouping separately under Appearance.
- All displayed RAM and feedback now use decimal MB, converting system bytes and PSS KiB correctly. Added system/app comparison bars, height-dependent sidebar entries and complete paginated RAM details instead of a fixed three-entry summary.
- Outlined Sort/View popups, stable highlighted On/outlined Off toggles, compact button surfaces with 48dp touch targets, reduced margins and no brand heading on the deck. Calibration preserves controller navigation.
- Application versionCode 9; UserService implementation/protocol remains 8. Hardware acceptance remains required for the changed UI and actual RAM-banner delivery.

## 0.7.0-prototype

- Split task listing from per-app RAM sampling. Slow/unavailable PSS no longer gates selection, opening or closing; stale samples are discarded and icons are cached.
- Added request deadlines, worker recovery and one automatic reconnect attempt. Service binding runs off the UI thread. Late results cannot overwrite newer state; destructive actions are not replayed.
- Verify ordinary removal against current tasks. Launcher close also removes original task records and checks old PIDs; restart verifies different PIDs. Failed, partial and unverified results name each app/window in View > Last action and keep the menu open where appropriate.
- Group windows into one app card by default with one per-app RAM sample, a count and Choose window picker. Separate-window mode remains available; grouped close targets the represented windows.
- Independent Grid columns (Auto/2–10) and minimum label size (10–22 sp). Fit all scrolls if the chosen readable size cannot fit. Added Layout options and rounded, controller-operable controls.
- Separate optional direct-switch chord for the last two distinct apps. Conflicting equal/subset chords are rejected; recording retains reserved navigation controls.
- Diagnostics includes feedback test/status, notification permissions, reconnect and last-action details. Actual RP6 notification visibility and privileged task behavior remain hardware acceptance checks.
- VersionCode/UserService implementation 8. AIDL adds separate memory, previous-app switch and launcher-restart verification operations at IDs 6–8. Shizuku remains required.

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
