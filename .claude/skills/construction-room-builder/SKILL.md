---
name: construction-room-builder
description: "Implements the OSRS Construction skill: player-owned houses, room placement, furniture hotspots, servant system, dungeon building, and the Costume Room/Trophy Room/Combat Room mechanics."
argument-hint: "[scope: rooms|furniture|servants|dungeons|all]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /construction-room-builder [scope]

Implements the OSRS Construction skill and player-owned houses.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| `data/osrsbox/items/` populated | Yes — item definitions |
| World region loader working | Yes — house is an instanced region |
| Tick engine implemented | Yes |
| Cache downloaded | Yes — house tile definitions |

Read `design/gdd/construction-gdd.md` if it exists. Spawn `/gdd-osrs-specialized-framework construction` to create it if missing.

## Phase 2: House Data Definitions

Spawn `skill-decomposer` to define all Construction data from the wiki:

```yaml
# data/construction/rooms.yaml
rooms:
  - id: garden
    name: "Garden"
    level_required: 1
    build_cost: 1000
    size_tiles: 8x8
    max_per_house: 4
    outdoor: true
    hotspots:
      - type: CENTREPIECE
        furniture_ids: [1, 2, 3]  # Decorative rock, small plant, etc.
      - type: PLANT_1
        furniture_ids: [10, 11]
      - type: PLANT_2
        furniture_ids: [12, 13]

  - id: parlour
    name: "Parlour"
    level_required: 1
    build_cost: 1000
    size_tiles: 8x8
    outdoor: false
    hotspots:
      - type: CHAIR_HOTSPOT
        furniture_ids: [100, 101, 102]  # Crude chair, Wood chair, etc.
      - type: TABLE_HOTSPOT
        furniture_ids: [200, 201]
      - type: FIREPLACE
        furniture_ids: [300, 301, 302]
```

Key room types and level requirements:

| Room | Level | Cost | Notes |
|------|-------|------|-------|
| Garden | 1 | 1,000 | Outdoor, entrance |
| Parlour | 1 | 1,000 | First indoor room |
| Kitchen | 5 | 5,000 | Cooking range hotspot |
| Bedroom | 20 | 10,000 | Bed for regen, servant summon |
| Achievement Gallery | 80 | 250,000 | Tier-based displays |
| Portal Chamber | 50 | 100,000 | Teleport portals |
| Dungeon | 70 | 7,500 | Clue scroll puzzles, combat |
| Throne Room | 60 | 150,000 | Treasure chest, throne |

## Phase 3: House Instance System

Spawn `world-region-loader` to implement player house instancing:

```kotlin
class HouseInstance(
    val owner: String,           // player name
    val buildMode: Boolean,      // build mode vs play mode
    val layout: HouseLayout,
    val region: InstancedRegion
) {
    companion object {
        const val HOUSE_REGION_BASE_X = 7744  // instanced house base coordinate
        const val TILES_PER_ROOM = 8
        const val MAX_ROOMS = 8  // 8x8 grid
    }
}

data class HouseLayout(
    val rooms: Array<Array<PlacedRoom?>> = Array(8) { arrayOfNulls(8) },
    val portalExit: WorldPoint = WorldPoint(3203, 3424, 0)  // Rimmington default
)
```

House region allocation follows the same X += 6400 offset as raids (from `InstanceManager`).

## Phase 4: Room Placement Mechanics

Spawn `item-system-engineer` to implement room building:

```kotlin
fun buildRoom(player: Player, house: HouseInstance, gridX: Int, gridY: Int, roomType: RoomType) {
    require(player.skills.getLevel(Skill.CONSTRUCTION) >= roomType.levelRequired)
    require(player.inventory.hasCoins(roomType.buildCost))
    require(house.layout.canPlace(gridX, gridY, roomType))  // adjacency check

    player.inventory.removeCoins(roomType.buildCost)
    house.layout.placeRoom(gridX, gridY, PlacedRoom(roomType, rotation = 0))
    player.skills.addXp(Skill.CONSTRUCTION, roomType.xpReward)

    // Reload house region to show new room
    reloadHouseRegion(house)
}
```

