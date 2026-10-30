# TaskDeck user flow — 1.1.6

## First launch

Setup runs once; Settings is used thereafter. Eight screens cover Connection, Confirm calibration, Open-menu shortcut, Direct-switch shortcut, Shortcut-service access, Appearance, Closing behavior and Notifications. Each screen explains one decision. L1 goes back and R1 advances, with visible labels; mapped Confirm/Back and visible controls remain available. Connection is required. Its completion is derived from the live backend, never a saved preference. Incomplete optional controller/access steps explicitly offer **Skip for now**.

Choose A/B/X/Y or PlayStation labels inline before calibrating the printed A/Cross button. Calibration infers the common face-button swap; it cannot identify every firmware permutation. Shortcut recording reserves navigation and Confirm/Back/Start. Theme choices use a two-by-two layout. Permission states are checked against Android rather than inferred from clicking a grant button. Finishing saves completion, shows an animated checkmark and holds for about 1.9 seconds; Confirm may continue sooner.

## Main screen

Opening selects the first task; Refresh is selected when no tasks exist. Fit all and Recent are defaults, preserving explicit changes. Touch acts on its first tap; controller input switches to explicit focus navigation.

- Up from the top task row reaches Refresh; Left/Right traverses the toolbar.
- Right from the task edge reaches RAM details; Up from RAM reaches Settings.
- Down from Tasks always enters the leftmost dock item, independent of the previously visited dock side.
- Right/Down from RAM details enters the right Thumb corner; Up from that corner returns to RAM details.
- Confirm opens, Back exits, X closes, Y closes all and R1 closes all but current by default. Main actions are remappable.

The Current badge is separate and green. Favorites are shortcuts, not synthetic tasks or protection. Required services remain excluded from closable cards; ordinary user locks sort last. Empty states stay inline.

## Settings

Categories sit beside content in landscape. Confirm enters content; Back first returns to categories, then exits Settings. D-pad/stick supports adjacent options. Buttons in a group have equal widths based on actual content with padding, wrapping and height when necessary; no arbitrary narrow cap or ellipsis hides the action.

Appearance includes theme, grouping, stay-open behavior and sixteen color dots in complete responsive rows. Dots use the remaining area, have accessible names/tooltips and show stored selection with an inner ring and focus with an outer ring. Reset restores the theme accent. Semantic Current/success colors remain green.

## Quick Access and reports

Dock settings offer style, size, theme and behavior with the real bottom-position preview. Thumb corners is default. Add-app selection is alphabetical, four items per page, with search and visible icons. The preview count does not change favorites. Controller Previous/Next page mappings are independent of main actions. Last action page controls align to opposite edges. Long press/Select opens item options; touch drag or Move left/right reorders favorites.

## Recovery and limits

Connection interruption directs users to Settings → Connection; denied access points to Shizuku authorization; missing apps suggest refresh/removal. Destructive requests are never automatically replayed. Last action retains detailed outcomes in memory. Permission/channel restrictions can suppress notifications, and unknown RAM is unavailable rather than zero. Closing tasks does not prove their services stopped or that performance improved.

This is the current interaction contract. Historical changes belong in CHANGELOG.md. Physical controller routing, native fonts/insets, permission returns, Shizuku and notification delivery require device acceptance; simulation is not hardware certification.
