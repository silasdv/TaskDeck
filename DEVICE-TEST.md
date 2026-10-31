# TaskDeck 3.0 — release acceptance

31 October 2026. User feedback reports satisfactory app appearance/function. This release preparation ran automated gates; it did not add a new physical RP6/second-device sign-off. ADB inventory was blocked by Windows denying creation of the Android configuration directory. No screenshot or simulated button event is used as proof of physical mapping.

| Area | Evidence available | Physical acceptance still required |
| --- | --- | --- |
| Interruptions and permissions | Backend binding deadlines, disconnected recovery UI, shortcut-access gating and unknown-outcome tests | Stop/restart Shizuku, revoke optional access, reboot Android and reconnect |
| Controller changes | Mixed-device chord isolation, calibration/mapping/label and input-routing tests | Built-in, Bluetooth and USB controllers; hotplug while open; firmware-specific printed labels |
| Closing under load | Fresh identity/protection checks, bounded retry, unknown/permission results, grouped windows and bulk-exit tests | Emulator plus disposable apps under high RAM/load; retained Current app; launcher restart and return behavior |
| Touch and focus | First-tap, focused bounds, spatial navigation and preview-button layout regressions | Thumb reach, compact targets, edge clipping and controller-to-touch transitions on RP6 |
| Longer sessions | Request lifecycle/cancellation and repeated input/rebuild regression coverage | Extended open/refresh/close session, responsiveness and process memory growth |
| Backup on another device | Document-intent/stream export, schema checks, preview/apply/cancel, missing/runtime data excluded | Native DocumentsUI/providers, absent imported apps, different controller calibration, unchanged Android grants |
| Accessibility | 130%/200% caption scaling/reflow, reduced-motion completion, names and focus state checks | Actual text rendering, TalkBack flow, theme/accent contrast and all scaled scroll targets |
| Release artifacts | 216 tests pass; lint 0 errors/33 warnings; signed APK/AAB build; version/key/manifest/source/provenance checks | Install/update from official 2.0 and smoke-test on target firmware |

## Suggested device pass

1. Use noncritical/disposable app state. Install the signed 3.0 APK over 2.0; verify favorites, mappings, theme and protection remain.
2. Complete/check setup. Confirm actual permissions, then test revoked access, Shizuku restart and Android reboot. Ensure a visible recovery path remains.
3. Navigate by D-pad/stick, touch and each external controller. Open, close, protect, restart a launcher and Close all but current; test stay-open and exit behavior.
4. Test both Dock menus, 6/8 limits, legacy extras/trim, preview order and Undo including expiry/rotation. Test empty app search and Clear search.
5. Independently refresh RAM; check selected task, sample age/unavailable/stale state and MB scale. Observe real notification and haptic feedback when TaskDeck exits.
6. Export/import through the device's document provider. Inspect the complete preview and cancellation, invalid file handling, missing installed apps and controller recalibration on a second device.
7. Repeat at 130%/200% text size, reduced motion, all themes and both orientations. Include a longer session and note version/firmware/controller details with any failure.
8. Capture current screenshots using [the screenshot checklist](docs/SCREENSHOTS.md); the user will supply the Reddit screenshot.

Android may keep/restart services or HOME. Task removal and app PSS are not universal force-stop/performance guarantees. Historical checklists are archived in [DEVICE-TEST-HISTORY.md](docs/DEVICE-TEST-HISTORY.md).
