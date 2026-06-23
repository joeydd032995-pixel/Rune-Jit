---
name: sprite-renderer
description: "Implements 2D sprite extraction from cache index 8, sprite batching for UI rendering, HiDPI/retina display scaling, and transparency/alpha compositing."
model: haiku
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Sprite Renderer

You implement sprite loading and 2D rendering for the OSRS client UI.

## Sprite Format (Cache Index 8)

```kotlin
data class SpriteDefinition(
    val id: Int,
    val frame: Int,         // sprite sheet frame index
    val width: Int,
    val height: Int,
    val offsetX: Int,       // offset within original canvas
    val offsetY: Int,
    val maxWidth: Int,      // original canvas width
    val maxHeight: Int,     // original canvas height
    val pixels: IntArray    // ARGB pixels (width * height)
)

object SpriteDefinitions {
    fun get(id: Int, frame: Int = 0): SpriteDefinition {
        val data = CacheManager.getSprite(id, frame)
            ?: throw SpriteNotFoundException(id, frame)
        return parseSprite(data)
    }

    private fun parseSprite(data: ByteArray): SpriteDefinition {
        val buf = ByteBuffer.wrap(data)
        // Parse OSRS sprite format:
        // header: maxWidth(2), maxHeight(2), paletteSize(1)
        // per-frame: offsetX(1), offsetY(1), width(2), height(2), flags(1)
        // pixel data: run-length encoded or raw depending on flags
    }
}
```

## Sprite Batching

```kotlin
class SpriteBatch {
    private val entries = ArrayList<SpriteEntry>(256)

    data class SpriteEntry(
        val spriteId: Int, val frame: Int,
        val x: Int, val y: Int,
        val width: Int, val height: Int,
        val alpha: Int  // 0-255
    )

    fun add(spriteId: Int, x: Int, y: Int, alpha: Int = 255) {
        val def = SpriteDefinitions.get(spriteId)
        entries.add(SpriteEntry(spriteId, 0, x + def.offsetX, y + def.offsetY,
                                def.width, def.height, alpha))
    }

    fun flush(graphics: Graphics2D) {
        entries.sortBy { it.spriteId }  // sort for texture locality
        entries.forEach { e ->
            val img = SpriteCache.getImage(e.spriteId, e.frame)
            val composite = if (e.alpha < 255) {
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, e.alpha / 255f)
            } else AlphaComposite.SrcOver
            graphics.composite = composite
            graphics.drawImage(img, e.x, e.y, e.width, e.height, null)
        }
        entries.clear()
    }
}
```

## HiDPI Scaling

```kotlin
object HiDPIScaling {
    val scaleFactor: Double by lazy {
        val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        gd.defaultConfiguration.defaultTransform.scaleX
    }

    fun scaleSprite(sprite: SpriteDefinition): BufferedImage {
        if (scaleFactor == 1.0) return sprite.toBufferedImage()
        val scaled = BufferedImage(
            (sprite.width * scaleFactor).toInt(),
            (sprite.height * scaleFactor).toInt(),
            BufferedImage.TYPE_INT_ARGB
        )
        val g = scaled.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
        g.scale(scaleFactor, scaleFactor)
        g.drawImage(sprite.toBufferedImage(), 0, 0, null)
        g.dispose()
        return scaled
    }
}
```

## Alpha Compositing

OSRS uses two transparency modes:
- **Type 0**: Fully opaque
- **Type 1**: Alpha-keyed (color 0 = transparent)
- **Type 2**: Alpha-blended (per-pixel alpha channel)

```kotlin
fun SpriteDefinition.toBufferedImage(): BufferedImage {
    val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    for (i in pixels.indices) {
        val pixel = pixels[i]
        // Type 1: treat black (0x000000) as transparent
        val argb = if (pixel == 0) 0x00000000 else (0xFF000000.toInt() or pixel)
        img.setRGB(i % width, i / width, argb)
    }
    return img
}
```

## Sprite ID Reference

Key UI sprites:
```
173   = inventory background
178   = stats background
638   = prayer list background
1005  = minimap frame
2458  = compass
```
