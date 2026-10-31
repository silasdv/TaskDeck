# Historical device acceptance checklists

These are development-era checklists, not proof that each item was executed. [Current 3.0 matrix](../DEVICE-TEST.md).

# 2.2 physical acceptance

- Dock menu: every option visible without scrolling on RP6; four wide-screen columns/two-by-two portrait, larger targets when height permits, centered plus/minus and balanced spacing.
- Icon/background shape independent; Rounded remains default. Inspect real adaptive icon square/squircle corners before/after B&W/Accent/Glass filtering. Legacy raster corners cannot be reconstructed.
- None removes fills/persistent borders while focus and Current dot remain usable. Check larger color samples and conditional thin edges in Light/Dark/OLED; grey vs black distinct.
- Logo fill 50/100/110/160 changes within unchanged boxes/Dock height. Focus magnification and indicators stay inside shape safety margins.
- Dock behavior: all three options same toggle style; focus/hover changes only the relevant help; rapid double Confirm never activates Back.
- Add-app catalogue fades on first load; quick paging/search during load must not produce a stale fade or flash.
- Appearance: consistent heading, theme-only first row, three behavior toggles second, Accent Colors unchanged, App icon style visual samples below. Check native fonts at enlarged system font sizes.
- About cup/logo and visible exact SilasDV destinations. Main Start/Select badge sizes match, Start+gear share one background, divider subtle. Existing input/close/Shizuku checks below remain applicable.

# 2.1 physical acceptance

- Inline Dock controls: Swap sides reachable by D-pad/stick and touch; compact size buttons do not overlap. Verify low-height pane scrolling and portrait focus.
- Source preview cards unselected, Current green; background swatches have contrasting outlines in Light/Dark/OLED.
- Round/Square/Squircle masks and matching highlights; Original/B&W/Accent/Glass and Follow accent color. Compare actual native icon detail/edges, including adaptive icons.
- Icon size 110 to 160 visibly grows, while box/focus fits and running/current dots remain visible. Minimum box size may expand to fit icons.
- Main task icon looks; protected/current lock badges above icons with matching gaps, including dense layouts.
- Start opens Settings (does not close Activity), hint visible; existing Start mappings migrate. Setup Start/chord semantics unchanged. Back returns to the originating setting button.
- Known transient close failure gets one extra attempt after 50 ms, then feedback; no retry after uncertain transport, protection change, interruption or deadline. Validate true Shizuku/firmware results with disposable app state.

# 2.0 release acceptance

- Thumb corners: two preview-count pairs; Floating/Shelf: one, with correct add/remove limits.
- Size and Icon size persist independently at 50/100/160%; logo focus remains inside its box. Upgrade from a signed 1.x build retains settings and initializes previous icon scale once.
- Dock appearance: background colors, selected/focused swatches, round/square legacy/adaptive icons and faithful bottom preview. Check white/OLED contrast and native edge rendering.
- Controller: all size-stepper directions, background grid to shape choices, Back returning to Dock style; no clipped controls in compact landscape or portrait.
- Repeat the existing setup, Shizuku, task-close, notification, Current-origin and drag/reorder acceptance below. Automated layout/state checks do not certify native RP6 graphics/firmware.

# 1.1.12 physical acceptance

- Add apps to Dock: compare first/later/backward page transitions with setup protection; confirm search and controller focus remain usable.
- Size-control icon stays inside its border; Appearance themes/close vibration have room on RP6 and portrait.
- Preview count controls show exactly one plus/minus and still add/remove on the correct side.

Native RP6 visual acceptance remains pending.

# 1.1.11 physical acceptance

- Compare first and later protection transitions; check slower motion, no initial jump/flash and stable focus during rapid paging.
- Compact Previous/Finish controls, labeled L1/R1 page hints and written Start/Options badge; narrow-screen fit.
- Dock style labels have one left start and more side padding; Swap sides has a swap icon.
- Recognizable PlayStation/store/GitHub marks, Record icons, circular redo arrow and memory-chip RAM icon.

