# TaskDeck

**A controller-first Android task manager for gaming handhelds, with full touch support.**

TaskDeck by **SilasDV** lets you open, close and protect real Android tasks without wrestling with touch-only Overview controls. Version **2.7** (local update), Android 10+, using Shizuku.

[Full 2.0 release notes: every change since 1.0](docs/RELEASE-2.0.md) · [Changelog](CHANGELOG.md)

## Vibecoded

This project is **vibecoded**: implementation and documentation were created with substantial AI assistance through OpenAI Codex, guided and iteratively tested by SilasDV. Automated tests cover selected behavior; they do not establish compatibility with every firmware/controller. Human review and contributions are welcome.

## Everyday use

One main screen combines app selection, task closing and RAM information. It starts on the first app, including a protected launcher. Refresh is selected only when no cards exist.

| Default input | Action |
| --- | --- |
| D-pad / left stick | Move selection |
| A / Cross | Open selected app |
| B / Circle | Exit menu |
| X / Square | Close selected app's reported windows |
| Y / Triangle | Close all eligible apps |
| R1 | Close all but current: retain the app used before opening TaskDeck |
| Select / Share | Protection, Dock pinning, window and launcher-restart options |

Fit all animates layout changes when Android animations are enabled. The legend also includes the mapped Select/Share task-options badge. B and PlayStation glyphs are white.

All six main actions can be remapped in Settings > Controller > Button mapping. **Page navigation** adds separate Previous/Next mappings (L1/R1 by default) for app selection, RAM details and window pages; their hints show actual bindings in button outlines. Confirm/Back remain reserved when assigning page navigation. The top-right action hints, immediately before Settings, follow Open–Exit–Close–Close all–Close all but current–Task options and display the actual mapped button labels.

- **Fit all** is the default. View offers exactly four presets: Fit all, Grid, List and Cards. There is no custom layout editor. Explicit preset choices are preserved. Fit all scrolls if the minimum readable size cannot fit the full set.
- Sort offers Recent, RAM size and Alphabetical in an outlined popup. Locked apps sort last, keeping eligible apps first.
- App windows group into one card by default, with a count and one package-wide RAM sample. Disable Group app windows in Settings > Appearance for individual task cards. A grouped X targets all represented windows; separate cards target one window.
- Compact toolbar: Refresh, Sort, View and an icon-only Settings control. No brand heading, mode tabs, Connect shortcut or Diagnostics screen uses deck space. Main-screen buttons use compact 36dp controls with 20dp face-button badges and unchanged text/background dimensions; Settings keeps larger controls.

Closing is immediate, with no confirmation dialog. Use disposable app state for testing; unsaved game progress is not saved automatically.

## Installation and first launch

Android 10+ and **Shizuku 13+** are required. Root is not required. Android 11+ can start Shizuku with wireless debugging; Android 10 needs its documented computer setup. Firmware permissions vary.

