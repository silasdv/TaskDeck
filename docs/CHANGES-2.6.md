# TaskDeck 2.6 — local update

- Unify Back buttons across forms, settings/setup submenus and Dock app selection using the mapped controller badge plus Back text. Remove the Dock-only badge override; Previous/Next actions retain their own bindings.
- Protect preview Apps minus/plus buttons from clipping: add drawing clearance, remove native elevation/translation-Z and state-list lift, and keep controls inside their row.
- Give every icon-look example the same two-line caption reserve and full icon area. Increase shared card sizes and use Appearance pane width so Black and White/Accent Color no longer shrink their artwork versus Normal/Glass.
- Preserve compact palette room by removing incidental theme-row margins.
- Document a source-based app-wide UX review with prioritized follow-ups separately from implemented fixes.

See [UX review](UX-REVIEW-2.6.md) for findings not implemented by this package. RP6 visual/controller acceptance remains open; public GitHub 2.0 is unchanged.

Validation: 193 tests passed; lint 0 errors / 33 existing warnings; release APK/AAB passed.
