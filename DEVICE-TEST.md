# Version 1.1.4 acceptance additions

- Four picker tile icons stay inside borders across actual app/adaptive icons, search keyboard, font scale and insets. Empty dock Add apps label keeps fully rounded focus.
- Current badge sits close above the task icon, centered as a group. Thumb corners has four independent boxes for four apps; test drag/reorder/overflow and Current green.
- Matching System/App RAM surfaces, no chart overflow at short heights, full RAM details available.
- Previous/Next controller badges follow remapping in picker, Last action, RAM and windows. Assign L2/R2, test pages/end limits and cancel capture with Back. Main actions remain unchanged.
- Accent palette in Light/Dark/OLED/Automatic; verify colors/contrast, round swatches, persistence and semantic Current green.
- Up from tasks reaches Refresh; Right traverses toolbar; task-edge Right reaches RAM details, Up reaches Settings, Right reaches right dock. Check D-pad/stick and empty decks. Back from Settings content returns to categories before exiting.

---

# Version 1.1.3 acceptance additions

- Verify 8dp border gaps in Cards/Fit/Grid/List, especially toolbar to tasks and RAM Details to dock. Check system insets, larger fonts, empty/collapsed docks and portrait.
- Face badges slightly larger, with unchanged text and button/background dimensions. Verify PlayStation labels and remapped shoulders.
- System RAM card contains system totals/usage; app diagrams remain scaled to total physical RAM and controller RAM details remain accessible.

---

# Version 1.1.2 acceptance additions

- Four picker tiles, L1/R1 pages, search, mapped Back and touch activation. Previous page at the first page must not exit.
- Current badge above icon/name in all views; green dock fill/outline/dot on OLED, Dark and Light.
- One/two/three favorites: shell corners align with focus bubbles; no empty right bubble for a single favorite. Check hover/controller and insets at 60–160%.
- Bottom preview shows different real apps at original size. Plus/minus must not pin/unpin actual favorites. Growing preview/dock shrinks content above. Check compact landscape and font scaling.
- Confirm on Settings category enters the right pane. Idle counts/selection labels and dock heading are absent; errors remain readable.

---

# Version 1.1.1 acceptance additions

- Picker: large icons/plain names, alphabetical order, search/no results, selection persistence, L1/R1 pages, L1 Back on first page, mapped Back. First touch executes after controller; D-pad/stick Down leaves search for results without a second touch.
- Dock: unset style defaults to Thumb corners; explicit style survives. Preview reflects every style/theme and 60–160% size. At maximum size, selected icons and bubbles stay inside their corner/viewport, including long overflowing favorites lists.
- Current badge remains attached to the correct app, separate from its name, in all four task views. Verify legibility and no clipping at system font scaling/insets.
- Mapping Right reaches Labels; Left returns to actions. Check all other two-pane/horizontal options. Header and RAM button backgrounds/padding should appear aligned.

---

# Version 1.1 acceptance additions

Install 1.1 over release 1.0; verify settings survive. The automated suite is not a substitute for these physical RP6 checks:

- Floating, Compact, Shelf and Thumb corners in landscape/portrait, font scaling and system insets. Thumb corner targets stay reachable; overflow scrolls independently at both sides.
- Pin closed/running apps; no synthetic task cards or protection changes. Touch hold/drag across corners and within overflow, drop, cancel; order persists after reopening. Controller Select move/remove provides an alternative.
- Dock size 60/100/160%, all themes, Automatic Android theme changes, always/focus visibility, hover/focus magnification and indicators with unavailable RAM data.
- First touch after controller use activates once. D-pad/stick can enter/leave dock, launch a favorite and reach RAM details. Header labels and circular Settings remain legible.
- Current marker follows the actual entry app for shortcut, tile and notification entry; Android may not report every origin reliably.
- Revoke/regrant Shizuku, notification and Accessibility permissions; setup indicators update honestly. Completion remains visible, and Confirm skips without accidentally launching an app.

---

# RP6 acceptance — 1.0

The checks below require physical hardware and have not been executed here. Record firmware/API, Shizuku version, controller reporting mode and font scale. Use disposable app state; immediate closing does not save games.

