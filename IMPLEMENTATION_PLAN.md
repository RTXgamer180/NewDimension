# Implementation checklist

The following pieces are required before this becomes a complete mod:

1. **Command parsing**
   - Register `/newdimension create`.
   - Parse a resource identifier, block registry entries, gamemode enum, and inventory mode.
   - Validate duplicate IDs and reject air/non-solid portal-frame blocks.

2. **Dimension definitions**
   - Write valid dimension type, dimension, biome source, and noise settings data for 26.2.
   - Use a noise-based Overworld-like generator with the chosen block as the top surface.
   - Add generated data to the world's datapack resources and trigger the appropriate reload/restart workflow.

3. **Portals**
   - Detect a complete rectangular frame of the configured block.
   - Activate the portal only when the interior is clear.
   - Save portal pairs with persistent IDs and link each side to the other.
   - Create a safe landing location and handle returning to the Overworld.

4. **Gamemode**
   - Persist the selected mode per dimension.
   - Apply it when a player enters, while deciding and documenting how to restore the previous mode on exit.

5. **Inventory**
   - `shared`: keep the normal player inventory.
   - `separate`: save and restore inventory, armor, offhand, and selected hotbar slot per player and dimension.
   - Handle death, disconnect/reconnect, dimension changes, and server restarts.

6. **Validation**
   - Test in a throwaway world on Minecraft Java 26.2 with the exact Fabric Loader/API versions.
   - Test single-player and dedicated-server behavior.
   - Do not distribute as a finished `.jar` until build and gameplay tests pass.
