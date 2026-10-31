# TaskDeck 2.3 — local update

Historical local milestone, included in [TaskDeck 3.0](RELEASE-3.0.md). Final 3.0 behavior supersedes intermediate designs below.

- Group Dock style into Dock / Background and Icons within the same screen. Rename Size to Dock Size.
- Hide Background Shape when Background Color is None, preserve the saved shape, and prevent that hidden shape from affecting icon sizing or focus geometry.
- Add a Dock Layout heading and centered icon-over-caption layout choices. Add captions to icon shapes and styles; remove the redundant Follow Accent control and fixed-box explanation.
- Compact and center Swap sides; top-align content panels and leave consistent gaps around preview controls and the Dock.
- Center Dock artwork inside its background while retaining clear running/current indicators.
- Keep App icon style eight dp below the accent palette and support controller navigation from the palette into style choices.
- Migrate the old accent override to the explicit Accent Color style when appropriate.

182 automated tests pass; release APK/AAB and lint pass (0 errors, 33 existing warnings). Physical RP6 visual/controller acceptance remains open. No GitHub assets were changed.
