---
name: pathfinding-and-clipping-engine
description: "Implements OSRS-accurate BFS pathfinding, loads clip flags from the cache, handles door/gate dynamic obstacles, and validates player/NPC movement against walkability data."
argument-hint: ""
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /pathfinding-and-clipping-engine

Implements OSRS-accurate pathfinding and movement validation.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Cache downloaded | Yes (clip data in index 5) |
| XTEA keys present | Yes (needed to decrypt landscape files) |
| World region loader | Recommended first |

## Phase 2: Load Clip Flags from Cache

Spawn `world-region-loader` to:
- Decrypt and load all landscape files (index 5) using XTEA keys
- Parse tile attributes → clip flags
- Build `CollisionMap[regionX][regionY][plane][localX][localZ]`

Clip flag bitmask (from cache):
```kotlin
const val BLOCK_NORTH      = 0x1
const val BLOCK_EAST       = 0x2
const val BLOCK_SOUTH      = 0x4
const val BLOCK_WEST       = 0x8
const val BLOCK_NE         = 0x10
const val BLOCK_SE         = 0x20
const val BLOCK_SW         = 0x40
const val BLOCK_NW         = 0x80
const val BLOCK_ALL        = 0x100  // solid tile
const val BLOCK_OBJECT     = 0x200  // game object blocking
```

## Phase 3: Implement BFS Pathfinding

Spawn `pathfinding-engineer` to implement `src/server/pathfinding/BreadthFirstSearch.kt`:

**Critical**: OSRS uses BFS, NOT A*. Using A* produces different paths and is a parity failure.

```kotlin
// BFS contract:
// - Max path length: 104 tiles (one region)
// - Check clip flags at each tile
// - Diagonal movement allowed ONLY if both cardinal sides are walkable
// - Stops at closest reachable tile if target unreachable
```

## Phase 4: Door/Gate Handling

Dynamic objects (doors, gates) modify clip flags at runtime:

```kotlin
class DoorHandler {
    fun openDoor(doorObjectId: Int, doorTile: Tile) {
        val def = ObjectDefinitions.get(doorObjectId)
        // Remove clip flags for door tiles
        collisionMap.removeBlockFlag(doorTile, def.orientation)
        // Schedule auto-close after 3 ticks if needed
    }
}
```

## Phase 5: NPC Pathfinding

NPCs use the same BFS but with different starting conditions:
- Follow range: max 10 tiles from spawn point (configurable per NPC)
- Wander: random BFS within `wanderRadius` tiles
- Combat follow: BFS toward target, re-evaluated every tick

## Phase 6: Parity Tests

```kotlin
// tests/parity/pathfinding/
@Test fun testBasicWalk() {
    // Walk from (3222, 3218) to (3225, 3218): should find 3-step path
}
@Test fun testWallBlocking() {
    // Walk through wall: should route around
}
@Test fun testDiagonalRestriction() {
    // Cannot walk diagonally through a corner
}
@Test fun testMaxPathLength() {
    // Path >104 tiles: returns partial path to closest reachable tile
}
```

## Phase 7: Performance Gate

BFS must complete in <5ms for any path up to 64 tiles.
For NPCs, batch pathfinding across all NPCs must stay within tick budget.

## Error Recovery

| Error | Recovery |
|-------|---------|
| XTEA keys missing for region | Mark region as empty/walkable (fallback) |
| Clip flag parse error | Log warning; use empty collision map for region |
| Path not found | Return walk-in-place result (player taps same tile) |

## Nuances

- Diagonal steps check BOTH sides: diagonal NE requires North AND East to be clear
- Doors are objects with clip flag removal — not separate tile type
- Agility shortcuts are handled by AgilityPlugin, not pathfinding
- Swimming/sailing uses different movement (not standard pathfinding)
- Multi-level (floors): stairs/ladders are teleport actions, not pathfinding

## Next Steps

1. Run `/combat-engine-full` (needs pathfinding for NPC movement)
2. Run `/implement-skill-action-framework woodcutting` (needs walking to trees)
