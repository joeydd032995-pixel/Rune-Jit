# Movement & Pathfinding GDD

**Status**: DRAFT
**Phase**: 2 — Server Core
**Primary Wiki**: https://oldschool.runescape.wiki/w/Walking
**Algorithm Source**: rsmod community research — https://github.com/rsmod/rsmod

---

## 1. Mechanic Overview

Every player and NPC movement in OSRS is tile-based. When a player clicks a destination, the
server runs a BFS (breadth-first search) pathfinder across the world's clip-flag collision map
to produce a sequence of tiles. The player then advances one tile per tick while walking (0.6 s
per tile) or two tiles per tick while running. The client mirrors this movement via its own
client-side prediction; the server is authoritative.

Collision is managed by per-tile bit-flags (RuneLite `CollisionDataFlag` values) that encode
walls, objects, and impassable floors. Doors and gates dynamically add/remove these flags when
toggled open or closed.

Source: https://oldschool.runescape.wiki/w/Walking

---

## 2. XP / Reward Formula

Movement grants **no XP**. There is no experience reward for walking or running.

Run energy drains while running and regenerates while walking/standing. Run energy is
influenced by the Agility level but is not an XP-granting system.
Source: https://oldschool.runescape.wiki/w/Run_energy
**Deferred**: run energy drain/regen requires Agility skill integration.

---

## 3. Content List

### Directions

| Direction  | dx | dy | Diagonal |
|------------|----|----|----------|
| NORTH      | 0  | +1 | No       |
| EAST       | +1 | 0  | No       |
| SOUTH      | 0  | -1 | No       |
| WEST       | -1 | 0  | No       |
| NORTH_EAST | +1 | +1 | Yes      |
| SOUTH_EAST | +1 | -1 | Yes      |
| SOUTH_WEST | -1 | -1 | Yes      |
| NORTH_WEST | -1 | +1 | Yes      |

OSRS coordinate system: x increases EAST, y increases NORTH.
Source: https://oldschool.runescape.wiki/w/Coordinates

### Clip Flag Constants (RuneLite CollisionDataFlag parity)

| Constant                  | Value    | Meaning                                    |
|---------------------------|----------|--------------------------------------------|
| BLOCK_MOVEMENT_NORTH_WEST | 0x1      | Wall on NW face of tile                    |
| BLOCK_MOVEMENT_NORTH      | 0x2      | Wall on N face                             |
| BLOCK_MOVEMENT_NORTH_EAST | 0x4      | Wall on NE face                            |
| BLOCK_MOVEMENT_EAST       | 0x8      | Wall on E face                             |
| BLOCK_MOVEMENT_SOUTH_EAST | 0x10     | Wall on SE face                            |
| BLOCK_MOVEMENT_SOUTH      | 0x20     | Wall on S face                             |
| BLOCK_MOVEMENT_SOUTH_WEST | 0x40     | Wall on SW face                            |
| BLOCK_MOVEMENT_WEST       | 0x80     | Wall on W face                             |
| BLOCK_MOVEMENT_OBJECT     | 0x100    | Solid object occupies tile                 |
| BLOCK_MOVEMENT_FLOOR_DEC  | 0x40000  | Floor decoration (decorative, not always blocking) |
| BLOCK_MOVEMENT_FLOOR      | 0x200000 | Impassable floor tile                      |
| BLOCK_MOVEMENT_FULL       | OR above | Aggregate fully-impassable                 |

Source: https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/CollisionDataFlag.java

### Region Geometry

- World map is divided into 64×64 tile regions.
- `regionId = (regionX shl 8) or regionY` where `regionX = worldX shr 6`, `regionY = worldY shr 6`.
- Planes 0–3 (ground through third floor).
- Source: https://oldschool.runescape.wiki/w/Coordinates

---

## 4. Level Requirements

| Action       | Level Required | Notes                                                      |
|--------------|----------------|------------------------------------------------------------|
| Walking      | None           | All players can walk from level 1                          |
| Running      | None (energy)  | Running depletes run energy; Agility affects regen — deferred |
| Diagonal walk| None           | Diagonal movement is permitted with same clip rules        |

Source: https://oldschool.runescape.wiki/w/Walking

---

## 5. Required Items / Tools

Walking requires no items or tools.

Running requires sufficient run energy (depleted while running; regenerates while walking).
**Deferred**: run energy system requires Agility skill implementation.
Source: https://oldschool.runescape.wiki/w/Run_energy

---

## 6. Tick Rate

| Movement Mode | Tiles per Tick | Real Time per Tile | Status      |
|---------------|---------------|--------------------|-------------|
| Walking       | 1             | 600ms              | Implemented |
| Running       | 2             | 300ms (per tile)   | Deferred    |
| Diagonal      | 1             | 600ms (same cost)  | Implemented |