Native RP6 visual/animation acceptance remains pending.

# 1.1.10 physical acceptance

- Setup themes Down reaches Stay open after clearing, then Vibrate on close; both On/Off actions readable and touch/controller operated.
- Protection: D-pad at row edges, L1/R1 app pages and touch swipe. A swipe must not toggle a lock. Edge hints, large footer frames, B back and Start finish must agree with actual mappings.
- Check narrow portrait footer fit and landscape focus visibility.
- Search/unlock/D-pad/theme/About/GitHub/redo/Dock/style/behavior/shortcut/Shizuku icon meaning and native contrast. Connected Shizuku retains its green check.
- Press Y once during task refresh: bulk action should run after list arrival and exit when configured. Partial failures still exit with an error message; Stay open On keeps the deck. No destructive automatic retry.
- Confirm real RP6 firmware, touch/swipe feel, Shizuku connection, notifications and haptics.

These device checks remain pending; simulations are not hardware certification.

# 1.1.9 physical acceptance

- Both pickers: centered icon/name groups, comfortable bottom padding and equal row/column border gaps; check native fonts and larger system text.
- Always show Dock checkbox: first touch and Confirm; off means on-navigation behavior, not transparency.
- About actions align vertically and fit without scrolling.
- Setup download links and adjacent permission reasons; verify external-page returns.
- D-pad/stick Right into Accent Colors, Left back to the originating theme/control.
- Protection: only setup Previous/Finish use L1/R1; app arrows page separately.
- Three vertical styles, Thumb corners first; old Compact becomes Floating. Saved preview apps retain order/sides before alphabetical extras.

Physical checks remain pending; simulation is not native RP6 certification.

# 1.1.8 targeted physical acceptance

- Five-page fresh/redo setup, permissions first, no nested capture. Denied shortcut access disables recording; granting enables it after return.
- Record both chords, release, checkmarks, rotate/reopen, then test actual shortcuts. Check cancellation, conflicts and reserved navigation.
- Eight picker icons in two rows of four: visibility while pressed/focused, search, paging, Clear Dock, right icon strip and named toast. No page scrolling.
- Equal style tiles; visibility slider by touch/controller; independent left/right preview counts and odd-count Swap sides. Preview must not change saved apps.
- Fit all transitions after closing: no stale hit targets or confirmed banner. Respect animations-disabled setting.
- Six one-row hints including Select/Share; B and PlayStation glyphs white.
- Vibration off causes no pulse; on requests a short light pulse after confirmed closing. Verify the RP6 motor physically.
- Ordinary launchers unlocked by default; explicit locks survive upgrades. Required services remain protected.
- Official Shizuku links, live permission states and slow-draw/short-hold completion.

These physical checks are pending; simulation is not hardware certification.

# 1.1.7 targeted acceptance

- With Orange accent, navigate all sixteen swatch boxes: focused border must be orange, distinct from the filled selected box. Check close row/heading spacing and no Reset. Appearance/About headings remain visible.
- At 100/80/70/60/50%, compare Thumb corner box dimensions, icon centering, hover clearance and task space; boxes must progressively shrink.
- Choose all dock styles directly using touch, D-pad and stick; no style/theme popup. Dock follows the app theme.
- Swap sides with 1, 2, 3 and 4 favorites: entire groups move, internal order persists, RAM enters the right group and launching selects the correct package. Drag, Move left/right and remove must work after swapping.
- Check preview and actual dock match; touch-drag guidance remains readable on compact landscape.

# 1.1.6 targeted acceptance

- Visit right Thumb dock → RAM → Tasks → Down using D-pad and stick: leftmost favorite must be selected. RAM Down/Right must still enter the right group.
- Appearance: sixteen larger colors in complete rows; selected inner ring and focused outer ring remain distinguishable. Reset restores theme accent; Current remains green. Check portrait reflow.
- Mapping, Shortcuts, Dock Behavior and Appearance: full labels at normal and larger Android font sizes; equal group widths, padding, no clipped text. Check native RP6 font/insets.
- First-launch Setup: connection stays incomplete until genuinely connected; optional controller/access steps say Skip for now. Choose PlayStation labels before Cross calibration. Check two-by-two themes and L1/R1/D-pad/stick routes.
- Return from Android permission screens without granting, then with granting: completion must reflect actual state. Completion animation holds before deck, with optional Confirm skip.
- Disconnect/revoke Shizuku and remove a favorite app: friendly recovery summary, no replayed destructive request.

