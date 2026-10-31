# TaskDeck 3.0

**All changes since the last public release, 2.0.** Local milestones 2.1–2.7 are included here. This describes the final behavior; intermediate designs remain documented as history.

[Download APK](https://github.com/silasdv/TaskDeck/releases/download/v3.0/TaskDeck-3.0.apk) · [Installation guide](https://github.com/silasdv/TaskDeck)

## Dock customization

- Separate **Dock / Background** and **Icons** menus replace the combined form. Changes stay on their menu; Back restores the originating Settings entry.
- Bounded, top-aligned panels use two columns in landscape and stack in portrait. Captioned visual choices replace stretched cards and layout popups.
- Independent **Dock Size** and **Icon Size**, both 50–160%, use matching centered minus / percentage / plus steppers. Icon scaling fills a fixed box instead of enlarging the background or Dock height. Saved sizes initialize icon scale once on upgrade.
- Background colors: **None, Follow theme, Black, Graphite, White, Blue, Purple, Green, Red and Amber**. None removes persistent fills/outlines, hides Background Shape and preserves its stored choice without affecting icon sizing. Navigation can still show a focus outline.
- Independent background shapes: **Rounded, Round, Square, Squircle**. Icon masks: **Round, Squared, Squircle**. Focus backgrounds and logo masks no longer control each other.
- Background previews show neutral forms; icon previews show masked app artwork without an extra plate. Layout thumbnails remain independent of Dock color/scale and use theme-appropriate contrast.
- Saved selection uses accent fill plus a tick; controller focus has a separate consistent outline. None has a slash; Follow theme has a split symbol. Focused color names appear separately from the stable Background Color heading. Graphite is distinct from black; edges are thin and conditional on contrast.
- Artwork is centered in its boxes. Focus magnification and running/current dots reserve space to avoid crop/overlap at masks, bubbles, sparse corners and screen edges. Actual Current status remains green.
- Dock behavior has matching On/Off controls for **Always show Dock, Magnify focused icons, Running indicators**, with explanations for the focused/hovered setting.
- The actual-size bottom **Live preview** uses distinct real apps, saved order/sides first and alphabetical extras next. Neutral samples reveal chosen colors. Count changes never pin/unpin apps. Thumb Corners has independent left/right count pairs; Floating/Shelf have one pair.
- Preview buttons have drawing clearance and no native lift. Unavailable range/count actions are dimmed; focus moves to a usable sibling at a size limit. Preview order also matches capped legacy lists.

## Favorites and app picker

- Limits are **6 apps for Thumb Corners**, **8 for Floating/Shelf**, including preview samples.
- Existing oversized lists are preserved: only the permitted first apps render; the picker exposes saved extras, count/limit and **Trim to limit**. No silent deletion.
- Remove, Clear Dock and Trim offer **10-second Undo Dock change**, restoring exact order, layout and sides. Undo works with gamepad, survives recreation during its window, and returns focus safely on expiry.
- The favorites rail uses readable fixed-height icon rows with bounded overflow instead of shrinking every app into a tiny strip.
- Initial catalogue results fade in over 220 ms. Existing 360 ms paging starts after layout; canceled/stale fades cannot replace later pages/searches.
- **Clear search** and an explicit D-pad target recover zero-result searches. Clearing returns focus to results. Full-Dock attempts explain the limit while favorites remain removable.

## Appearance and accessibility

- **Normal, Black and White, Accent Color, Glass** styles apply to Dock and main task icons. Glass is a stylized monochrome gradient, not Android blur or an Apple rendering API.
- Remove the redundant Follow accent checkbox; use Accent Color directly. Legacy overrides migrate to the explicit style when applicable.
- Adaptive layers are masked before look filters, fixing round system masks surviving square/squircle choices. Pre-rounded raster artwork cannot reconstruct missing corners.
- Appearance has consistent title placement: themes in one row, Group apps/Stay open after clearing/Vibrate on close in the next where space permits. Long labels wrap with sufficient padding and equal pane button widths.
- Accent Colors retain responsive complete rows. App icon style follows the actual palette with a consistent gap; D-pad moves from palette into styles.
- All icon-look cards reserve two caption lines and adequate space, so Normal/Glass and Black and White/Accent Color show equal full-size artwork.
- Drawn captions follow system font scale; bounds/column counts adapt. Above 125%, affected content reflows/scrolls with navigation outside the content. Normal-size forms stay compact.
- Reduced motion skips setup completion animation and fixed delay. With animations enabled, completion remains a short drawn check and Confirm can continue.

## Controller and navigation fixes

- **Start opens Settings** rather than exiting. It is reserved; previous Start mappings migrate to available buttons. Start/Select have matching outlined badge proportions, Start shares a background with Settings, and a divider separates tools from the legend.
- Every **Back** action uses the mapped controller badge plus Back text, including forms, Settings/setup submenus and app selection. Step/page actions keep their distinct labels.
- Back restores the originating Settings content button; another Back returns to categories before leaving Settings.
- Spatial D-pad/stick navigation covers Dock steppers, Swap sides, colors and shapes. Immediate focus restoration prevents rapid Confirm from accidentally activating Back during a rebuild.
- Task-card Close hints use actual mappings and vector PlayStation/swapped labels instead of hard-coded X. Protected badges align above icons like Current; Current plus protected share a badge.
- Settings recording is disabled without shortcut access, with an **Enable shortcut access** action. Missing access and backend disconnection show recovery feedback; Refresh can request reconnection.

## Closing and RAM

- A known transient close failure may receive **one retry after 50 ms**, after fresh task identity/protection checks. Already-absent ordinary tasks can be confirmed without another command.
- Permission/protection failures, uncertain transport, expired/interrupted work and ambiguous launcher results are not replayed. Batch deadlines remain bounded; persistent failures are reported after the permitted retry.
- Failed/unverified closes name affected apps. Remaining task descriptions expose Retry close with the mapped button. Successful-close overlays and the removed Last action menu stay out of the main flow.
- Per-app RAM shows sample age, loading/unavailable states and stale state after 30 seconds. System totals remain live; app estimates are not presented as summing to system usage. RAM values remain MB.
- **Refresh RAM** updates memory independently, without re-reading tasks or changing selected task identity.

## Local settings backup

- **Settings > About > Settings backup** exports/imports local JSON through Android's document picker. No new network/storage permission.
- Validate schema version, a strict settings allow-list, types/ranges, packages, mapping/chord conflicts, layout limits and a 64 KB maximum before applying anything.
- A paged preview shows app names/order and printed controller labels, using mapped Previous/Next controls. **Apply import** is explicit; omitted customization returns to defaults.
- Android grants and setup completion are not imported. Back/cancel discards pending data, including reads completing after cancellation. Trim an old oversized Dock before export.

## About, documentation and packaging

- About shows actual Ko-fi/GitHub destinations. Ko-fi uses its official cup with bundled attribution. Author **SilasDV**, vibecoded disclosure and optional support remain.
- Reuse grid placement/layout parameters to reduce measurement allocations. Release includes signed APK/AAB, tracked source ZIP and SHA-256 sums, with the same signing identity as 1.0/2.0.
- Public installation, interaction, privacy and validation documentation reflects the consolidated release; historical milestone notes remain available.

## Install/update and known limits

Android 10+ and running, authorized **Shizuku** are required; root is optional. Download `TaskDeck-3.0.apk`. Update over signed 1.0/2.0 or local 2.x builds to retain settings. Earlier 0.x debug builds used a different key and require uninstalling, which clears data.

Pairing alone does not start Shizuku. Without root it needs starting after reboot; follow the [official guide](https://shizuku.rikka.app/guide/setup/). Hotkeys need optional shortcut-service access; notifications are optional. No advertising, analytics or network permission.

See [validation results](https://github.com/silasdv/TaskDeck/blob/v3.0/VALIDATION.md) and [physical test matrix](https://github.com/silasdv/TaskDeck/blob/v3.0/DEVICE-TEST.md). Simulations cannot certify physical mapping, firmware, Bluetooth/USB hotplug, sustained load, native document providers, banners or haptics. Android may restart HOME/services; closing recent tasks is not a universal force-stop or guaranteed performance gain. RAM estimates remain approximate. Use disposable state for closing tests.

[Compare 2.0 to 3.0](https://github.com/silasdv/TaskDeck/compare/v2.0...v3.0) · [Milestone history](https://github.com/silasdv/TaskDeck/tree/v3.0/docs)

Free/open source under MIT. Vibecoded with substantial AI assistance, guided by SilasDV. Optional support: [ko-fi.com/silasdv](https://ko-fi.com/silasdv).
