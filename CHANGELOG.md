# Changelog

## 2.4 — unreleased

- Use the available settings area instead of leaving a broad empty band above the Dock. Both background panels share one bottom edge; visual choices expand into spare space and compact layouts retain the tested no-scroll fit.
- Separate saved selection (accent fill and a tick) from controller focus (a consistent outline). Layout thumbnails no longer have an extra dark backdrop.
- Use the same minus / percentage / plus stepper for Dock Size and Icon Size; dim unavailable range and preview-count actions.
- Show neutral, captioned background shapes without app artwork. Icon masks retain their real-app examples. Use a two-row background-shape grid when height permits.
- Distinguish None with a slash and Follow theme with a split light/dark sample. Reveal color names in the heading on focus/hover; preserve accessible names and tooltips.
- Label the bottom area Live preview, keep its Dock at the actual size/position, and explain that app-count controls change preview samples only. Use input-specific help and neutral app states, so Current tint no longer conceals the chosen background color.
- Add the mapped Back controller badge to the compact Back button. Preserve controller navigation and saved Dock contents while adjusting preview counts.

## 2.3 — unreleased

- Group Dock style into Dock / Background and Icons within the same screen. Rename Size to Dock Size.
- Hide Background Shape when Background Color is None, preserve the saved shape, and prevent that hidden shape from affecting icon sizing or focus geometry.
- Add a Dock Layout heading and centered icon-over-caption layout choices. Add captions to icon shapes and styles; remove the redundant Follow Accent control and fixed-box explanation.
- Compact and center Swap sides; top-align content panels and leave consistent gaps around preview controls and the Dock.
- Center Dock artwork inside its background while retaining clear running/current indicators.
- Keep App icon style eight dp below the accent palette and support controller navigation from the palette into style choices.
- Migrate the old accent override to the explicit Accent Color style when appropriate.

## 2.2 — unreleased

- Rebuild Dock style as four balanced columns on wide handhelds, or a two-by-two form in portrait. No scrolling is needed in the tested 640x360 dp maximum-size landscape case. Larger screens get larger targets and sample artwork. Style/Swap sides, Size/Icon size, background and icon appearance each have a clear area. Gaps and choice sizes are symmetric.
- Icon shape and Background shape are separate. Background defaults to the previous rounded box family; rounded, round, square and squircle backgrounds are available. Icon choices show the same example app with round/square/squircle masks instead of text labels. Adaptive layers are masked **before** color/look filters, fixing the round system mask surviving Square/Squircle.
- Add Background color **None**, which removes fills and persistent outlines; controller selection can still show a focus outline. Current remains identifiable by its green dot. Larger color samples use a fine contrasting edge only when they blend into the theme. Graphite is lighter so it is distinct from black. Add amber for a complete two-row palette.
- Icon size changes logo fill inside a **fixed** box; it no longer enlarges the box or Dock height. 50–160% selects the usable fill range, with magnification/indicator space reserved. Changing Dock Size sets the box scale; shape combinations retain safe margins.
- Center plus/minus artwork independently of TextView alignment. Shape and look choices use visual examples with accessible labels/tooltips. Saved/current real app artwork is preferred and refreshed after catalogue loading.
- Dock behavior has three matching icon-backed On/Off buttons: Always show Dock, Magnify focused icons, Running indicators. Its right pane explains only the hovered/focused option; touch changes retain the relevant explanation.
- Initial Add apps to Dock results fade in over 220 ms. Stale fades cannot replace a later page transition. Existing 360 ms page motion remains.
- Appearance keeps the standard category heading position. Light/Dark/OLED/Auto have their own row. Group apps (shortened label), Stay open after clearing and Vibrate on close share the next row, with equal heights and readable wrapping. Accent Colors retain their swatches. **App icon style** and four visual samples follow below.
- About shows actual destinations beside Ko-fi and GitHub actions, keeping the confirmed SilasDV URLs. Replace the heart with Ko-fi's official cup from its Creator kit; bundle its source/usage attribution.
- Start uses the same 21x16 dp outlined badge dimensions as Select and shares one button background with the Settings gear. A subtle divider separates View/tools from the action legend.
- Preserve controller focus immediately when rebuilding an option page, preventing a quick second Confirm from activating Back before the deferred focus restoration.
- Cache grid placement specs and reuse layout parameters rather than allocating them during every measurement.


