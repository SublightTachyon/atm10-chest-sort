# ATM10 Chest Sort

A small client-side NeoForge mod for All the Mods 10 (Minecraft 1.21.1). It adds a compact control panel to chest-style container screens with:

- **Sort Chest** and **Sort Inventory** — combine partial stacks, then sort items by display name and registry ID. Inventory sorting leaves all nine hotbar slots untouched.
- **Merge → Chest/Inventory** — move only item types already present at the destination.
- **All → Chest** — move the main inventory first. Once it is empty, press again to move the hotbar.
- **All → Inventory** — quick-move every chest stack that the normal menu permits.

The mod sends ordinary vanilla inventory clicks. It therefore works in single-player and can be used on a multiplayer server without installing it on the server.

## Install

1. Build with `./gradlew build`, or use the JAR from `build/libs/`.
2. Put `atm10_chest_sort-1.0.2.jar` in the ATM10 instance's `mods` folder.
3. Keep the mod on the client only.

The project targets Minecraft 1.21.1, Java 21, and NeoForge 21.1.x.

## Compatibility and safety

Buttons appear only when the screen exposes a player inventory plus a regular chest-width storage grid (nine or more columns). This avoids treating crafting and machine input/output screens as chests. Transfer actions use the menu's own quick-move handling, so slot filters and locks remain in effect.

Sorting needs an empty cursor. If you are carrying an item with the mouse, put it down before pressing a sort button.
