# Changelog

## 0.3.0-prototype

- Separate touch/controller modes: first tap activates; explicit rounded controller focus on setup controls and sort choices.
- Four setup screens with Next/Previous and actual connection, saved mapping, Accessibility and notification status. Controller navigation/Confirm/Start remain usable during macro recording.
- Notification shortcut can be hidden again; permission/channel blocking is visible. RAM notifications have one stateful enable/disable action.
- Sort by Recent, RAM or Alphabetical. Manual locks always sort last; mandatory protected infrastructure is hidden. Empty or entirely locked decks show “Nothing to close”.
- New openings select the first visible card. Refreshes retain the current selection where possible.
- Y closes eligible tasks and exits to HOME; R1 closes others and returns to the app identified on entry, retaining all of that package’s tasks. Both handle empty lists. A setting keeps the deck open instead; operation errors remain visible.
- Removed the blanket system-app and foreign-top-activity exclusions. Targeted launcher, IME, core-service and manual protection still apply. This changes TaskDeck policy, not Android’s system gesture provider.
- Colored scalable action vectors, compact landscape cards and wrapped narrow-screen controls.
- UserService version 3 loads the changed backend on upgrade. No new permissions.

## 0.2.0-prototype

- Replaced native list focus with explicit app-card selection for D-pad and left-stick input. A opens the selected task, X closes it immediately, Y closes eligible tasks without a confirmation dialog.
- Horizontal cards, compact landscape layout and distinct Light/Dark/OLED palettes.
- First-run setup, connection and controller-recording screens replace setup dialogs.
- Long-press or Select opens a protection popup. A locks/unlocks the app, B dismisses. Persistent user locks are checked by the privileged backend and shown with vector lock icons.
- HOME launchers and Wave Launcher are protected. Android Settings is closable unless manually locked.
- Multi-button shortcut recording shows held/captured names and scan codes. Save with Start after releasing the buttons. Partial combinations pass through, complete chords trigger once; legacy single-button shortcuts are disabled on upgrade.
- Best-effort per-app PSS, explicit unavailable/shared states and optional notifications showing the measured available-RAM delta after closing. No guaranteed RAM-reclamation or performance claim.
- Shizuku UserService version increased so existing connections load the new backend.

## 0.1.0-prototype

Initial local Shizuku task reader, switcher and task remover with a Quick Settings tile and single-key shortcut. User feedback exposed broken controller selection, cramped vertical layout and intrusive setup/close dialogs; these are the main 0.2 changes.
