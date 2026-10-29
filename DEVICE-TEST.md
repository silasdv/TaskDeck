# RP6 acceptance test — 0.2

Record firmware/API, Shizuku version and Retroid controller mode. Use disposable app state first; X/Y no longer ask for confirmation.

1. Update over 0.1, connect Shizuku if necessary, finish the dedicated setup. Verify later launches open the deck directly.
2. In landscape, verify multiple horizontal cards, readable labels/RAM states and no clipping. Repeat portrait and with larger system text if used.
3. Without touching a card first, use D-pad left/right and left stick to select the second/third app. A must open that actual existing task. Stop the stick and verify selection stops. B must return useful focus to the previous app.
4. Test X on a disposable task and Y on a disposable set. Removal should be immediate, confirmed by refreshed task data, and should skip protected apps. No close-confirmation dialog should appear.
5. Verify Wave/Home launchers remain protected and Android Settings is closable. Long-press or Select on an ordinary card, toggle protection with A, dismiss with B. Verify the vector lock, persistence after reopening, Y exclusion and unlocking.
6. Record both rear buttons held together, release, inspect both names/scan codes and save with Start. Verify a single rear button still reaches another macro, the full unused chord opens once, and holding does not repeatedly open. Verify actual firmware reports distinct identifiers; analog-only or reserved keys may not work.
7. Test Light/Dark/OLED and setup navigation with D-pad/stick. Android/Shizuku permission screens remain external.
8. Check per-app PSS, including multiple tasks for one app. Unknown values must not be shown as zero. Compare only as approximate memory samples, not guaranteed reclaimable RAM.
9. Grant optional notification permission; close a task and inspect available-RAM delta. It may be zero or negative and is not attributed exclusively to that task.
10. Test tile/notification entry, Shizuku stop/reconnect/reboot, permission revocation and dock/external display. No unavailable backend operation should appear successful.

Report PASS / FAIL / NOT TESTED. Avoid screenshots of private data. Simulation/build success is not a hardware PASS.
