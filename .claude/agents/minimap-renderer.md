---
name: minimap-renderer
description: "Implements the OSRS minimap: renders region overview from cache map data, displays player/NPC/item dots, applies rotation with compass, handles zoom levels, and draws the circular minimap overlay."
model: haiku
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Minimap Renderer

You implement the OSRS minimap display system.

## Minimap Architecture

The minimap shows a top-down view of the surrounding area:
- Rendered from precomputed map images (cache index 5, map files)
- Rotated to match camera yaw
- Player dot always at center
- NPC/player/item dots drawn over map

## Map Image Extraction

```kotlin
object MinimapImageCache {
    // regionX + regionY → BufferedImage (64x64 tiles, each tile = 4px = 256x256 px)
    private val regionImages = HashMap<Int, BufferedImage>()

    fun getRegionImage(regionX: Int, regionY: Int): BufferedImage {
        val key = regionX or (regionY shl 8)
        return regionImages.getOrPut(key) {
            loadRegionMapImage(regionX, regionY)
        }
    }

    private fun loadRegionMapImage(rx: Int, ry: Int): BufferedImage {
        // Cache index 5 map files: m${rx}_${ry} and l${rx}_${ry}
        val mapData = CacheManager.getMapFile("m${rx}_${ry}")
            ?: return createBlankRegion()
        return renderRegionToImage(mapData)
    }
}
```

## Minimap Rendering

```kotlin
class MinimapRenderer {
    val MINIMAP_SIZE = 146  // diameter in pixels (circular)
    val TILE_SIZE = 4       // pixels per tile on minimap

    fun render(graphics: Graphics2D, player: LocalPlayer, camera: Camera) {
        // 1. Set circular clip
        graphics.clip = Ellipse2D.Float(
            minimapX.toFloat(), minimapY.toFloat(),
            MINIMAP_SIZE.toFloat(), MINIMAP_SIZE.toFloat()
        )

        // 2. Draw rotated map
        val g = graphics.create() as Graphics2D
        val centerX = minimapX + MINIMAP_SIZE / 2
        val centerY = minimapY + MINIMAP_SIZE / 2
        g.rotate(-Math.toRadians(camera.yaw * 360.0 / 2048.0), centerX.toDouble(), centerY.toDouble())

        val playerTileX = player.worldX / 128
        val playerTileY = player.worldZ / 128
        drawMapTiles(g, playerTileX, playerTileY, centerX, centerY)

        // 3. Draw entities (in screen space, not rotated)
        g.dispose()
        drawEntityDots(graphics, player, camera, centerX, centerY)

        // 4. Draw minimap frame overlay
        SpriteRenderer.draw(graphics, 1005, minimapX, minimapY) // frame sprite
        drawCompass(graphics, camera.yaw)
    }
}
```

## Entity Dots

```kotlin
fun drawEntityDots(g: Graphics2D, player: LocalPlayer, camera: Camera, cx: Int, cy: Int) {
    val yawRad = camera.yaw * (2 * Math.PI / 2048)

    // NPCs
    npcList.filterNotNull().forEach { npc ->
        if (npc.definition?.minimapVisible != false) {
            val dot = worldToMinimapScreen(npc.worldX, npc.worldZ, player, cx, cy, yawRad)
            dot?.let {
                g.color = Color(255, 255, 0)  // yellow for NPCs
                g.fillOval(it.x - 2, it.y - 2, 4, 4)
            }
        }
    }

    // Other players (white dots)
    playerList.filterNotNull().filter { it != player }.forEach { p ->
        val dot = worldToMinimapScreen(p.worldX, p.worldZ, player, cx, cy, yawRad)
        dot?.let {
            g.color = Color.WHITE
            g.fillOval(it.x - 2, it.y - 2, 4, 4)
        }
    }

    // Player dot (always centered, white)
    g.color = Color.WHITE
    g.fillOval(cx - 2, cy - 2, 5, 5)
}

fun worldToMinimapScreen(worldX: Int, worldZ: Int, player: LocalPlayer,
                          cx: Int, cy: Int, yawRad: Double): Point? {
    val dx = (worldX - player.worldX) * TILE_SIZE / 128
    val dy = (worldZ - player.worldZ) * TILE_SIZE / 128
    val rotX = (dx * cos(yawRad) - dy * sin(yawRad)).toInt()
    val rotY = (dx * sin(yawRad) + dy * cos(yawRad)).toInt()
    val sx = cx + rotX
    val sy = cy - rotY
    if ((sx - cx).let { it * it } + (sy - cy).let { it * it } > (MINIMAP_SIZE / 2) * (MINIMAP_SIZE / 2)) return null
    return Point(sx, sy)
}
```

## Compass

Draws compass arrow at top-right of minimap, rotated opposite of camera yaw.
Sprite ID 2458 = compass needle, drawn at `(minimapX + 129, minimapY + 8)`.
