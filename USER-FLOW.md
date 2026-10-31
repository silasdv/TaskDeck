# TaskDeck user flow — 2.2

## Setup

Five pages: Permissions → Controller → Shortcuts → Appearance → App protection. L1/R1 navigate except on final app protection. Shizuku is required for tasks; shortcut-service access is required only for hotkeys; notifications are optional. All status reflects live permission checks. The official Shizuku guide is linked in setup and Connection settings.

Controller label selection and calibration stay inline. Both deck and Quick Swap Apps chords record inline after Android grants shortcut access. Hold up to four eligible buttons and release to save. Checkmarks confirm saved recordings. D-pad, Confirm/Back/Start and setup L1/R1 stay reserved; Back cancels capture. Appearance includes themes, accent swatches, closing behavior and vibration. Protection uses the searchable paged app grid. Ordinary apps/launchers are not locked by default; required services remain protected.

Completion draws for 800 ms and opens the deck after about 950 ms total; Confirm can skip. Settings > About > Redo Setup repeats onboarding without clearing configuration.

## Main screen

First task selected on entry, Refresh when empty. Fit all layout changes animate when system animations are enabled. Successful closing has no confirmed banner; errors remain inline. Last action is removed from Settings. The one-row legend includes outlined Select/Share for task options; B text and PlayStation glyphs are white.

Tasks Up → Refresh → toolbar; Tasks Right → RAM details. RAM Up → Settings; RAM Down/Right → right Thumb corner. Tasks Down enters the leftmost Dock app. Current is a separate green badge. Confirm enters Settings content; Back first returns to categories. Touch acts on its first tap.

## Dock

Use Dock consistently. Add-app selection has eight alphabetical tiles in two rows of four, search, paging, Clear Dock and an icon-only strip of selected apps on the right. Image layers sit above the buttons. Adding an app gives an app-named toast. A picker page needs no scrolling.

Style choices are three equal-size vertical controls: Thumb corners, Floating, Shelf. Compact migrates to Floating. Size and Icon size are independent 50–160% controls; boxes grow as needed to fit logos, magnification and visible indicators. Thumb corners supports Swap sides and touch-hold dragging. Thumb corners has left/right plus/minus controls; Floating and Shelf have one pair above the bottom preview; preview edits do not change saved apps. Dock style includes eight background choices, Round/Square/Squircle masks, four icon looks and Follow accent color inline; Follow theme is the default. Always show Dock is a compact touch/Confirm checkbox. Off reveals the Dock on navigation/tap; it is not transparency. Internal menus use TaskDeck panels; system permission interfaces remain Android/Shizuku-owned.

## Limits

Required services stay protected. No automatic destructive replay, fake permission grant or closing undo promise. RAM is estimated MB and does not prove a speed gain. Native RP6 fonts, transition appearance, controller routing, real permission returns, haptics, Shizuku and notifications still need physical acceptance.

Picker icon/name groups are centered with matching border gaps. Setup download links include Google Play and GitHub; each permission has an adjacent reason. Setup Appearance Right enters colors; Left returns to the originating choice. Protection app paging uses arrows separately from setup L1/R1. About stacks its three actions. Preview starts from saved apps/sides, then alphabetical extras.

Appearance Down reaches both full-size On/Off actions. Bulk closing honors the exit setting even on partial outcomes. A Close All request during refresh runs once after the fresh task list; destructive actions are never automatically replayed.

The final setup footer is compact and labels Previous page/Next page beside L1/R1. Start/Options is written in its badge. Page motion starts after layout over 360 ms, with canceled/stale transforms reset. Dock style texts share a left start with more padding. Record actions use Record icons; RAM uses a chip, Swap sides opposing arrows, and layout/download controls their own brand marks.

Dock app pages use the same 360 ms, after-layout transition as setup protection pages. Preview count buttons show one plus/minus icon beside Apps; size and Appearance choices have larger internal padding.

