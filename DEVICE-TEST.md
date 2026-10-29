# RP6 acceptance test — 0.4

Record firmware/API, Shizuku version and Retroid controller mode. Use disposable app state first. The following hardware checks are NOT TESTED here.

1. Upgrade over 0.3 without uninstalling. Verify saved preferences and Shizuku reconnection to UserService version 4.
2. Complete the four setup screens entirely with D-pad/left stick and calibrated Confirm. Return from Android permission settings and verify live connection/Accessibility/notification status. All content and Next/Previous fit without scrolling in landscape; verify Back spacing and neutral gear/menu icons.
3. Change themes with a single touch, then immediately navigate with the controller. Repeat lock/unlock and setup toggles; no first-tap focus-only behavior. Focus outlines must follow rounded controls.
4. Record both rear buttons together, release, inspect identifiers, save with Confirm or Start. D-pad and Confirm must not become chord inputs. Verify service enable/disable and full chord opens once; partial chords still reach the game.
5. In landscape verify more than two ordinary cards, readable RAM states and no clipping; repeat portrait, large text and dock. HOME/Wave must be visible and protected, even without a recent task; A must open the launcher. Other infrastructure/taskless processes belong in the small RAM summary, not as closable cards.
6. Select apps using D-pad/stick without first touching. Open A and close X. On every fresh opening select the first visible card; if nothing is closable, focus Refresh immediately. Right must reach Sort without an Up press. Down may select a protected launcher, but cannot leave the toolbar if there are no cards. Repeat after prior touch use and after closing the last eligible task.
7. Long-press or Select, protect/unprotect with touch or Confirm; verify locks persist and always sort last for Recent, RAM and Alphabetical. Locked ordinary apps remain visible. Sort popup supports D-pad/stick/Confirm/Back.
8. Dismiss Settings and another ordinary preinstalled app through TaskDeck, then compare native Overview. There is no blanket system-app block; explicit infrastructure protections remain intentional. Report any Android rejection verbatim. This is task removal, not package force-stop.
9. From a disposable game/browser, open TaskDeck via the chord. Select a different card and change sort, then R1: previous app/package stays and opens, other eligible tasks close. Repeat from HOME, split-screen and dock; previous-task detection may fall back to recent order on unsupported firmware.
10. Y closes eligible tasks and returns HOME. Both Y and R1 exit with an empty deck; “Nothing to close” stays inline. Used RAM may remain high: verify the sidebar explanation and approximate/unknown memory for protected/taskless apps. Set “Stay open after close actions” and repeat: menu stays. Backend errors should remain visible instead of claiming success.
11. Show then hide notification shortcut, revoke/grant Android notification permission and block/unblock each channel. Labels reflect actual state. Enable/disable RAM notifications and inspect available-memory delta; zero/negative change is valid. Result notification should still arrive after the menu exits.
12. Test tile/notification entry, Shizuku stop/reconnect/reboot and shortcut-service conflicts. Firmware/permission failures must not appear successful. Native swipe-up remains Android’s overview.

Report PASS / FAIL / NOT TESTED. Simulation and build success do not establish hardware compatibility. No screenshots of private app content are required.