## 2.1 — unreleased

- Dock style & size now includes background color, Round/Square/Squircle shape, Original/B&W/Accent/Glass icon looks and Follow accent color inline. Dock appearance is no longer a destination; old restored destinations redirect to Dock style.
- Smaller, separated size-stepper buttons. Swap sides is a normal action button and participates in spatial controller navigation. Style-preview glyphs stay neutral white on a dark miniature background, independent of app colors.
- Preview does not force the first app selected. Current still uses semantic green. Background swatches have a contrasting dark/light outline as well as selected/focused treatment.
- Icon scale genuinely grows to 160%. Dock Size is a minimum box size: boxes grow when required to contain logos, focus magnification and the indicator strip. Circle, square and squircle focus backgrounds follow the chosen mask. Running/current dots remain below the logo.
- Original/B&W/Accent/Glass task icons are available in Appearance. Glass is a stylized monochrome gradient highlight, not Android system blur or an Apple rendering API.
- Start opens Settings rather than exiting, with an outlined Start hint beside its icon. It is reserved from remapping; old Start action/page bindings migrate to available buttons. Setup/chord save semantics remain available.
- Back from a settings subpage restores its originating content button, e.g. Dock behavior, instead of returning selection to the Dock category. Back again returns to categories before exiting.
- Protected-task lock badges sit above app icons with the same 6dp gap as Current. Current + protected share a badge to avoid a second vertical row.
- Closing can retry exactly once after 50 ms, before final feedback, only after fresh task identity/protection checks. Already absent ordinary tasks are confirmed without another close. Protected/permission failures, unknown connection outcomes, interrupted/deadline-expired work and ambiguous launcher outcomes are not replayed. Persistent failure is reported after the second attempt. Batch deadline remains bounded at 60 seconds.
- Compact landscape Dock panes can scroll when necessary; larger screens show both panes together with the unchanged bottom preview. Appearance toggles share a row with wrapping where space permits, retaining readable text and the accent palette.


## 2.0 — all changes since public 1.0

The local 1.1–1.1.12 updates are included in this release. [Installation, validation and known limits](docs/RELEASE-2.0.md).

### New: the Dock

- Pin apps for quick launch independently of running tasks and protection. Select/long-press a task to pin it, or choose installed apps in Settings > Dock. Favorites never fabricate running task cards.
- Three styles: **Thumb corners** (default), **Floating** and **Shelf**. Thumb corners puts individual app bubbles at the bottom-left/right, with independent overflow scrolling. The former Compact style migrates to Floating.
- Touch hold-and-drag reorders shortcuts; controller task options offer move/remove. Swap sides exchanges Thumb corner groups while preserving odd group sizes and ordering. An empty Dock clearly offers Add apps to Dock.
- Configure always-visible versus reveal-on-navigation behavior, focus magnification and running indicators. The former Android-style visibility popup/slider is now an explicit Always show Dock checkbox.
- **Size and Icon size are separate**, each 50–160%. Changing box size does not change the stored icon scale. Large logos are limited to fit their box and magnified focus state. Existing local Dock sizes initialize the icon scale once to retain their appearance.
- **Background color:** follow the app theme, black, graphite, white, blue, purple, green or red. **Icon shape:** Round or Square, including adaptive icon layers. These controls live in Dock appearance, reachable from Dock style & size.
- The full-width preview occupies the actual bottom Dock position and uses real, distinct installed apps. Saved favorites appear first in their actual order/sides; alphabetical extras follow. Preview changes never change saved favorites. Thumb corners has left/right add/remove sample controls; Floating and Shelf have only one pair. Plus/minus appears once as an icon.

- Empty-Dock add text adapts to the chosen background and focus fill; all color presets meet 4.5:1 text contrast.

### App picker and task deck

