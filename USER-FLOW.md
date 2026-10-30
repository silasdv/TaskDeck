# User flow — 1.1.2

## Main screen

One deck, without a separate switcher mode. Every opening selects the leftmost card in the current sort order, even when protected. An empty deck selects Refresh. D-pad/stick move selection, Confirm opens, Back exits, and mapped close/all/retain-current actions execute directly. Existing protections still apply.

Refresh, Sort, View and icon-only Settings form the compact toolbar. Main controls are 36dp with reduced padding; the mapped shoulder label is smaller and has no decorative action icon. Fit all is the default; Cards/List/Grid remain presets. No custom layout editor.

Used/total RAM accompanies the deck. System and app diagrams share the total physical RAM denominator. No available/free RAM number is displayed. All figures are decimal MB; unknown PSS remains unavailable. RAM details pages through all reported apps.

## First launch

Eight short setup screens: Shizuku connection, confirm calibration, open-menu shortcut, direct-switch shortcut, shortcut-service access, appearance, closing, notifications. Connection is required before continuing; controller/shortcut steps are optional. L1 previous/R1 next remain fixed. Finish saves completion and draws a checkmark then holds it until 1.9 seconds total, with Confirm to continue immediately. Later launches skip this wizard.

## Settings

Category list: Connection, Controller, Appearance, Dock, Notifications, About. Connection has a green checkmark when connected. Appearance contains theme, grouping and Stay open after clearing, with a stable highlighted On/outlined Off toggle. Notifications has feedback, shortcut visibility, a bell-icon feedback test and permissions. About shows version, SilasDV, vibecoded/open-source information, GitHub and optional Ko-fi.

Controller opens PadBoard-style Button mapping: letter/PlayStation profiles with swapped variants, confirm calibration, six individual actions and reset. Pressing an already-used button swaps actions. Labels and vectors follow actual bindings. Settings respects mapped Confirm/Back, while D-pad and Android Back remain available. Profiles do not pretend to detect firmware physical layout automatically. Chord shortcuts remain a separate submenu.

Landscape category up/down previews the pane; left/right follows panes and horizontal option rows. First touch executes immediately. Back returns from settings subpages to Settings; RAM/window pages return to the deck. Physical RP6 input, notification and font/inset behavior remain hardware acceptance cases.

## Quick Access and Current

Controller action hints sit top-right immediately before circular Settings. Current marks the reported entry app, independent of selection. Down from the bottom app row enters the favorites dock; Up returns, Left/Right selects, Confirm launches and Select opens reorder/remove options. RAM details is an explicit right-edge selection reachable with D-pad/stick.

Pin/unpin from card Select/long-press; Add apps in Dock settings opens a paginated installed-app picker. The picker returns to its originating screen. Pins are launch shortcuts, not task/protection records. Touch long-hold drag changes order only on drop; cancellation restores saved order.

Dock settings separates Add apps, Style & size and Behavior. Styles: Floating, Compact, Shelf, Thumb corners. Thumb corners uses bottom-left/right panels with larger targets and independent overflow scrolling. Size 60–160%; dock theme Follow app/Light/Dark/OLED. Behavior controls visibility (always or navigation/tap), magnification and running indicators. App Automatic theme follows Android. An empty dock offers Add apps; collapsed mode has a tappable handle.

Setup checkmarks require real access. Revoked Shizuku/accessibility/notification permissions are rechecked on resume; saved shortcut/calibration preferences are not substitutes. Physical-device acceptance remains required.

## Dock usability revision 1.1.1

Thumb corners is the default for an unset style; explicit choices stay unchanged. Style/theme/size settings contain a live preview. Focus grows only the centered icon within a reserved bubble, preventing overflow of its outline or image. Thumb overflow scrolls each selected end item fully into view.

The installed-app picker is a full-width alphabetical icon/name list, six entries per page with scrolling as needed. Search filters in place and keeps typing focus; Down returns to results. A selected row denotes an existing favorite. L1 previous (Back from page one), R1 next, mapped Back and a visible Back control remain available without touching the screen. Search is also controller-focusable.

Current is a separate colored badge beside the app name, or below it on narrow cards. Fit preserves enough row height for readable content and scrolls when necessary. Header actions and Refresh/Sort/View/RAM details use full-height backgrounds and increased padding. Horizontal form navigation crosses aligned panes, including action mapping to Labels; horizontal option groups retain their own left/right order.

## Dock interaction revision 1.1.2

The picker now shows four alphabetical tiles per page instead of a scrolling list. L1/R1 moves through pages; L1 does nothing at the first page. Mapped Back and the visible Back button exit. Search remains; its results keep four tile slots and no Add prefixes.

The outlined green Current badge is the top item of a task card, above both icon and name in every task view. Dock Current fill/outline/dot use the same shared green. Six-dp shell padding surrounds square focus bubbles; the empty opposite corner has no phantom shell when only one favorite exists.

Live preview sits at the bottom, at real dock width/height, using distinct installed launchable apps. Apps + / − Apps changes the sample count only. Style, theme and size update this preview. The actual and preview dock reserve measured height; the weighted content above shrinks as dock size increases.

Confirm on a Settings category enters the right content pane. Fit all and Recent remain defaults for unset choices; saved choices persist. Dock heading, idle selected-item text and task count are omitted; feedback errors and RAM information remain.
