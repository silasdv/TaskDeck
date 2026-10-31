# TaskDeck UX review — 31 October 2026

**Implementation update:** all nine follow-ups below are implemented in local 2.7; see [changes](CHANGES-2.7.md). Dock now limits additions to six/eight apps per the subsequent user correction. The original review below records the pre-2.7 findings and is retained as history.

Scope: source review across main task actions, RAM details, Dock settings/preview, app picker and protection, all setup steps, connection, controller mapping/shortcut capture, notifications and About. Relevant layout/navigation regressions are exercised by the automated suite. This is not a physical RP6 test, screenshot comparison or proof of Android permission/notification behavior.

## Fixes included in 2.6

- Centralize all visible Back actions in the shared button factory: mapped controller badge and Back text, compact consistent geometry, no arrow variant. Include ordinary forms and the Dock picker. Previous/Next step and page navigation retain their own meanings and labels.
- Preview count row has protected top/bottom space and unclipped child drawing. Remove native lift/shadows from its buttons. Confirm actual visibility on RP6; the exact device rendering cause was not reproduced from a screenshot.
- Look examples reserve the same two caption lines for every style. Enlarge all example cards to accommodate the longest caption and full artwork, including the shared Dock examples. Appearance uses its available width. Trim incidental theme-row margins to retain palette space on compact screens.

## New findings and proposals (not implemented)

| Priority | Area | Finding and source evidence | Proposed improvement |
| --- | --- | --- | --- |
| P1 | Main task cards | renderCards() literally uses `X to close`. This is wrong after remapping and with PlayStation labels. Main legend already resolves mappings. | Derive every card action hint from ControllerBindings; reuse vector badges rather than a hard-coded letter. Verify after remap, calibration and profile changes. |
| P1 | App search / controller | In dispatchKeyEvent(), Down in pickerSearch clears focus but chooses a target only if pickerOptions is nonempty. No-results searches have no explicit successor. This is a code-path finding, not a reproduced native freeze. | When no results exist, route Down to a visible Search/Clear/Back action; maintain a valid focus target and expose a clear-query action. Test zero results and clearing a query using only gamepad. |
| P1 | Shortcuts / disconnected actions | Settings record actions remain enabled without shortcut access. startRecording() writes feedback via updateStatus(), but the shortcut form has no status text view. openSelected()/remove() can also return without feedback if backend vanished. | Disable unavailable actions with an adjacent reason and an Enable access/Reconnect action. Show a compact inline explanation when access disappears, retaining a way back. No modal prompts. |
| P2 | RAM | System totals are read live while per-app values come from a sampled snapshot. The UI says approximate but gives no snapshot age. | Show per-app sample age and unavailable/loading/stale states; refresh RAM independently without moving task selection. Do not imply app sums equal used system RAM. |
| P2 | Dock favorites | Clear Dock and Remove from Dock immediately rewrite saved order; there is no Undo. | Brief inline Undo for favorite removal/clearing, restoring exact order and sides. No confirmation popup and no app/task close. |
| P2 | Dock picker rail | updatePickerRail() assigns each favorite equal weight in a fixed-height strip. Many favorites make the icons unreadably small. | Fixed minimum readable icon size, scrollable overflow and a compact count/overflow indicator. Keep the app grid and its eight-item paging intact. |
| P2 | Failed close feedback | remove() emits a generic Some apps could not be closed toast when a batch exits. Per-app results are stored, but remaining cards have no specific outcome marker. | Identify failed apps by name in concise feedback or an inline retry state on remaining cards. Keep confirmed-close overlays and the removed Last action menu out of the main flow. |
| P2 | Setup / accessibility | Completion always schedules its 950-ms transition delay. Custom caption typography uses density rather than scaledDensity, so user font scaling does not carry through those drawn labels. | Honor reduced-motion preferences for completion delay; make caption layout support system font scaling with adequate bounds. Test at 130–200% and distinguish visual acceptance from geometry checks. |
| P3 | Configuration recovery | allowBackup is false and no explicit export/import UI exists. Dock order, controller mappings and theme configuration have no app-provided recovery path after reinstall. | Optional local settings export/import via Android document picker, schema validation and preview of imported values. No network permission. |

## Checked areas without a new change proposed

- Permission setup already distinguishes mandatory Shizuku access from optional hotkey/notification access. Keep the five-step wizard and actual checks.
- Main task navigation and Dock navigation already have regression coverage; no new whole-screen navigation rewrite is justified by this source review.
- Dock preview counts remain separate from saved favorites, background shape is hidden under None, and split menus keep their own edits.
- About retains the confirmed SilasDV project/support destinations. No new external link or publication is needed.
- Notification delivery and firmware-specific task closing still need device observation. Automated checks cannot certify banners, physical mapping or haptics.

Suggested next work: P1 action hints, empty-search focus, and unavailable-action feedback first. Then RAM sample age and recoverable favorite edits. Existing wider visual consistency proposals remain in UX-CONSISTENCY.md; they are not presented as new findings here.