1. 1.0 has a dedicated release key. Note settings and uninstall a 0.x debug build before installing it. Future release-key updates preserve data. Application versionCode 100, UserService 8. Existing setup completion opens the deck rather than the wizard.
2. Fresh-install flow: eight separate screens, one controller task per screen. Check connection, confirm calibration, open-menu chord, direct-switch chord, service access, appearance, closing and notifications. Optional steps skip with Next. L1 previous/R1 next work once per press, and Next is on the right.
3. Finish shows the drawn completion checkmark, then opens the deck. Relaunch never repeats setup. Rotate during setup/calibration/completion and verify sensible restoration without repeating completed setup.
4. One deck, no mode tabs. Opening selects the first card, even when protected. Empty lists focus Refresh. A opens, B exits; X/Y/R1 close eligible tasks with the current mapped labels. Main buttons are compact, Settings has an accessible icon without text, and Close all but current has no trailing icon.
5. Fit all is the default when no preset was explicitly selected. View has exactly four outlined presets: Fit all, Grid, List and Cards. No custom layout editor. Test many apps, minimum readable text and scroll fallback. Existing explicit preset choice is preserved.
6. Sort popup outline remains visible in Light/Dark/OLED. D-pad/stick/Confirm/Back and first-tap touch work. Verify Recent, RAM and Alphabetical; locks sort last in the deck, without disturbing the deck ordering.
7. Settings is a category list, never a multi-step wizard. Visit all five categories in landscape with no clipped controls. Up/down changes categories; right enters controls; left returns. Horizontal theme rows support left/right. Subpage Back returns to Settings. Controller focus follows rounded shapes.
8. Stay open after clearing: On is filled/highlighted and keeps the deck open; Off is outlined and exits after bulk clearing. The label is stable. RAM feedback and notification shortcut show the same state convention. Touch acts once; controller can select and change repeatedly without losing position.
9. Setup calibration must allow D-pad/left stick and Back/center navigation, not capture navigation as mapping. Test swapped AB/XY reporting, then both separate digital chords, scan-code display, conflict rejection and service status. Analog-only/firmware-reserved buttons may be unavailable.
10. Inspect system used/total bar and app comparison bars. All memory text, card labels, test toast and notifications say MB, without free/available RAM counters. App bars must scale to total physical RAM, not the largest app. 1 MB means 1,000,000 bytes. Open RAM details: every reported app is reachable through pages, not only three entries. Labels and bars fit at large font scale and unknown samples stay unavailable.
11. Group two windows of one app; verify count and one package PSS sample. A opens the representative; Choose window opens the requested task. Grouped X closes represented windows. Settings > Appearance > Group app windows Off restores individual task cards.
12. Never-opened installed launchers do not appear. Open a launcher, unlock its real task, close it, and inspect Last action. Restart preserves its saved lock and must confirm different PIDs where permitted. Default HOME may reopen automatically. Required keyboards/core services stay protected; ordinary Settings tasks can be removed.
13. Open from a disposable app, change sorting/selection, then Close other apps: the entry app is retained. Close all returns HOME when Stay open is Off, even for an empty deck. Empty decks focus Refresh and controller navigation never freezes.
14. Stop/restart Shizuku, reconnect through Settings > Connection, and test a backend failure. No destructive operation repeats automatically. Failed/unverified results stay named under Settings > Connection > Last action.
15. Settings > Notifications > Test feedback; then X/Y/R1 and launcher restart. Check visible toast/banner after the Activity exits, permission denied/granted, blocked channels, DND and gaming mode. Hiding the notification shortcut removes it. Actual banner presentation is Android controlled and remains unverified here.
16. Repeat at RP6 landscape dimensions, portrait, dock, nondefault system font size and insets. Check no toolbar/footer clipping, target size, legibility, focus continuity, RAM pagination and app selection after touch/controller transitions.

17. Settings > Controller > Button mapping: test all four label profiles, calibration with printed A/Cross, custom action assignment and conflict swaps. New bindings and vector labels must agree in the footer, dialogs and Settings navigation. D-pad/Android Back must remain usable during assignment. Reset preserves label style. Repeat after changing Android controller mode.
18. Connection has a green checkmark only when connected, in every theme. Appearance includes Stay open after clearing; there is no Closing behavior category. Test feedback uses a bell icon. About shows 1.0, SilasDV and the optional external Ko-fi/GitHub links.
