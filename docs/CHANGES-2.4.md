# TaskDeck 2.4 — local update

Historical local milestone, included in [TaskDeck 3.0](RELEASE-3.0.md). Final 3.0 behavior supersedes intermediate designs below.

- Use the available settings area instead of leaving a broad empty band above the Dock. Both background panels share one bottom edge; visual choices expand into spare space and compact layouts retain the tested no-scroll fit.
- Separate saved selection (accent fill and a tick) from controller focus (a consistent outline). Layout thumbnails no longer have an extra dark backdrop.
- Use the same minus / percentage / plus stepper for Dock Size and Icon Size; dim unavailable range and preview-count actions.
- Show neutral, captioned background shapes without app artwork. Icon masks retain their real-app examples. Use a two-row background-shape grid when height permits.
- Distinguish None with a slash and Follow theme with a split light/dark sample. Reveal color names in the heading on focus/hover; preserve accessible names and tooltips.
- Label the bottom area Live preview, keep its Dock at the actual size/position, and explain that app-count controls change preview samples only. Use input-specific help and neutral app states, so Current tint no longer conceals the chosen background color.
- Keep focus on a usable sibling when a size or preview-count limit disables the active control. Layout illustration ink follows the theme foreground for light/dark contrast.
- Add the mapped Back controller badge to the compact Back button. Preserve controller navigation and saved Dock contents while adjusting preview counts.

Physical RP6 appearance and input acceptance remain open. No GitHub publication is included.

A source review of other Settings/setup inconsistencies is recorded in UX-CONSISTENCY.md. It proposes shared states, theme/palette/toggle controls and navigation treatments; those wider changes are not part of this Dock update.

Validation: 187 tests passed, lint 0 errors / 33 existing warnings, release APK/AAB builds passed.
