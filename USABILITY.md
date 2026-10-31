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
