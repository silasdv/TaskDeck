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
