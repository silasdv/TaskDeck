# TaskDeck user flow — 2.1

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
