# TaskDeck 2.0

This release collects **all changes since the last public release, 1.0**. The intervening 1.1–1.1.12 builds were local test updates. TaskDeck remains free and open source, by **SilasDV**.

## New: the Dock

- Pin apps for quick launch independently of running tasks and protection. Select/long-press a task to pin it, or choose installed apps in Settings > Dock. Favorites never fabricate running task cards.
- Three styles: **Thumb corners** (default), **Floating** and **Shelf**. Thumb corners puts individual app bubbles at the bottom-left/right, with independent overflow scrolling. The former Compact style migrates to Floating.
- Touch hold-and-drag reorders shortcuts; controller task options offer move/remove. Swap sides exchanges Thumb corner groups while preserving odd group sizes and ordering. An empty Dock clearly offers Add apps to Dock.
- Configure always-visible versus reveal-on-navigation behavior, focus magnification and running indicators. The former Android-style visibility popup/slider is now an explicit Always show Dock checkbox.
- **Size and Icon size are separate**, each 50–160%. Changing box size does not change the stored icon scale. Large logos are limited to fit their box and magnified focus state. Existing local Dock sizes initialize the icon scale once to retain their appearance.
- **Background color:** follow the app theme, black, graphite, white, blue, purple, green or red. **Icon shape:** Round or Square, including adaptive icon layers. These controls live in Dock appearance, reachable from Dock style & size.
- The full-width preview occupies the actual bottom Dock position and uses real, distinct installed apps. Saved favorites appear first in their actual order/sides; alphabetical extras follow. Preview changes never change saved favorites. Thumb corners has left/right add/remove sample controls; Floating and Shelf have only one pair. Plus/minus appears once as an icon.

## App picker and task deck

- Searchable, alphabetical **eight-app grid**, two rows of four, without scrolling inside a page. Large, bounded icons and centered names; matching horizontal/vertical border gaps. A right-side icon strip shows Dock contents. Clear Dock and app-named added feedback are included.
- Outlined, remappable Previous/Next page badges; Dock app pages use the same **360 ms, after-layout animation** as setup protection pages. First/later pages reset old transforms and ignore stale animation starts.
- Main controller hints move to the top-right beside Settings, including outlined Select/Share for task options. Refresh is redrawn; Settings background/focus is circular. Button text is smaller while face-button badges are clearer. Semantic RAM-chip, redo, swap, Record, search, unlock, D-pad, info and Dock-style vectors replace misleading icons. PlayStation, Google Play, GitHub and Shizuku use their own marks; B/PlayStation legend symbols are white.
- The app used before TaskDeck gets a separate **green Current badge above its icon/name**, plus a green card outline and green Dock treatment. It does not push the app name sideways.
- Fit all changes animate when Android animations are enabled. Task space shrinks when the Dock grows. Fit all and Recent remain defaults for unset preferences; explicit choices are retained. Redundant Dock/selection/task-count labels and successful-close confirmation banners are removed.
- Matching System RAM and App RAM background cards, one shared physical-RAM scale, responsive sidebar entries and consistent 8dp main border spacing. More padding around RAM details and toolbar controls.

## Setup, appearance and settings