- Searchable, alphabetical **eight-app grid**, two rows of four, without scrolling inside a page. Large, bounded icons and centered names; matching horizontal/vertical border gaps. A right-side icon strip shows Dock contents. Clear Dock and app-named added feedback are included.
- Outlined, remappable Previous/Next page badges; Dock app pages use the same **360 ms, after-layout animation** as setup protection pages. First/later pages reset old transforms and ignore stale animation starts.
- Main controller hints move to the top-right beside Settings, including outlined Select/Share for task options. Refresh is redrawn; Settings background/focus is circular. Button text is smaller while face-button badges are clearer. Semantic RAM-chip, redo, swap, Record, search, unlock, D-pad, info and Dock-style vectors replace misleading icons. PlayStation, Google Play, GitHub and Shizuku use their own marks; B/PlayStation legend symbols are white.
- The app used before TaskDeck gets a separate **green Current badge above its icon/name**, plus a green card outline and green Dock treatment. It does not push the app name sideways.
- Fit all changes animate when Android animations are enabled. Task space shrinks when the Dock grows. Fit all and Recent remain defaults for unset preferences; explicit choices are retained. Redundant Dock/selection/task-count labels and successful-close confirmation banners are removed.
- Matching System RAM and App RAM background cards, one shared physical-RAM scale, responsive sidebar entries and consistent 8dp main border spacing. More padding around RAM details and toolbar controls.

### Setup, appearance and settings

- Setup is rebuilt as **five unnested steps:** Permissions, Controller, Shortcuts, Appearance, App protection. Normal launches go straight to the deck; About > Redo Setup reruns setup without clearing settings.
- Permissions come before recording. Live checks distinguish Required Shizuku access from Optional shortcut/notification access and explain why each is needed. Saved settings alone no longer produce misleading permission checkmarks. Official Shizuku guide, Play Store and GitHub downloads are linked in setup and Connection.
- Inline controller labels and Confirm calibration; Record icons restored. Deck shortcut and **Quick Swap Apps** (renamed from Direct switch) record/save on release and show calibrated checkmarks. Recording is unavailable until Android grants shortcut access, and reserved navigation buttons keep working.
- Optional app protection is explained and searchable. Ordinary apps and launchers are **not locked by default**; existing explicit locks and necessary core protection remain. Protection pages support touch swipes, D-pad edge navigation and L1/R1. The final footer explicitly labels Previous/Next page; B returns and a text Start/Options badge finishes.
- Completion check draws more slowly (800 ms) while total completion time is about 950 ms; Confirm can continue. Earlier excessive completion delay was removed.
- Light, Dark, OLED and **Automatic** themes. Sixteen inline accent swatches use the available space, accessible names, selected background boxes and a stronger accent-colored controller focus. No extra color submenu or reset control. Appearance/About headings restored.
- Stay open after clearing and **Vibrate on close** use consistent On/Off controls; optional short close vibration adds VIBRATE permission. Closing behavior stays in Appearance. Theme and vibration controls have more padding.
- Equal content-measured button widths per menu pane, enough padding/wrapping for long labels, equal vertical Dock-style choices with left-aligned text, and vertically stacked About actions. No Android-style popup is used for Dock styles. Settings Last action is removed; errors remain inline. Developer/version, GitHub and Ko-fi stay in About.

### Controller and reliability fixes

- Touch acts on the first tap; controller focus is separate. Confirm enters Settings content; Back returns to categories before closing Settings. Horizontal choices/panes, setup themes-to-accents and both appearance toggles are reachable with D-pad/stick.
- Tasks Up goes to Refresh, then across the toolbar. Tasks Right reaches RAM details; RAM Up reaches Settings; RAM Down/Right reaches the right Thumb corner. Tasks Down always starts at the **leftmost Dock item**, even after using the right corner. Up from the right corner returns to RAM details.
- Separate Previous/Next page mappings cover Dock selection, RAM and window pages, with actual mapped labels. Confirm/Back remain reserved. Size steppers and Dock background/shape controls have explicit controller routes.
- Empty-deck entry retains a usable tool focus. Focus magnification scales centered icons inside reserved space instead of clipping bubbles, screen edges or sparse-corner backgrounds. Picker icon elevation, disappearing icons and bottom-aligned names are corrected.
- Setup hotkeys now persist on release; required access is checked before capture. Shizuku connection status uses a live green checkmark.
- **Close all during task refresh is queued once** and runs against the refreshed list instead of losing the input. Bulk clearing honors the stay-open/exit preference for partial results, so another press is not needed just to leave. Failed operations remain reported and are not destructively retried.


### Build and distribution

