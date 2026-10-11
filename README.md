# New Dimension — Fabric for Minecraft Java 26.2

This is a **starter source project**, not a compiled or tested `.jar`. It establishes the mod project and documents the intended command/config design. Dynamic dimension registration, safe portal creation, and per-dimension inventory/gamemode enforcement need to be implemented and tested against the exact 26.2 mappings before this can be considered a finished playable mod.

## Intended command

```text
/newdimension create <id> <surface_block> <portal_frame_block> <gamemode> <shared|separate>
```

Example:
```text
/newdimension create moon minecraft:stone minecraft:crying_obsidian survival separate
```

- `id`: lowercase dimension name, e.g. `moon`
- `surface_block`: block ID used as the primary terrain surface
- `portal_frame_block`: block required for the physical portal frame
- `gamemode`: `survival`, `creative`, `adventure`, or `spectator`
- inventory: `shared` or `separate`

## What is and isn't implemented here

Included:
- Fabric mod metadata for Minecraft 26.2
- Java entrypoint scaffold
- Build configuration scaffold
- command specification and implementation plan

Not yet implemented:
- Runtime registration/loading of generated dimension definitions
- Surface-block substitution in vanilla biome terrain generation
- Portal frame scanning, ignition/activation, and two-way travel
- Persistent per-dimension inventory snapshots
- Enforcing a configured gamemode on dimension entry
- A built/tested release `.jar`

These are core gameplay systems, not just command syntax. This source starter must not be treated as a working mod yet.

## Build environment

Minecraft 26.2's Fabric toolchain uses a recent Java/Gradle/Loom setup. Check the official current versions before building:
- https://docs.fabricmc.net/develop/
- https://fabricmc.net/develop/

Once the implementation is completed, run `./gradlew build` (Windows: `gradlew.bat build`) and install the **remapped** JAR from `build/libs/` into the `mods` folder alongside the matching Fabric Loader and Fabric API.

Back up any world before testing mods that change dimensions.