These are pending physical checks, not results claimed from simulation.

# Version 1.1.5 acceptance additions

- RAM Details Down/Right selects right Thumb corner; Up returns to RAM Details. Empty/one-app docks retain accessible fallback.
- Appearance has inline color dots, no color names/rectangular swatch backgrounds, recognizable selected/focus rings. Check portrait reflow, controller movement and touch targets.
- Picker icons remain visible at short landscape heights with system bars/search keyboard and stay within tile borders beside their names.
- Last action Previous/Next align left/right; controller bindings still work.
- Menu buttons have matching, limited widths per pane and full readable labels: Appearance, Mapping, Shortcuts, Dock Behavior, Notifications. Light/Dark/OLED/Auto have more horizontal padding.

---

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

## Local 2.3 follow-up

Dock style groups Dock / Background and Icons on one screen. Under None, Background Shape is hidden and its previous value retained. Check restored focus, captions, compact spacing, centered icons and running indicators on RP6; verify palette-to-icon-style navigation with D-pad and stick. Automated layout checks passed, but physical acceptance is still required.

## Local 2.4 UX follow-up

Dock settings use the available panel height, with equal-height groups, captioned neutral background forms and matching percentage steppers. A saved choice is filled with a tick; focus is outlined independently. None and Theme have distinct symbols and focused colors reveal a name. The live preview uses neutral app states and never saves sample-count changes. Back carries its mapped controller badge. On RP6, inspect focus versus saved selection in Light/Dark/OLED, color edges, caption readability, compact targets, maximum Dock size, panel fit and actual preview alignment. Compact targets do not establish a universal 48-dp touch-target guarantee; physical acceptance remains open.

## Local 2.5 — split Dock settings

Settings > Dock now has Add apps to Dock, Dock / Background, Icons and Dock behavior. The two customization menus retain their own edits and return focus to their originating Settings entry. Each has the actual-size preview at the bottom, bounded two-panel forms in landscape and stacked forms in portrait. Check both menus at maximum Dock/icon sizes on RP6, including D-pad/stick access to every control and Back, None hiding the shape section, focus name versus saved color, and live preview updates without changing favorites. Wider Settings/setup consistency proposals remain deferred in docs/UX-CONSISTENCY.md.

## Local 2.6

All Back actions use the mapped controller badge and common compact dimensions; wizard Previous/Next and paging keep their distinct semantics. Preview count controls have no native lift and reserve drawing clearance. All icon-look examples reserve two caption lines and share full artwork size. On RP6 inspect pressed/focused preview buttons, all Back badges with remapped/PlayStation controls, equal example artwork and compact Appearance palette readability. Source-review findings outside these fixes are documented in docs/UX-REVIEW-2.6.md and remain proposals.

## Local 2.7 acceptance

The nine UX-REVIEW-2.6 follow-ups are implemented, with the user's six/eight-app Dock limits replacing unrestricted growth. Check zero-result search entirely by gamepad; disabled shortcut recording and recovery access; remapped/PlayStation card hints; memory age and independent refresh without losing selection; named failed-close feedback; favorite removal, clear and trim Undo (including expiry and rotation); full Dock addition limits; and readable picker rail overflow for legacy saved extras.

Check native DocumentsUI export/import on RP6, reviewing every imported setting before Apply, cancellation while reading, and unchanged Android grants. Import resets omitted customization to defaults. Check 130% and 200% system fonts and reduced motion. Automated tests use fake IPC/document streams and do not establish physical controller mapping, Shizuku outcomes, haptic/notification delivery or native rendering.
