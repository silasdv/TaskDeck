# Usability review — TaskDeck 1.1.6

Reviewed against [Jakob Nielsen’s ten usability heuristics](https://www.nngroup.com/articles/ten-usability-heuristics/), before the release build. This is a source/layout review with regression checks, not a moderated user study or physical RP6 acceptance.

| Heuristic | Finding and response | Remaining limits |
| --- | --- | --- |
| Visibility of system status | Live connection/permission checks, explicit required/optional Setup steps, Current badge, inline empty states and Last action. Incomplete optional steps now say Skip for now. | Firmware may suppress notifications; confirm actual delivery on device. |
| Match with the real world | A/B/X/Y and PlayStation labels, MB, named actions; Setup now lets users choose labels before calibration. | Android key mappings may differ from printed buttons; calibration is not universal detection. |
| User control and freedom | Back returns from Settings content to categories before exit; optional Setup steps can be skipped; reset accent and individual remapping are available. | Closing cannot recover unsaved app state. No false undo promise. |
| Consistency and standards | Tasks Down always enters the left dock; RAM Down/Right enters the right dock. Setup theme choices use a two-by-two grid and spatial controller navigation. | Physical input/IME routing needs RP6 verification. |
| Error prevention | Confirm/Back/navigation remain reserved during chord capture; required-service protection and backend rechecks retained. | User-authorized closing remains immediate, as requested. |
| Recognition rather than recall | Visible page hints, inline label choices and sixteen large color swatches; selected and focused swatches have distinct rings. | Swatches retain accessible names/tooltips, although visible color labels are intentionally omitted. |
| Flexibility and efficiency | First-touch actions, D-pad/stick navigation, mapped controls, shortcuts and quick-access favorites retained. | Native firmware gesture replacement is outside this app. |
| Aesthetic and minimalist design | Palette uses complete responsive rows. Buttons use measured text widths/padding and wrapping, rather than narrow caps or smaller text. Redundant Appearance/About pane headings removed. | Large system fonts/native rendering still need device review. |
| Recognize, diagnose and recover from errors | Friendly summaries distinguish connection interruption, denied access and unavailable apps; detailed Last action remains available. No destructive auto-retry. | Native backend errors may contain additional technical details. |
| Help and documentation | USER-FLOW.md rewritten as one current guide; Setup and DEVICE-TEST describe actual limits and recovery. | No claim that all firmware behaves identically. |

## Setup audit

Retained: eight focused steps, once-only onboarding, live permission checks, L1/R1 hints, reserved navigation during recording and completion animation with a deliberate hold. Fixed: missing label selection before PlayStation calibration; ambiguous optional progression; unclear required connection status; four theme choices squeezed into one row. Theme/profile choices now have spatial D-pad/stick routes. The completion animation was not shortened.

Checks exercise compact landscape step fit, optional labels, PlayStation selection, rendered theme labels and existing permission/navigation paths. Full actual permission round trips, controller discovery and perceived animation duration remain hardware checks.

An optional undo-remove-favorite idea is deferred; it is not needed for the approved navigation, sizing, palette or Setup corrections. App closing has no misleading undo or confirmation popup.
