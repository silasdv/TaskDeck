# TaskDeck 2.7 — local update

- Derive task-card Close hints from the active mapping and vector button labels, including PlayStation and swapped profiles.
- Empty app searches give D-pad Down an explicit Clear search target, and clearing returns controller focus to results. Missing shortcut access disables recording and offers an Enable shortcut access action. Disconnection feedback points to Refresh/reconnect instead of silently ignoring actions.
- Show per-app RAM sample age, loading/unavailable states and a stale marker after 30 seconds. RAM details can refresh the memory sample without re-reading tasks or changing selected task identity.
- Add a 10-second inline Undo for removing/clearing Dock favorites; restore exact order, layout and side preference. Undo is controller-accessible and survives rotation during its active window.
- Limit new Dock favorites and preview counts to six for Thumb Corners and eight for Floating/Shelf. Retain existing over-limit saved lists without silent deletion, show a count/limit and offer Trim to limit with Undo. The Dock renders only its layout limit; the picker preserves access to saved extras.
- Keep picker rail icons readable with fixed-height rows and bounded overflow instead of shrinking every favorite to a tiny fraction of the strip.
- Failed/unverified closes identify affected apps in feedback; remaining app descriptions expose Retry close using the mapped button. Keep positive close overlays and the removed Last action menu out of the main flow.
- Respect disabled system animations for setup completion. Drawn captions scale with the system font, with larger visual-choice bounds/reflow. At font scales above 125%, affected form content can scroll while navigation remains outside it; normal-size layouts remain unchanged.
- Add Settings backup under About: local Android document-picker export/import, versioned JSON schema and strict allow-list/type/range/size checks. Preview imported values with mapped page navigation, app names and printed controller labels before Apply import. Omitted customization returns to defaults; setup completion and Android permission grants are not imported. Canceled/stale reads cannot reintroduce an abandoned import.

No new network or storage permissions. Physical RP6 controller/haptic/notification/rendering and native document-provider acceptance remain open. GitHub 2.0 assets are unchanged.

Validation: 216 tests passed; lint 0 errors / 33 existing warnings; release APK/AAB passed.
