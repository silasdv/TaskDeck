# TaskDeck 3.1 visual refresh

The main screen adopts the approved landscape mockup while retaining TaskDeck's configured themes, accent colors, controller glyphs and current/running states.

## Main screen

- Icon-only Refresh retains its accessibility name and tooltip.
- Sort and View show the active values; their controller-accessible menus open beneath the associated control.
- Controller hints retain touch actions, mappings, descriptive accessibility labels and focus treatment. Their idle surfaces are transparent. Open is represented by its badge alone.
- Task cards use a subtle vertical light edge that adapts to dark and light themes. Focus and current-app outlines remain independent of the neutral glass edge.
- Larger icons are bounded by the cell's available width/height. Dense Fit all layouts retain the compact fallback and existing overflow behavior.
- Current/Protected badges share a centered reserved row above the icon. The same space is reserved on other cards, avoiding a different icon baseline for protected apps.
- RAM remains proportional to device total capacity, in decimal MB. System and app samples retain separate surfaces; estimates are explained in RAM details instead of the permanent main heading.
- Dock bubbles receive a restrained tonal sheen. User-selected colors/shapes, green current highlights and running indicators remain intact.

## Settings scope

No settings navigation redesign. Dock/background and Icons remain separate pages, with larger samples where space permits and compact sizing on short displays. Controller mappings, accessibility font layouts and first-run setup remain available.

## Verification boundaries

Automated controller/input regressions and native Android-view rendering provide local evidence. This is not physical RP6 confirmation; OEM font rendering, controller navigation feel and the final appearance still need device review. No new permission, network dependency or publication is introduced.

## 3.1.1 correction and visual acceptance review

Compared the generated landscape reference with native Android-view renders, rather than accepting layout bounds alone. Fixture package names/icons/RAM values are test data, not device evidence.

| Requirement | Reviewed result |
| --- | --- |
| Icon-only Refresh | Rounded rectangle; accessible name and tooltip retained |
| Selected-value Sort/View | Independent vector chevrons, consistent insets, anchored menus |
| Controller labels | A Open restored; black letters on existing face colors; 8 dp badge/text gap |
| Start separation | Short subtle vertical rule immediately before the shared Start/Settings control |
| Main section separators | No horizontal toolbar border or vertical task/Memory border |
| Task artwork | Scales with card width/height; no former 88 dp ceiling |
| Core alignment | Icon/name/RAM core centered within 5 dp in the reviewed landscape fixture; badge displacement constrained by card padding |
| Status badges | Current and Protected centered above artwork, with the existing status meanings |
| Cards | Neutral dark surfaces, fine light-catching border, restrained focus tint |
| Memory | Two panels, clear heading, stronger used-value hierarchy, aligned app rows and total-RAM-scale bars |
| Dock | Individual bubbles, centered artwork, retained green/current and running indicators |
| Settings | Existing styles/navigation retained; Dock preview surfaces match main follow-theme bubbles |

The final dark/light fixtures use six tasks and six Dock entries. Local layout/controller regressions cover smaller landscape sizes, portrait, font scaling and alternate views. The raster fixtures do not establish physical RP6 rendering, controller feel or OEM behavior. No public release was performed.

## 3.1.2 user revision

This supersedes the 3.1.1 colored-badge and icon-based RAM-details decisions. Controller labels now use theme-inverted monochrome fills; the visible View RAM Details button is restored under the two Memory panels, and App RAM rows contain no application artwork. START has a wider, readable filled label beside the Settings glyph. OLED task fills are almost black without a fill gradient; the fine edge remains. Dark keeps its gradient. Native reference fixtures cover Dark, Light and OLED, including the restored button and populated app-RAM list.
