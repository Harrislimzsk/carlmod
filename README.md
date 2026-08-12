# Carl Mod — Stage 1

Fabric mod for Minecraft 1.20.4 / Java 17.

## Build

Requires a JDK 17. Fabric Loom will download the rest on first run:

```
./gradlew build
```

(No Gradle wrapper jar is included in this drop — run `gradle wrapper` once with a
local Gradle 8.x install, or open the folder in IntelliJ IDEA with the Fabric/Loom
plugin, which will generate the wrapper for you.)

Double-check `gradle.properties` against the current values on
https://fabricmc.net/develop/ before building — Yarn mappings, Loader and Fabric API
versions are occasionally revised even for the same Minecraft version.

## What's implemented in Stage 1

- **Big Mouth Staff** (`carlmod:big_mouth_staff`)
  - Shaped recipe: Nether Star / Heart of the Sea / Stick (top to bottom).
  - Right-click a `CarlEntityMarker` entity → teleport to the Carl Dimension,
    generate a 3x3 bedrock landing platform, and remember the player's origin
    dimension + coordinates via `CarlTeleportState` (a `PersistentState`, saved to disk).
  - Right-click empty air while inside the Carl Dimension → teleport back to the
    remembered origin.
- **Wild Carl** (`carlmod:wild_carl`)
  - 0.1 movement speed, `isAttackable() == false` → immune to being targeted by any
    other mob's AI.
  - Every 10 ticks, scans a 20x20x20 box centered on itself and `discard()`s every
    `LivingEntity` inside except players, non-living entities, and any Carl-type
    entity — no hurt sound, no death animation.
  - Each erased entity drops one randomly enchanted equipment piece or enchanted
    book, enchantment level 15-30.

## Known stub / TODO for later stages

- `ModDimensions.CARL_DIMENSION_KEY` is declared but the dimension itself (dimension
  type json, dimension json, chunk generator, `CarlTeleporter`) is not registered yet
  — `server.getWorld(...)` returns `null` until that stage lands, and the staff
  already handles that case with a chat message instead of crashing.
- No models/textures/blockstates are included yet (`assets/carlmod/models`,
  `assets/carlmod/textures`) — the staff and Wild Carl will render as the missing
  texture placeholder until those are added.
- `TameableCarlEntity`, taming logic, melee counter-AI, and natural spawn placement
  rules (Plains-only for Wild Carl weight 1; Carl-Dimension-only for Tameable Carl
  weight 10) are planned for the next stages.