2.0 adds independent icon scale, masked adaptive icons, Dock backgrounds and style-specific preview controls. [All changes since the public 1.0 release](docs/RELEASE-2.0.md).

2.1: Start opens Settings and is reserved; Back restores the originating setting. Protected badges sit above icons. Background previews are not forced-selected and swatches have contrast outlines. Main task icons and Dock icons offer monochrome, accent and glass looks. Known close failures get one fresh-identity-checked retry after 50 ms; uncertain/deadline/permission outcomes are not blindly replayed. Compact Dock panes retain scroll access rather than clipping controls. [Detailed local changes](docs/CHANGES-2.1.md).

2.2 supersedes the 2.1 Dock layout/scaling: four compact areas (two-by-two on phones), independent icon/background shapes, None fill and fixed box dimensions. Visual app samples replace shape/look text choices. Appearance separates themes, three behavior toggles, unchanged accent swatches, then App icon style. Dock behavior help follows focus/hover. Initial app loading fades and quick repeated Confirm retains focus. [Full redesign details](docs/CHANGES-2.2.md).

## Local 2.3 follow-up

Dock style groups Dock / Background and Icons on one screen. Under None, Background Shape is hidden and its previous value retained. Check restored focus, captions, compact spacing, centered icons and running indicators on RP6; verify palette-to-icon-style navigation with D-pad and stick. Automated layout checks passed, but physical acceptance is still required.

## Local 2.4 UX follow-up

Dock settings use the available panel height, with equal-height groups, captioned neutral background forms and matching percentage steppers. A saved choice is filled with a tick; focus is outlined independently. None and Theme have distinct symbols and focused colors reveal a name. The live preview uses neutral app states and never saves sample-count changes. Back carries its mapped controller badge. On RP6, inspect focus versus saved selection in Light/Dark/OLED, color edges, caption readability, compact targets, maximum Dock size, panel fit and actual preview alignment. Compact targets do not establish a universal 48-dp touch-target guarantee; physical acceptance remains open.

## Local 2.5 — split Dock settings

Settings > Dock now has Add apps to Dock, Dock / Background, Icons and Dock behavior. The two customization menus retain their own edits and return focus to their originating Settings entry. Each has the actual-size preview at the bottom, bounded two-panel forms in landscape and stacked forms in portrait. Check both menus at maximum Dock/icon sizes on RP6, including D-pad/stick access to every control and Back, None hiding the shape section, focus name versus saved color, and live preview updates without changing favorites. Wider Settings/setup consistency proposals remain deferred in docs/UX-CONSISTENCY.md.

## Local 2.6

All Back actions use the mapped controller badge and common compact dimensions; wizard Previous/Next and paging keep their distinct semantics. Preview count controls have no native lift and reserve drawing clearance. All icon-look examples reserve two caption lines and share full artwork size. On RP6 inspect pressed/focused preview buttons, all Back badges with remapped/PlayStation controls, equal example artwork and compact Appearance palette readability. Source-review findings outside these fixes are documented in docs/UX-REVIEW-2.6.md and remain proposals.

## Local 2.7 acceptance

The nine UX-REVIEW-2.6 follow-ups are implemented, with the user's six/eight-app Dock limits replacing unrestricted growth. Check zero-result search entirely by gamepad; disabled shortcut recording and recovery access; remapped/PlayStation card hints; memory age and independent refresh without losing selection; named failed-close feedback; favorite removal, clear and trim Undo (including expiry and rotation); full Dock addition limits; and readable picker rail overflow for legacy saved extras.

Check native DocumentsUI export/import on RP6, reviewing every imported setting before Apply, cancellation while reading, and unchanged Android grants. Import resets omitted customization to defaults. Check 130% and 200% system fonts and reduced motion. Automated tests use fake IPC/document streams and do not establish physical controller mapping, Shizuku outcomes, haptic/notification delivery or native rendering.