Room rotation: rooms can be placed at 0°, 90°, 180°, 270° — affects which sides connect to corridors/other rooms.

## Phase 5: Furniture Hotspot System

Spawn `item-system-engineer` to implement furniture building within rooms:

```kotlin
data class FurnitureDefinition(
    val id: Int,
    val name: String,
    val levelRequired: Int,
    val xpReward: Double,
    val materials: List<MaterialRequirement>   // e.g. 4x oak planks + 1x bolt of cloth
)

data class MaterialRequirement(
    val itemId: Int,
    val quantity: Int
)

fun buildFurniture(player: Player, hotspot: Hotspot, furnitureDef: FurnitureDefinition) {
    require(player.skills.getLevel(Skill.CONSTRUCTION) >= furnitureDef.levelRequired)
    require(player.inventory.containsAll(furnitureDef.materials))

    furnitureDef.materials.forEach { mat ->
        player.inventory.remove(mat.itemId, mat.quantity)
    }
    player.skills.addXp(Skill.CONSTRUCTION, furnitureDef.xpReward)
    hotspot.setFurniture(furnitureDef.id)
    refreshRoom(hotspot.room)
}
```

## Phase 6: Servant System

Spawn `npc-behavior-simulator` for butler/demon butler functionality:

| Servant | Level | Wage | Fetch Speed | Notes |
|---------|-------|------|-------------|-------|
| Servant | 20 | 500 coins | 4 trips/bank run | Rimmington |
| Cook | 30 | 1,000 coins | 5 trips | Draynor |
| Gardener | 50 | 3,000 coins | 6 trips | |
| Butler | 50 | 5,000 coins | 8 trips | |
| Demon Butler | 58 | 10,000 coins | 8 trips, fastest | |

```kotlin
class Servant(val servantType: ServantType) {
    fun fetchItems(player: Player, itemId: Int, quantity: Int) {
        // Teleport to bank, retrieve items, return after servantType.travelTicks ticks
        scheduleReturn(player, servantType.travelTicks) {
            player.inventory.addItems(itemId, quantity)
        }
    }
}
```

## Phase 7: Dungeon Implementation

Spawn `world-region-loader` for dungeon rooms (underground floor):

- Dungeons are on floor level 0 but rendered below the house
- Dungeon rooms connect via staircases
- Treasure rooms (clue scroll puzzles) use dungeon room type
- Oubliette room can contain NPCs (guard dog, dungeon demon)

## Phase 8: Parity Tests

Spawn `content-parity-verifier` to validate:

| Mechanic | Expected | Wiki Source |
|----------|----------|------------|
| Oak plank XP | 60 XP | wiki/Construction#Experience_table |
| Mahogany table XP | 840 XP | wiki/Mahogany_table |
| House teleport | Teleports to house portal | wiki/Teleport_to_House |
| Servant fetch delay | 8 ticks (demon butler) | wiki/Servants |
| Room removal refund | 50% of build cost | wiki/Construction#Removing_rooms |

## Error Recovery

| Error | Recovery |
|-------|---------|
| House region collision | Increase instance offset; check InstanceManager allocation |
| Furniture objects not rendering | Check cache object IDs against room definition |
| Servant not returning | Verify tick scheduler for delayed NPC actions |
| Room won't connect | Check adjacency rules — rooms need valid doorway alignment |

## Nuances

- House region uses special tile format — not same as overworld regions
- Servants require the player to have a bedroom with a servant's moneybag
- Some furniture (lecterns, altars) provides services (enchanting, prayer restore)
- Build mode vs play mode changes NPC spawns (servants present in play mode)
- Portal Chamber requires separate portal tablets (itemIds) per destination
- Costume Room, Achievement Gallery have special display logic for examining items

## Next Steps

1. Run `/verify-mechanic-parity-1to1 construction` to score parity
2. Integrate with `/raids-and-instanced-content-generator` — shares instance framework
3. Implement Mahogany Homes minigame (uses same furniture system, different rewards)
