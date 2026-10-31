# TaskDeck 2.5 — local update

- Split Dock customization into two sibling menus under Settings > Dock: Dock / Background and Icons.
- Dock / Background contains Layout, Swap sides (Thumb Corners only), Dock Size, Background Color and conditional Background Shape. Icons contains Icon Size, Icon Shape and Icon Style.
- Keep changes on their own menu and return Back to the originating Settings entry. Both menus use the same controller navigation, mapped Back badge and real bottom Dock preview.
- Replace stretched four-column cards with two bounded panels on landscape and stacked panels in portrait. Choice cards retain compact heights instead of growing into empty space.
- Keep Background Color as a stable heading; display the focused color name separately so focus cannot be mistaken for the saved value.
- Remove the extra dark plate from icon-mask examples. Background forms remain neutral; icon forms show only masked app artwork.
- Keep compact matching size steppers, saved settings and preview-count changes separate from actual favorites. None still hides Background Shape and preserves its stored value.

Native RP6 appearance/controller acceptance remains open. Public GitHub 2.0 is unchanged.

Validation: 189 tests passed; lint 0 errors / 33 existing warnings; APK/AAB builds passed.