- Setup is rebuilt as **five unnested steps:** Permissions, Controller, Shortcuts, Appearance, App protection. Normal launches go straight to the deck; About > Redo Setup reruns setup without clearing settings.
- Permissions come before recording. Live checks distinguish Required Shizuku access from Optional shortcut/notification access and explain why each is needed. Saved settings alone no longer produce misleading permission checkmarks. Official Shizuku guide, Play Store and GitHub downloads are linked in setup and Connection.
- Inline controller labels and Confirm calibration; Record icons restored. Deck shortcut and **Quick Swap Apps** (renamed from Direct switch) record/save on release and show calibrated checkmarks. Recording is unavailable until Android grants shortcut access, and reserved navigation buttons keep working.
- Optional app protection is explained and searchable. Ordinary apps and launchers are **not locked by default**; existing explicit locks and necessary core protection remain. Protection pages support touch swipes, D-pad edge navigation and L1/R1. The final footer explicitly labels Previous/Next page; B returns and a text Start/Options badge finishes.
- Completion check draws more slowly (800 ms) while total completion time is about 950 ms; Confirm can continue. Earlier excessive completion delay was removed.
- Light, Dark, OLED and **Automatic** themes. Sixteen inline accent swatches use the available space, accessible names, selected background boxes and a stronger accent-colored controller focus. No extra color submenu or reset control. Appearance/About headings restored.
- Stay open after clearing and **Vibrate on close** use consistent On/Off controls; optional short close vibration adds VIBRATE permission. Closing behavior stays in Appearance. Theme and vibration controls have more padding.
- Equal content-measured button widths per menu pane, enough padding/wrapping for long labels, equal vertical Dock-style choices with left-aligned text, and vertically stacked About actions. No Android-style popup is used for Dock styles. Settings Last action is removed; errors remain inline. Developer/version, GitHub and Ko-fi stay in About.

## Controller and reliability fixes

- Touch acts on the first tap; controller focus is separate. Confirm enters Settings content; Back returns to categories before closing Settings. Horizontal choices/panes, setup themes-to-accents and both appearance toggles are reachable with D-pad/stick.
- Tasks Up goes to Refresh, then across the toolbar. Tasks Right reaches RAM details; RAM Up reaches Settings; RAM Down/Right reaches the right Thumb corner. Tasks Down always starts at the **leftmost Dock item**, even after using the right corner. Up from the right corner returns to RAM details.
- Separate Previous/Next page mappings cover Dock selection, RAM and window pages, with actual mapped labels. Confirm/Back remain reserved. Size steppers and Dock background/shape controls have explicit controller routes.
- Empty-deck entry retains a usable tool focus. Focus magnification scales centered icons inside reserved space instead of clipping bubbles, screen edges or sparse-corner backgrounds. Picker icon elevation, disappearing icons and bottom-aligned names are corrected.
- Setup hotkeys now persist on release; required access is checked before capture. Shizuku connection status uses a live green checkmark.
- **Close all during task refresh is queued once** and runs against the refreshed list instead of losing the input. Bulk clearing honors the stay-open/exit preference for partial results, so another press is not needed just to leave. Failed operations remain reported and are not destructively retried.

## Install / update

Download **TaskDeck-2.0.apk**. Android 10+ and Shizuku 13+ remain required. Start Shizuku and authorize TaskDeck; non-root Shizuku must normally be restarted after reboot. Shortcut accessibility access and notifications are optional unless you use those features.

Install over **1.0 or the signed 1.1 test builds**: the release key/package are unchanged and settings remain. Earlier 0.x debug-signed prototypes require uninstalling first, which clears their settings. UserService version 9 reloads the revised backend. APK is for direct installation; AAB is for distribution tooling. Source ZIP and SHA-256 checksums are attached.

## Validation and known limits

**158 automated tests passed**, with zero failures/errors/skips. Release lint completed with **0 errors and 33 warnings**. Signed APK and App Bundle builds passed; APK signing identity and final-commit bundle metadata are checked before publishing. Automated checks use fake IPC and simulated layouts; they do not certify physical hardware.

Physical RP6 animation/drag behavior, controller firmware routing, Shizuku task reporting and visible RAM banners still need device acceptance. Current identifies the previous app reported by Android. Core processes stay protected; removing a task does not necessarily stop background services, and Android may reopen HOME. RAM figures are decimal MB and estimates, not a performance promise. Feedback is owned by the Application and can outlive Activity exit, but not force-stopping the whole process. TaskDeck does not replace Android's system swipe-up gesture or obtain live task screenshots.

No INTERNET permission, analytics or text/screen-content logging. MIT licensed; **vibecoded** with substantial AI assistance through Codex, guided by **SilasDV**. Optional support: [Ko-fi](https://ko-fi.com/silasdv). Brand-asset licenses/notices are included.

[All commits since 1.0](https://github.com/silasdv/TaskDeck/compare/v1.0...v2.0)
