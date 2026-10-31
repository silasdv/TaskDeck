# TaskDeck

**A free, controller-first Android task manager for gaming handhelds, with full touch support.**

Android's Recent Apps interface can be awkward or unavailable to navigate with a gamepad, depending on the device and launcher. TaskDeck by **SilasDV** gives you a controller-friendly deck for opening, closing and protecting apps, plus a favorites Dock and approximate RAM information.

**Version 3.0 · Android 10+ · Shizuku required · Root optional**

[Download APK](https://github.com/silasdv/TaskDeck/releases/download/v3.0/TaskDeck-3.0.apk) · [Release 3.0: every change since 2.0](docs/RELEASE-3.0.md) · [Changelog](CHANGELOG.md)

## Install or update

1. Download `TaskDeck-3.0.apk` from the [official release](https://github.com/silasdv/TaskDeck/releases/tag/v3.0) and install it. Signed 1.0/2.0 and local 2.x installations update with settings retained. Old 0.x debug builds use a different key and require uninstalling first, which clears app settings.
2. Install/start [Shizuku](https://shizuku.rikka.app/download/) using its [official guide](https://shizuku.rikka.app/guide/setup/), then authorize TaskDeck. Pairing alone does not start the service. Without root, start Shizuku again after reboot. Android 11+ supports wireless-debugging setup; Android 10 needs Shizuku's documented computer setup.
3. Follow the five setup steps: Permissions, Controller, Shortcuts, Appearance, App protection. Shizuku access is required for task operations; shortcut-service access is optional but required for hotkeys; notifications are optional. L1/R1 navigate setup, with page navigation distinct on the protection page.
4. Choose A/B/X/Y or PlayStation labels and calibrate Confirm if Android reports a swapped layout. Record optional multi-button Deck/Quick Swap Apps shortcuts. Pick appearance/protection choices; ordinary apps and launchers are not locked by default. Necessary system protection remains.
5. Try opening/closing noncritical apps first. Closing does not save unsaved game state. Redo Setup in About retains configuration.

## Controls

| Default input | Action |
| --- | --- |
| D-pad / left stick | Move selection |
| A / Cross | Open selected app |
| B / Circle | Exit or go Back |
| X / Square | Close selected app's reported windows |
| Y / Triangle | Close all eligible apps |
| R1 | Close all but current |
| Select / Share | App options: protection, Dock pinning, windows and launcher restart where available |
| Start / Options | Settings |
| L1 / R1 on paged menus | Previous / Next page |

Main/page actions can be remapped independently. Hints and Back badges follow actual mappings and the chosen printed-label profile. Start stays reserved for Settings. Android firmware can remap keys before TaskDeck receives them; calibration/manual correction is available. Touch acts on the first tap and clears controller focus styling.

## Task deck and RAM

- Fit all is the default, alongside Grid, List and Cards presets. Recent is the default sort; RAM size and Alphabetical are available. Protected apps sort last. Current marks the app used before TaskDeck opened.
- Group apps combines reported windows; disabling it gives separate task cards. Close all but current retains the origin app. Stay open after clearing controls whether bulk actions leave TaskDeck; empty actions still honor the preference.
- User protection and launcher restart are explicit choices. Mandatory keyboard/core services are excluded from closable cards; RAM information explains additional system/service usage. Installed launchers are not fabricated as open tasks.
- System RAM and approximate app RAM use MB and a common physical-RAM scale. App PSS does not sum to system usage. Sample age/loading/unavailable/stale states are visible. RAM details has an independent Refresh RAM action that retains task selection.
- Closing is immediate. Known transient errors may get one bounded retry after 50 ms and fresh identity/protection checks; uncertain transport, protection/permission failures and ambiguous launcher outcomes are not blindly replayed. Failed/unverified outcomes identify apps.

## Favorites Dock

- Pin via Select or touch long-press; add installed launchable apps from Settings > Dock. Favorites are shortcuts, independent of running tasks and protection.
- **Thumb Corners: up to 6 apps. Floating/Shelf: up to 8.** Existing over-limit saved lists remain stored; only permitted first apps render. The picker exposes extras and offers Trim to limit.
- Remove/clear/trim offers **Undo Dock change** for ten seconds, restoring order/layout/sides. Undo is controller-accessible and survives Activity recreation within its window. Touch-hold/drag reorders; app options also provide Move left/right and Remove.
- The app picker has eight alphabetical icon/name tiles per page, search/Clear search, mapped paging, count/limit and a readable favorites strip. Empty searches have a valid controller recovery target.
- **Dock / Background:** layout, Swap sides, Dock Size 50–160%, ten background choices including None, and independent Rounded/Round/Square/Squircle background shape. None hides the shape control without losing its saved value.
- **Icons:** Icon Size 50–160% within the fixed box, Round/Squared/Squircle masks and Normal/Black and White/Accent Color/Glass looks. Adaptive layers are masked before filtering; legacy pre-rounded raster corners cannot be reconstructed. Glass is a stylized gradient, not native blur.
- Both menus show an actual-size bottom preview using real apps/order/sides. Sample-count changes never alter favorites. Selection and controller focus are distinct. Always show Dock, focus magnification and running indicators have matching controls with contextual help.

## Appearance, setup and backups

Light, Dark, OLED and Automatic themes; sixteen accent colors; main-task icon styles; grouping, stay-open behavior and optional close vibration. Larger system fonts reflow captions/choices; above 125%, affected content may scroll to keep controls readable. Reduced motion skips the setup completion delay. Back returns to the originating Settings control and then category navigation.

**Settings > About > Settings backup** exports/imports local JSON through Android's document picker. Validate and page through the preview before Apply import. Omitted customization resets to defaults; Android grants and setup completion stay on the device. Cancellation discards pending reads. Unknown/invalid settings, conflicts, oversized files and above-limit Dock lists are rejected. Trim an old oversized Dock before export. No network/storage permission is added.

## Compatibility and validation

Built for Retroid Pocket 6 and other Android handhelds in landscape or portrait. Firmware/Shizuku restrictions vary. Removing a recent task is not a universal force-stop: Android can keep/restart services or its HOME launcher. Clearing tasks does not guarantee more game performance. Android controls notification banners.

Automated gates cover simulated input, state, layout, backend boundaries and document streams. They do not certify physical controller mapping, Bluetooth/USB hotplug, long sessions, native document providers, haptics or banners. See [validation](VALIDATION.md) and [device test matrix](DEVICE-TEST.md). Current release screenshots should come from a real 3.0 installation; no simulated image is presented as device evidence.

## Build and contribute

Use JDK 17, Android SDK 35 and the included Gradle wrapper (8.11.1):

```sh
./gradlew testDebugUnitTest lintRelease assembleRelease bundleRelease
```

Release signing requires your own private key/configuration; see [release instructions](docs/RELEASING.md). Never commit signing material. [Contributing](CONTRIBUTING.md) · [Privacy](PRIVACY.md) · [Third-party notices](THIRD_PARTY_NOTICES.md) · [MIT license](LICENSE).

## Vibecoded and support

TaskDeck is **vibecoded**: substantial AI-assisted implementation/documentation through OpenAI Codex, guided and iteratively tested by **SilasDV**. Human review and contributions are welcome. No ads, analytics, usage-history logging or INTERNET permission.

Free and open source. Optional tips: **[ko-fi.com/silasdv](https://ko-fi.com/silasdv)**. The app opens the external browser for project/help/support links.