- VersionCode 200, UserService 9; existing signing key/package retained for updates over 1.0 and signed 1.1 test builds.
- 159 automated tests passed, release lint 0 errors/33 warnings, signed APK/AAB builds passed. Hardware acceptance remains open as documented.
- English documentation, aggregate release notes, updated brand licenses/notices, clean source ZIP and SHA-256 checksums.

## 1.1.12

- Reuse the setup protection picker transition for Add apps to Dock: animate the measured grid in place for 360 ms without rebuilding the page/search.
- Increase Dock size-control padding and height to keep the icon inside its frame.
- Remove repeated plus/minus characters from preview Apps labels; retain icons and distinct accessible descriptions.
- Increase theme-choice and Vibrate on close padding in Appearance.

## 1.1.11

- Restore Record icons for calibration, recording and Configure shortcuts; use PlayStation/Google Play/GitHub marks for their targets.
- Redraw redo arrow and add memory-chip RAM/Swap sides icons.
- Start page motion after layout with a slower 360 ms transition and stale-start protection.
- Compact final footer with labeled page hints and text Start/Options badge.
- Add side padding and left-aligned Dock style texts.

## 1.1.10

- Fix setup themes Down navigation to both full-size appearance actions; unify close vibration as an On/Off toggle.
- Protection pages horizontally by D-pad edge, touch swipe or L1/R1, with edge hints. B returns and Start finishes; enlarge footer frames and fit narrow displays.
- Replace generic gears with semantic search/unlock/D-pad/info/redo/Dock/brush/behavior/shortcut icons; omit duplicate theme icons.
- Include official GitHub/Shizuku brand assets, retaining upstream notices/license. Connected status keeps its green tick beside the Shizuku mark.
- Queue one explicit Close All behind task refresh and honor bulk exit preference for partial results; report failures without destructive retries.

## 1.1.9

- Center picker icons/names using measured text layout and equalize border gaps in both pickers.
- Replace visibility slider with Always show Dock checkbox; stack About actions.
- Add Shizuku Play/GitHub downloads and adjacent Required/Optional permission reasons.
- Fix Setup Appearance cross-pane navigation and separate protection paging from the single setup L1/R1 pair.
- Three equal vertical styles: Thumb corners, Floating, Shelf; Compact migrates to Floating.
- Preview saved Dock order/sides first, then alphabetical extras without displacing saved sides.

## 1.1.8

- Five unnested setup pages, permissions before hotkeys, inline capture/save-on-release and calibrated checkmarks; Redo Setup added.
- Quick Swap Apps and Dock naming standardized; Shizuku explained and linked.
- Eight-app picker with corrected icon elevation/layering, search, right icon strip, Clear Dock and named added feedback.
- Equal-size style vectors, independent left/right preview controls and controller-friendly visibility slider.
- Select/Share task-options hint, white B/PlayStation glyphs, Fit all transitions and optional close vibration.
- Successful-close banner and Settings Last action removed.
- Ordinary launcher locks are opt-in; explicit locks/core protection preserved. UserService version 9.
- Completion draw slowed to 800 ms; total time reduced to roughly 950 ms.

## 1.1.7

- Replace white swatch rings with rounded boxes, selected accent-tinted fill and stronger accent-colored controller focus; tighten row/heading spacing and remove Reset.
- Restore Appearance and About category headings.
- Scale Thumb corner boxes with their icons down to 50%, rather than retaining the old fixed minimum box.
- Replace the dock-style popup with inline choices; dock theme follows the app.
- Add Swap sides for Thumb corners, preserving odd group sizes, with touch-drag guidance and matching navigation/preview.

## 1.1.6

- Reset Tasks → dock entry to the leftmost item, regardless of the previously visited right corner; retain RAM → right-corner routing.
- Expand accent selection to sixteen larger swatches in complete responsive rows, with distinct stored-selection/focus rings and theme reset.
- Measure menu labels and compound content with generous horizontal padding; allow wrapping/height instead of narrow caps, ellipsis or font shrinking.
- Clarify required/optional Setup steps, offer inline A/B/X/Y or PlayStation labels before calibration and use a two-by-two theme grid.
- Improve common recovery summaries and consolidate the current interaction guide; record the Nielsen heuristic and Setup audit.

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
