# User flow — 0.8

TaskDeck is a quick task switcher. App selection is the primary screen and uses most of the display. No brand heading or permanent Connect button competes with it.

## First launch

Eight focused steps: Shizuku connection, confirm-button calibration, open-menu chord, direct-switch chord, shortcut-service access, appearance, closing behavior, notifications. Controller steps each show one task and a saved/enabled indicator. A connection is required before continuing; optional controller steps are skipped with Next. L1 goes back, R1 goes forward. Finish saves completion and draws a brief checkmark before opening the deck. Later launches never reopen this wizard.

## Everyday use

Two separate modes share the app catalog. **Switch apps** shows selection/opening and has no destructive X/Y/R1 actions or RAM sidebar. **Manage tasks** shows closing/protection and RAM tools. The toolbar provides mode selection; L1 changes mode with a destination badge. The last mode is remembered. A new installation starts in Switch apps.

Fit all is the default unless a preset was explicitly selected. Refresh, Sort, View and Settings occupy one compact toolbar without a brand heading or Connect button. Sort/View popups have an outline and selected state. Visually compact buttons retain 48dp touch targets. Fit respects readable text size and scrolls only when necessary.

Manage tasks has a system used/free RAM chart and app PSS comparison bars that adapt to the available height. RAM details exposes all reported entries through pages rather than a fixed three-entry list. Values use decimal MB (1 MB = 1,000,000 bytes), including feedback notifications. Unknown measurements remain unavailable. App PSS is approximate and does not decompose system RAM.

## Settings

A category list replaces the repeat wizard: Connection, Controller, Appearance, Closing behavior, Notifications. Categories open an adjacent content pane in landscape; controller up/down previews categories and left/right follows panes and horizontal option rows. Back returns to the deck; subpages return to Settings once setup is complete. Connection and notification tools live here, with no Diagnostics destination.

Stay open after clearing is a stable-label toggle: highlighted/on means the deck remains open, outlined/off means bulk actions return home or to the retained app. RAM feedback and notification shortcut use the same state convention. Controller focus is an additional outline, independent of enabled state. First touch acts immediately.

## Views

Cards, List, Grid and Fit all are the only presets. There is no custom layout editor. Grouping is a separate setting under Appearance. Explicitly selected presets are preserved; Fit all is the default.

## Acceptance

No always-on setup or diagnostics clutter. Controller and touch must reach every setting, RAM page and popup. Charts include textual values/accessibility descriptions and never depend on color alone. Compact buttons retain at least 48dp touch height. Check actual RP6 font scaling/insets and notification delivery on hardware; automated layout/input tests cannot establish those.
