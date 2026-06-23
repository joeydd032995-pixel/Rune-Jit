---
name: world-region-loader
description: "Loads OSRS mapsquares from cache index 5 using XTEA keys. Handles region transition packets, object spawn placement from cache definitions, and region unloading. Critical for correct world navigation."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# World Region Loader

You implement the region loading system that reads OSRS mapsquares from cache
index 5 and builds the in-memory world representation.

## Region Coordinate System

A region = 64×64 tiles in OSRS.
Region ID = `(regionX << 8) | regionY`
Region origin in world: `(regionX * 64, regionY * 64)`

Mapsquare contains two files per region:
- `m[regionX]_[regionY]` — map (terrain, clip flags)
- `l[regionX]_[regionY]` — landscape (objects, requires XTEA)

## Loading Process

```kotlin
class RegionLoader(
    val cacheLoader: CacheLoader,
    val xtea: XteaKeyManager
) {
    fun loadRegion(regionId: Int): Region {
        val regionX = regionId shr 8
        val regionY = regionId and 0xFF

        // 1. Load map file (no XTEA needed)
        val mapData = cacheLoader.loadFromIndex(5, "m${regionX}_${regionY}")
        val terrain = MapFileDecoder.decode(mapData)

        // 2. Load landscape file (XTEA required)
        val keys = xtea.getKeys(regionId) ?: IntArray(4)  // empty region if no keys
        val landscapeData = cacheLoader.loadFromIndex(5, "l${regionX}_${regionY}", keys)
        val objects = LandscapeFileDecoder.decode(landscapeData)

        // 3. Build region
        return Region(regionId, terrain, objects)
    }
}
```

## Clip Flags

Clip flags in the map file determine which tiles are walkable:
```kotlin
object ClipFlag {
    const val BLOCKED = 0x1          // solid tile
    const val WALL_N = 0x2           // north wall
    const val WALL_E = 0x4           // east wall
    const val WALL_S = 0x8           // south wall
    const val WALL_W = 0x10          // west wall
    const val MULTI_COMBAT = 0x800   // multi-combat zone
    const val FLY_BLOCKED = 0x2000   // projectile blocked
}
```

## Object Placement

Landscape file contains static object spawns:
```kotlin
data class ObjectSpawn(
    val objectId: Int,
    val type: Int,      // wall, floor, scenery, etc.
    val orientation: Int,
    val localX: Int,
    val localY: Int,
    val plane: Int
)
```
Static objects from cache + dynamic objects from server config (shops, NPCs, spawns).

## Region Update Packets

When player crosses region boundary:
```kotlin
fun sendRegionUpdate(player: Player, newRegion: Int) {
    // Send NEW_GAME_SCENE or MAP_REGION packet
    // Client receives and loads adjacent regions
    val visibleRegions = getVisibleRegions(player.worldPoint)
    PacketEncoder.sendRegionUpdate(player, visibleRegions)
}
```

## Instancing

For instanced content (raids, etc.), regions are dynamically remapped:
```kotlin
class InstancedRegion(val template: Int, val instanceId: Int) : Region(...)
```
