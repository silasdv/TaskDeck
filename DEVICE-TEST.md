# RP6 acceptance test — not yet executed

Record Android version/API, Retroid firmware build, controller input mode and Shizuku version. Do not include personal text or screenshots of sensitive apps in a report.

1. Start Shizuku and connect TaskDeck. Verify a shell/root UID and actual recent tasks. Record any displayed error exactly.
2. Open two non-critical apps with recognizable state, such as a browser test page and calculator. Open TaskDeck from its Quick Settings tile; A must return to the selected existing task.
3. Test D-pad and left-stick navigation. Hold and release the stick; selection must stop. B must exit the menu and return usable input focus to the prior app.
4. If needed, calibrate printed A. Verify A open, B back, X close and Y close-all. Confirmation dialogs must accept D-pad plus calibrated A and cancel with calibrated B. Held buttons must not trigger repeated operations.
5. Close one non-critical app task. It should disappear only after backend confirmation. Check Android's Overview separately. A remaining background service is not automatically an error: this is task removal, not force-stop.
6. Confirm that home/system tasks and the active keyboard are marked protected and cannot be removed. The Y count must exclude them.
7. With only disposable tasks eligible, test Y confirmation and per-task result counts. Do not use unsaved games for this test.
8. Learn an unused digital/macro key and enable the optional Accessibility service. Open TaskDeck from the launcher, browser and an emulator. Other game buttons must still reach the game. Holding the shortcut must open only once.
9. Disable the shortcut and verify its key is released back to the game. Withdraw Accessibility access and verify the Quick Settings tile still works.
10. Stop Shizuku and verify an explicit disconnected/error state. No destructive operation may appear successful. Restart and reconnect; test again after reboot.
11. Check landscape, portrait and dock/external display. Verify touch buttons remain reachable and that opening/closing dialogs does not leave stick-repeat active.

Report actual results as PASS / FAIL / NOT TESTED for each item. Do not convert compile/test success into a hardware PASS.