1. Download **TaskDeck-2.0.apk** from [Releases](https://github.com/silasdv/TaskDeck/releases/tag/v2.0) and install it. **Earlier 0.x test builds used a different key: uninstall one first, after noting your settings/mappings. Uninstalling clears app data.** Version 2.0 uses the same dedicated release key as 1.0 and installs over it, retaining settings.
2. Install Shizuku from its [official download page](https://shizuku.rikka.app/download/) and follow the [official setup guide](https://shizuku.rikka.app/guide/setup/). Pairing is not the same as starting Shizuku. Without root, start it again after reboot.
3. Setup has five unnested pages: Permissions, Controller, Shortcuts, Appearance and App protection. Shizuku connection is required. Shortcut-service access is required to record hotkeys; notifications are optional. L1/R1 navigate.
4. Controller calibration and both shortcut recordings stay inline. Hold and release a chord to save; completed deck and Quick Swap Apps recordings show checkmarks. Appearance includes accent selection and optional close vibration. Protection lets users choose locks. Completion draws for 800 ms and opens the deck after roughly 950 ms total. Settings > About > Redo Setup preserves configuration.
5. Test switching and closing with two noncritical apps before using a game with unsaved state.

## Settings

Settings has Connection, Controller, Appearance, Dock, Notifications and About categories, with an adjacent content pane in landscape. Subpages return to Settings after setup. Connection/reconnect tools live in Connection; there is no permanent Connect button on the deck.

**Stay open after clearing** is a stable-label toggle. Highlighted **On** keeps the deck open after bulk clearing. Outlined **Off** returns HOME for Close all, or to the retained app for Close all but current. The exit preference applies to partial results; errors are reported without retries. Empty bulk actions still follow the chosen exit behavior.

Closing behavior lives in Appearance, beside grouping, themes and inline **Accent Colors**. Sixteen larger color dots fill complete, responsive rows without visible names. Each swatch has a rounded background box; selection fills/highlights that box, and controller focus uses a stronger border in the current accent color. Rows stay close together near the heading. Accessible names and tooltips identify colors. Choices include Red, Green, Blue, White, Purple, Cyan, Violet, Pink, Orange, Amber, Teal, Lime, Yellow, Indigo, Coral and Sky. There is no color-reset control; choose another swatch directly. Current/status colors retain their meaning. A green checkmark on Connection indicates an active backend. About shows version/developer information, GitHub and an optional Ko-fi link. RAM feedback and the notification shortcut use the same On/Off convention. Vibrate on close uses the same full-size On/Off control as Stay open after clearing. Notifications also contains Test feedback and Android permission settings. Android controls actual heads-up/banner visibility. Last action is removed from Settings; results remain transient in memory. Successful closes have no confirmed banner.

Controller navigation: Up from the top task row selects Refresh; Left/Right moves through the toolbar. Right from the task edge reaches RAM details; Up there reaches Settings; Right or Down enters the right dock group, whose Up returns to RAM details. In Settings, Confirm enters a category’s content; Back returns to categories before a second Back exits.

Menu buttons share a content-measured width within each pane, with horizontal padding and wrapping/height when needed; text is not shrunk or ellipsized to fit a narrow cap. Theme buttons have additional horizontal padding. Previous/Next page controls align to opposite edges.

The full interaction plan is in [USER-FLOW.md](USER-FLOW.md).

## Dock

Pin launchable apps with Select or touch long-press on a task card. Add installed apps in Settings > Dock even when they are not open. Favorites are shortcuts, separate from task cards and protection: pinning does not lock an app or fabricate an open task.

**Thumb corners is the default.** Choose Thumb corners, Floating or Shelf using equal-size, left-aligned vertical choices. A full-width bottom preview uses real installed apps: saved Dock apps in their order/sides first, then alphabetical extras. Preview edits never change saved favorites. Thumb corners has independent left/right add/remove sample controls; Floating and Shelf have one pair. Count buttons use one plus/minus icon, without duplicate signs in the text.

Dock **Dock Size** and **Icon Size** are independent **50–160%** settings. Under Settings > Dock, **Dock / Background** contains layout, Swap sides, Dock Size, background color (None, follow theme, black, graphite, white, blue, purple, green, red, amber) and independent Background Shape. **Icons** contains Icon Size, Round/Squared/Squircle masks and Normal, Black and White, Accent Color and Glass looks. None hides Background Shape without losing the saved shape. There is no separate Follow Accent toggle: choose Accent Color in Icon Style. Appearance offers the same four looks for main task icons. Both menus show the actual-size neutral Dock preview at the bottom. Preview app counts never change saved favorites.

Thumb corners separates favorites into bottom-left/right groups with individual bubbles and independent overflow scrolling. **Swap sides** exchanges groups, including odd counts. Touch-hold and drag reorders apps. An empty Dock offers **+ Add apps to Dock**. Always show Dock is an On/Off button matching the other Dock behavior controls; off reveals it on navigation/tap. Old Compact choices migrate to Floating. Automatic app theme follows Android; the Dock follows it unless a background override is chosen.

The dock has no visible heading. Thumb corners uses a separate bordered box for each app, with 8dp spacing; Current uses the same green as task cards. Growing the dock reduces task space. Idle selection/app-count status text is omitted; actionable errors remain visible.

Dock behavior offers always visible or show on navigation/tap, focus/hover magnification and optional running indicators. Indicators use reported process data; an ordinary recent task alone does not prove an app is running. The app used before TaskDeck is marked **Current**, with a separate green outlined badge grouped just above its icon/name when Android reports that origin; selecting another card does not change it.

Touch-hold and drag to reorder; dropping outside cancels the reorder. Controller: Down from the last app row always enters the leftmost Dock item, including after visiting the right dock; RAM details Down/Right enters the right group. Left/Right selects, Confirm launches, Up returns. Select opens Open, Move left, Move right and Remove options. Removing a favorite never uninstalls or closes it. Installed-app selection shows eight alphabetical icon/name tiles in two rows of four, with search, Clear Dock and an icon-only Dock strip at the right. Adding shows an app-named toast. Native Button elevation is disabled so it cannot cover the icons. Icons shrink to fit short tiles rather than disappearing. Selected tiles indicate favorites. L1/R1 change pages; mapped Back or the visible Back button exits. Down from search returns to results.

RAM details is reachable by D-pad or left stick at the right edge of the app area and through its toolbar control in portrait. Setup checks live Shizuku, accessibility and notification access; a saved preference alone never proves permission was granted.

## Controller mapping

Settings > Controller > Button mapping follows PadBoard's convention: **A/B/X/Y**, **A/B/X/Y (swapped)**, **PlayStation symbols**, **PlayStation symbols (swapped)**. Letter names do not guess Nintendo/Xbox physical placement. PlayStation face hints are consistently sized vectors.

Calibrate confirm button asks for printed A or Cross to infer the common AB/XY pair swap. Calibration resets the four face actions while preserving the remaining actions through conflict swaps. Choose an individual action to assign any supported digital controller button; an occupied assignment swaps the previous action so both stay reachable. Reset mapping keeps your chosen label profile. Settings also follows mapped Confirm/Back, while D-pad, Android Back and setup's L1/R1 navigation remain available. Bindings are global, not per-device. Repeat calibration after changing Retroid's Android controller mode; it does not identify arbitrary permutations or the controller model automatically.

## Shortcuts and entry points

**Quick Settings:** add the TaskDeck tile in Android's tile editor. **Notification:** enable Notification shortcut in Settings > Notifications, with Android notification permission if required.

**Controller:** Settings > Controller > Configure shortcuts records two independent digital chords: one opens the deck, Quick Swap Apps switches to the previous distinct app without opening TaskDeck. Each is optional and independently enabled. Enable TaskDeck's Accessibility service to use them. It handles key events only; it does not retrieve screen contents or typed text.

Hold up to four desired buttons together and release. Setup saves automatically; the Settings recorder uses Start or Confirm after inspecting key names/scan codes. D-pad, Confirm, Back and Start remain reserved during recording. Equal or subset/superset conflicts between the two chords cannot be saved. Partial combinations can reach the current game before the completing key is intercepted; choose unused combinations. Held events do not repeatedly trigger. Different controllers/scan codes are kept separate by the chord recognizer.

If both rear buttons report identical key AND scan codes, assign different digital outputs in Retroid settings. Guide/Home and analog-only keys may be intercepted by firmware. Confirm calibration infers the common AB/XY swap, not arbitrary button permutations. Quick Swap Apps depends on firmware task ordering; HOME, split-screen and dock remain acceptance cases.

## RAM charts and feedback

All displayed memory uses **decimal MB: 1 MB = 1,000,000 bytes**. Android PSS input is KiB and is converted, not relabeled. Unknown samples are unavailable, never zero.

The deck uses consistent 8dp gaps between main borders. Matching rounded System RAM and App RAM cards separate system totals from per-app estimates. The System RAM card contains used/total memory and its system usage bar, without an available/free RAM counter. Short sidebars show only the estimates that fit inside the card; RAM details exposes the rest. Every app bar uses the same denominator: total physical RAM. App PSS is converted from KiB to this common scale, rather than making the largest process look like 100% of RAM. Visible sidebar entries adapt to the available height. **RAM details** exposes every reported entry through paginated charts, without a fixed three-entry cutoff. Values and text descriptions accompany colors; app PSS is not a decomposition of total system RAM and should not be added to match system usage. Android, services, shared pages and cache account for other usage.

Task listing completes independently of RAM sampling. A slow or unavailable sample does not disable opening/closing. Old samples are discarded after refresh or catalog changes. Ordinary task removal is not evidence that an app process stopped.

Feedback reports signed RAM change and used/total MB. It does not prove that a specific amount was reclaimed or that game performance improved. An Application-owned handler coalesces feedback; bulk exit publishes before leaving the Activity. It requests a toast and, when permitted, a quiet high-importance `ram_results` notification. This survives Activity exit, not Android killing/force-stopping the entire app. Channel blocks, notification permission, DND and gaming firmware control presentation. Test feedback does not pretend to close apps.

## Closing, protection and limits

Ordinary closing removes real recent/running tasks, similarly to Android Overview. It does not force-stop all app services. Required keyboards, TaskDeck, PadBoard, Shizuku and core services stay protected and are excluded from closable cards. Ordinary system apps such as Settings can be dismissed unless user-locked. Ordinary apps and launchers are not automatically locked; explicit user locks remain. Installed-but-unopened launchers never generate synthetic cards. Background-only apps are information, not task cards.

Launcher-only close requests a package force-stop and removal of original task records. Restart is an explicit one-off action retaining its saved lock. The backend rechecks package, current user and required-service protection. Launcher stop verifies original PIDs/tasks; restart checks a new nonempty PID set. Android may automatically reopen its default HOME launcher. Task removal is checked against a fresh list; unresolved/denied samples are reported as unverified. Missing protection discovery fails closed.

Requests have deadlines, late responses are ignored and a stalled connection gets one automatic reconnect attempt. Destructive actions are **never automatically replayed**: Android may already have accepted them before a timeout. Errors remain inline; operation outcomes are held transiently in memory. There is no arbitrary shell-command endpoint.

TaskDeck does not replace native swipe-up/Quickstep, obtain live task screenshots, perform emulator savestates or offer general-purpose package force-stop. It is not a RAM booster. Native gesture replacement is separate system integration; this app uses Shizuku and optional key/tile/notification entry points.

## Build

No INTERNET permission, ads, analytics, screen-content retrieval or typed-text logging. Task/PSS snapshots, results and feedback diagnostics remain in memory. Preferences persist explicit configuration such as locks, unlocked launchers, theme, view, favorites/dock preferences, shortcuts, calibration and closing/notification choices, controller bindings and label profile.

Build with JDK 17, Android SDK 35 / Build Tools 35.0.0 and Gradle 8.11.1:

```sh
./gradlew testDebugUnitTest lintRelease assembleRelease bundleRelease
```

Windows uses gradlew.bat. Set ANDROID_HOME or local.properties. See [Release signing](docs/RELEASING.md) for signing configuration. Keys/passwords live in ignored `.local/` and are never included in source archives. Without release signing material, the release artifact is unsigned. Application versionCode is 220; UserService implementation is 9.

See [VALIDATION.md](VALIDATION.md) for automated results and [DEVICE-TEST.md](DEVICE-TEST.md) for outstanding hardware checks. UI tests use fake IPC; actual RP6 Shizuku behavior, controller routing and visible RAM banners are not proven by them.

## Privacy and contributions

No network permission, advertising, analytics or usage-history logging. See [Privacy](PRIVACY.md), [Contributing](CONTRIBUTING.md), [MIT license](LICENSE) and [Third-party notices](THIRD_PARTY_NOTICES.md).

## Support SilasDV

TaskDeck is free and open source. Optional tips: **[ko-fi.com/silasdv](https://ko-fi.com/silasdv)**. The in-app link opens your browser.

UserService version 9 refreshes the revised launcher policy. VIBRATE supports opt-in short close feedback; INTERNET remains absent. App menus use TaskDeck-styled panels, while Android/Shizuku own permission interfaces.

Picker icon/name groups are centered using measured text layout; border gaps match across rows and columns. Setup offers direct Google Play and GitHub Shizuku downloads with adjacent Required/Optional reasons. Right from setup themes enters Accent Colors and Left returns. Protection uses separate app-page arrows, leaving only one setup L1/R1 pair. About actions are stacked vertically.

On the final protection page, swipe or move the D-pad past an edge to page horizontally. L1/R1 page apps, B returns, and Start finishes setup. Footer badges explicitly label page actions; Start/Options is written inside its badge. Setup Appearance Down reaches both full-size On/Off actions. Close All during a successful refresh runs once after the fresh list, rather than discarding the input. Semantic icons replace generic gears; theme icons are omitted. Official GitHub/Shizuku assets identify their links.

The final setup footer is compact and labels Previous page/Next page beside L1/R1. Start/Options is written in its badge. Page motion starts after layout over 360 ms, with canceled/stale transforms reset. Dock layout choices use centered thumbnails above their captions. Record actions use Record icons; RAM uses a chip, Swap sides opposing arrows, and layout/download controls their own brand marks.

Dock app pages use the same 360 ms, after-layout transition as setup protection pages. Preview count buttons show one plus/minus icon beside Apps; size and Appearance choices have larger internal padding.

[2.1 local changes](docs/CHANGES-2.1.md): inline Dock customization, mask-matched focus, 160% icons, Start Settings hint/reservation, restored submenu focus, protected badges and one identity-checked close retry. The previous public GitHub release remains 2.0. That 2.1 scrolling layout is superseded by the non-scrolling 2.2 form. Glass is a stylized icon highlight.

[2.2 local redesign](docs/CHANGES-2.2.md): non-scrolling Dock form, independent visual shape controls, None background, fixed-box logo fill, contextual Dock behavior help, visual App icon style below Accent Colors, matching Start/Settings badge and visible About links.

Local 2.3 reorganizes Dock style into **Dock / Background** and **Icons**. Background Shape is hidden under None without losing its stored value. See [local changes](docs/CHANGES-2.3.md).

Local 2.4 uses the available Dock settings space, equal-height groups, distinct focus/selection, matching percentage steppers and a neutral, explicitly labelled live preview. See [changes](docs/CHANGES-2.4.md).

Local 2.5 replaces the combined Dock style screen with separate **Dock / Background** and **Icons** menus. See [changes](docs/CHANGES-2.5.md).

Local 2.6 unifies mapped Back badges, protects preview controls and equalizes full-size icon-look artwork. [Changes](docs/CHANGES-2.6.md) · [App-wide UX review and proposed follow-ups](docs/UX-REVIEW-2.6.md).

## Local 2.7 improvements

Dock holds up to **6 apps in Thumb Corners**, or **8 in Floating/Shelf**. Above-limit saved lists are preserved: only the allowed first apps appear in Dock, while Add apps to Dock shows the saved count and offers Trim to limit. Removing/clearing/trimming favorites offers a controller-accessible **Undo Dock change** for ten seconds. Unselected apps cannot be added while full; selected favorites remain removable.

RAM details provide **Refresh RAM** and sample age. System totals are live; per-app samples show loading, unavailable or stale state and remain approximate. Memory refresh preserves selected task identity. Failed close feedback names the affected apps.

**Settings > About > Settings backup** exports a local JSON file through Android's document picker. Import first validates and previews all saved customization, with L1/R1 (or mapped page controls) for review. Apply is explicit; omitted customization resets to defaults. Android permissions and setup completion stay on this device. Back/cancel discards the pending import. Backups are limited to 64 KB and reject unknown keys, invalid mappings, conflicting shortcuts and excessive Dock lists. Trim an old over-limit Dock before exporting it. No network permission is used.

System font scaling applies to icon captions; above 125% affected content scrolls/reflows so navigation remains available. Reduced motion skips the setup completion delay. [Full changes](docs/CHANGES-2.7.md).
