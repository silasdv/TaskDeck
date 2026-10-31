# Settings and setup consistency audit — 31 October 2026

Source review following the Dock 2.4 redesign. These are proposed follow-ups, not claims of completed changes or native device validation.

| Priority | Area | Evidence / issue | Proposed shared rule |
| --- | --- | --- | --- |
| High | Theme choice in Appearance and setup | Uses generic button selected border; Dock visual choices now use fill/tick plus independent focus outline. | Reuse one exclusive-choice surface and selection/focus states. |
| High | Accent Colors | AccentDot uses 1-dp selected outline and 3-dp focus outline, differing from DockColorSwatch. | Keep the existing palette arrangement but adopt the same selected/focus semantics and accessible focused name. |
| High | On/Off controls: Appearance, Dock behavior, Notifications, shortcuts | toggle() uses full accent fill and a light focus outline when on, accent outline when off. | One toggle component: stable state indicator, consistent focus outline, readable foreground in all themes. Do not imply radio-choice exclusivity for independent toggles. |
| High | Setup / Appearance reuse | Setup themes use two columns, Settings themes use one row, and palette area sizing differs. | Shared responsive theme/palette components, adapting to actual pane width and height; preserve wizard context and five steps. |
| High | Setup headers | App protection uses a smaller standalone title than the other four form pages. | Share wizard title metrics, spacing and progress placement across all five steps. |
| Medium | Back / footer navigation | Dock Back has mapped controller badge; other form Back buttons retain a generic arrow. Wizard footer has separate paging semantics. | Shared mapped Back badge in submenus, B returns to category before leaving Settings; retain labelled step/page navigation in setup. |
| Medium | Button mapping and shortcut recording | Fixed explanatory panes and action rows differ from contextual Dock behavior help. | Align labels/badges and use focus/hover-specific explanations; retain visible capture/permission/calibration status. |
| Medium | App pickers / protection | Shared app picker already serves both, but paging lives in different footer contexts. | Preserve a common card layout, spacing, focus and loading motion; clearly distinguish Dock membership from closing protection. |
| Medium | Layout / accessibility | formPage retains generic panel heights, widths and paddings. Compact targets cannot automatically guarantee 48 dp everywhere. | Shared spacing and target-size tokens, remove incidental gaps without stretching every screen, test portrait/landscape, maximum font scaling and controller reachability. |

Keep intentional differences: permissions use granted/required/optional status, protection uses a lock, setup shows progress/Next/Finish, settings applies changes immediately. A navigation-category highlight is not a saved preference or an enabled toggle. Preserve those semantic distinctions while sharing dimensions and focus treatment.

Suggested order: shared state styles and spacing tokens; shared theme/palette/toggle components; mapped submenu Back controls; setup responsive layouts; keyboard/controller and physical RP6 acceptance. No additional submenu redesign is implemented by this audit.
