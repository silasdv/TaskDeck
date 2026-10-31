# TaskDeck 2.1 — local update

- Dock style & size now includes background color, Round/Square/Squircle shape, Original/B&W/Accent/Glass icon looks and Follow accent color inline. Dock appearance is no longer a destination; old restored destinations redirect to Dock style.
- Smaller, separated size-stepper buttons. Swap sides is a normal action button and participates in spatial controller navigation. Style-preview glyphs stay neutral white on a dark miniature background, independent of app colors.
- Preview does not force the first app selected. Current still uses semantic green. Background swatches have a contrasting dark/light outline as well as selected/focused treatment.
- Icon scale genuinely grows to 160%. Dock Size is a minimum box size: boxes grow when required to contain logos, focus magnification and the indicator strip. Circle, square and squircle focus backgrounds follow the chosen mask. Running/current dots remain below the logo.
- Original/B&W/Accent/Glass task icons are available in Appearance. Glass is a stylized monochrome gradient highlight, not Android system blur or an Apple rendering API.
- Start opens Settings rather than exiting, with an outlined Start hint beside its icon. It is reserved from remapping; old Start action/page bindings migrate to available buttons. Setup/chord save semantics remain available.
- Back from a settings subpage restores its originating content button, e.g. Dock behavior, instead of returning selection to the Dock category. Back again returns to categories before exiting.
- Protected-task lock badges sit above app icons with the same 6dp gap as Current. Current + protected share a badge to avoid a second vertical row.
- Closing can retry exactly once after 50 ms, before final feedback, only after fresh task identity/protection checks. Already absent ordinary tasks are confirmed without another close. Protected/permission failures, unknown connection outcomes, interrupted/deadline-expired work and ambiguous launcher outcomes are not replayed. Persistent failure is reported after the second attempt. Batch deadline remains bounded at 60 seconds.
- Compact landscape Dock panes can scroll when necessary; larger screens show both panes together with the unchanged bottom preview. Appearance toggles share a row with wrapping where space permits, retaining readable text and the accent palette.

Same package/signing key as 2.0; versionCode 210, UserService 9. Install over 2.0 to retain settings. No additional permissions or network access. Native RP6 glass/mask appearance, focus, firmware inputs and privileged close outcomes remain physical acceptance cases. GitHub 2.0 is unchanged; this update is packaged locally.
