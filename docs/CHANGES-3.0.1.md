# TaskDeck 3.0.1 — dock launch recovery

Updating an app can leave its previous task/activity in Android Recents. The dock previously preferred that task and stopped on failure; restarting TaskDeck could hide the problem by refreshing state.

The dock now observes package changes for the activity lifetime, invalidates the affected icon and resolves a fresh launcher for the affected package. Updates received while TaskDeck is paused are applied when returning. If an update event was missed and reopening a recent task fails, it falls back to the package's current launcher. Successful recent restoration remains unchanged. A missing or blocked launcher leaves the shortcut/order intact and the menu available.

220 automated tests pass; lint has no errors and 33 warnings. Signing identity and permissions remain unchanged. Real RP6 update-cycle verification is still open. This is a local update, not a new GitHub publication.