Walk = 1 tile per 600ms tick. Diagonal is NOT penalised — it costs 1 tile-tick like a cardinal.
Source: https://oldschool.runescape.wiki/w/Walking
Source: https://oldschool.runescape.wiki/w/Game_tick

---

## 7. Special Mechanics

### Corner-Cutting Forbidden

Diagonal moves require BOTH cardinal components to be unblocked. A diagonal step that would
"cut a corner" around a wall is refused; the BFS routes via two cardinal steps instead.
Source: https://oldschool.runescape.wiki/w/Walking (diagonal movement rules)

### Doors as Clip Toggles

Doors and gates are represented as wall clip flags on a tile. Opening a door removes the
directional flag; closing it restores it. This allows the pathfinder to route through an
open doorway and be blocked by a closed one without any special door logic in BFS.
Source: rsmod door handling — https://github.com/rsmod/rsmod

**Deferred**: real door object ID → tile/wall mapping requires ObjectDefinitions (cache index 2).
Auto-close after 3 ticks requires a tick-queue timer per door.

### Partial Path (Closest Reachable Tile)

When a destination is unreachable, BFS returns the closest reachable tile (minimum Chebyshev
distance to target), tie-broken by minimum BFS distance from start. `reachedTarget = false`.
Source: rsmod BFS reference — https://github.com/rsmod/rsmod

### 104-Tile Path Clamp

OSRS limits path length to 104 tiles. Longer reconstructed paths are truncated;
`reachedTarget` is set to `false` even if the target was nominally reachable beyond 104 steps.
Source: https://oldschool.runescape.wiki/w/Walking (path length)

### New Click Replaces Path

Receiving a new walk destination while moving replaces the in-progress path entirely.
`MovementQueue.walkTo()` clears the step queue before loading new directions.
Source: https://oldschool.runescape.wiki/w/Walking

### Large NPC Footprint

NPCs with `size > 1` (e.g., 2×2 Kalphite Queen, 5×5 Corporeal Beast) require BFS to check
all leading-edge tiles of their footprint for each directional step. The `BreadthFirstSearch`
accepts a `size` parameter for this purpose.
Source: https://oldschool.runescape.wiki/w/Kalphite_Queen (size 2 footprint)

### Planes Are Teleports

BFS operates on a single plane. Moving between planes (stairs, ladders) is handled as a
teleport action that changes `player.plane`, not as a BFS diagonal. Multi-plane pathing
is deferred pending stair/ladder action implementation.
Source: https://oldschool.runescape.wiki/w/Staircase

---

## 8. Parity Target

**90%** — Required for server deployment (matching global weighted parity gate).

Key parity items:
- Clip flag semantics must match RuneLite `CollisionDataFlag` exactly.
- BFS neighbour expansion order (`NORTH, EAST, SOUTH, WEST, NE, SE, SW, NW`) must be fixed.
- 104-tile clamp must match OSRS exactly.
- Corner-cutting diagonal refusal must match the game.

Full parity requires real terrain clip loading from cache index 5 (landscape files), which
is deferred until `/load-osrs-cache-full` populates the cache.

Source: https://oldschool.runescape.wiki/w/Walking

---

## 9. Edge Cases

| Edge Case | Behaviour | Status |
|-----------|-----------|--------|
| Same-tile click | Returns `Path.EMPTY`, no movement | Handled |
| Target on blocked tile | Partial path to closest reachable | Handled |
| Start fully enclosed | Returns `Path.EMPTY` (walk-in-place) | Handled |
| Target > 104 tiles | Path truncated to 104, `reachedTarget=false` | Handled |
| Diagonal corner-cut attempt | Refused; routes via two cardinals or around | Handled |
| New click while moving | Previous path cleared, new path loaded | Handled |
| Open door mid-path | Clip flag removed; next findPath() uses new state | Handled |
| Target on far side of closed door | Routes around; direct path opens after door toggle | Handled |
| Large NPC (size > 1) | Leading-edge footprint tiles checked per direction | Handled |
| Multi-plane movement (stairs) | Not BFS; handled as teleport action — deferred | Deferred |
| Run energy exhausted mid-run | Drops to walk speed (1 tile/tick) — deferred | Deferred |
| Stun / knockback | `movement.reset()` cancels queue | Handled |

---

## 10. Wiki Citation

Primary sources:
- https://oldschool.runescape.wiki/w/Walking — main movement mechanics
- https://oldschool.runescape.wiki/w/Run — running and run energy
- https://oldschool.runescape.wiki/w/Run_energy — energy drain/regen formula
- https://oldschool.runescape.wiki/w/Game_tick — 600ms tick rate
- https://oldschool.runescape.wiki/w/Coordinates — world coordinate system, region grid

Algorithm reference (open source):
- https://github.com/rsmod/rsmod — rsmod BFS implementation
- https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/CollisionDataFlag.java — clip flag constants
