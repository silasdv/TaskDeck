# Usability review — TaskDeck 2.0

Source/layout review using [Nielsen’s ten usability heuristics](https://www.nngroup.com/articles/ten-usability-heuristics/); not a physical RP6 certification or moderated user study.

| Heuristic | Current response | Remaining acceptance |
| --- | --- | --- |
| Visibility | Live permissions, calibrated checkmarks, Current badge, added-app toast and Dock icon strip; no redundant confirmed banner. | Real notification/permission behavior. |
| Familiar language | Dock and Quick Swap Apps consistently; explicit actions and MB. | Firmware key reports may differ. |
| User control | Inline recording, cancellation, Redo Setup, Clear Dock and explicit locks. | Closing cannot restore unsaved app state. |
| Consistency | Accent focus, equal vertical style controls, controller paging and spatial Dock routes. | Native rendering and hardware navigation. |
| Error prevention | Permissions precede and gate hotkeys; conflicts rejected; required services protected. | Partial chords can reach games. |
| Recognition | Eight app icons, vector style previews, Select hint and completion checkmarks. | Large Dock sets make the icon strip smaller. |
| Efficiency | First touch, mapped controls, search, eight-app pages and Fit all transitions. | Global shortcut/firmware routing. |
| Minimalism | Five setup pages, no nested recorder, Always show Dock checkbox, no Last action in Settings or Android-style app selection dialogs. | System permission interfaces remain outside app styling. |
| Recovery | Friendly inline errors and official Shizuku links; no destructive auto-retry. | Firmware-specific restrictions. |
| Help | Permissions/protection explained, visible drag hint and current flow/checklist. | No universal compatibility guarantee. |

Completion drawing is slower (800 ms), with about 150 ms further hold. Ordinary apps/launchers are no longer automatically locked. Simulated checks cover selected state, layout and input behavior, not actual motor feedback or Shizuku firmware behavior.

Reported geometry/navigation fixes: measured centered picker groups and equal gaps, single setup shoulder pair, explicit accent cross-pane routes, adjacent permission reasons/downloads and faithful saved-Dock previews.

Setup now routes themes Down through both appearance actions. Final protection uses horizontal edge/swipe navigation; shoulders page apps while B/Start handle setup. Compact footer frames explicitly label paging and fit narrow screens. Semantic and official-brand icons improve recognition. An explicit bulk input is retained during successful refresh, and the exit preference applies even when some closes fail. No automatic destructive retry is introduced.

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
