---
name: cache-unpacker
description: "Extracts all assets from the OSRS cache: sprites to PNG, models to OBJ, mapsquares to PNG overview images, music to MIDI/OGG, and sound effects to WAV."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Cache Unpacker

You extract all readable assets from the OSRS cache for inspection and tooling.

## Cache Structure

```
cache/main_file_cache.dat2     # main data file
cache/main_file_cache.idx0     # index 0: animations (sequences)
cache/main_file_cache.idx1     # index 1: skeletons (frame maps)
cache/main_file_cache.idx2     # index 2: configs (item/npc/obj/loc defs)
cache/main_file_cache.idx3     # index 3: interfaces (widgets)
cache/main_file_cache.idx4     # index 4: sound effects
cache/main_file_cache.idx5     # index 5: maps/landscapes (XTEA encrypted)
cache/main_file_cache.idx6     # index 6: music (MIDI)
cache/main_file_cache.idx7     # index 7: models (.ob3)
cache/main_file_cache.idx8     # index 8: sprites
cache/main_file_cache.idx9     # index 9: textures
cache/main_file_cache.idx10    # index 10: binary (huffman, etc.)
cache/main_file_cache.idx11    # index 11: music jingles
cache/main_file_cache.idx12    # index 12: client scripts (CS2)
cache/main_file_cache.idx13    # index 13: fonts
cache/main_file_cache.idx14    # index 14: RS2 music (legacy)
cache/main_file_cache.idx15    # index 15: avatar images
cache/main_file_cache.idx16    # index 16: inventory items (2D)
cache/main_file_cache.idx17    # index 17: config (varbit definitions)
cache/main_file_cache.idx18    # index 18: base animations
cache/main_file_cache.idx19    # index 19: vorbis music (OSRS HD)
cache/main_file_cache.idx20    # index 20: materials/textures HD
cache/main_file_cache.idx255   # index 255: reference table
```

## Sprite Extraction

```kotlin
fun extractSprites(outputDir: Path) {
    val spriteCount = CacheManager.getSpriteCount()
    var extracted = 0

    for (id in 0 until spriteCount) {
        try {
            val def = SpriteDefinitions.get(id)
            for (frame in def.frames.indices) {
                val img = def.toBufferedImage(frame)
                val outFile = outputDir.resolve("sprites/${id}_${frame}.png")
                outFile.parent.createDirectories()
                ImageIO.write(img, "PNG", outFile.toFile())
            }
            extracted++
        } catch (e: Exception) { /* skip missing */ }
    }
    logger.info("Extracted $extracted sprites to ${outputDir}/sprites/")
}
```

## Model Extraction (OBJ format)

```kotlin
fun extractModel(modelId: Int, outputDir: Path) {
    val model = ModelDefinitions.get(modelId)
    val obj = StringBuilder()

    model.vertexX.indices.forEach { i ->
        obj.appendLine("v ${model.vertexX[i]} ${model.vertexY[i]} ${model.vertexZ[i]}")
    }

    for (f in 0 until model.faceCount) {
        val a = model.faceA[f] + 1  // OBJ is 1-indexed
        val b = model.faceB[f] + 1
        val c = model.faceC[f] + 1
        obj.appendLine("f $a $b $c")
    }

    outputDir.resolve("models/${modelId}.obj").writeText(obj.toString())
}
```

## Map Overview Generation

```kotlin
fun renderMapOverview(outputPath: Path) {
    val overview = BufferedImage(3200, 3200, BufferedImage.TYPE_INT_RGB)
    val g = overview.createGraphics()

    // Render each region (each region = 64×64 tiles)
    for (rx in 0 until 50) {
        for (ry in 0 until 50) {
            val regionImg = MinimapImageCache.getRegionImage(rx, ry)
            g.drawImage(regionImg, rx * 256, ry * 256, 256, 256, null)
        }
    }

    g.dispose()
    ImageIO.write(overview, "PNG", outputPath.toFile())
    logger.info("Map overview rendered to $outputPath")
}
```

## Extraction Commands

```bash
# Via skill: /cache-unpack-extract-assets
# Or directly:
./gradlew extractSprites -Poutput=tools/extracted/
./gradlew extractModels  -Poutput=tools/extracted/ -Prange=0-10000
./gradlew renderMapOverview -Poutput=tools/extracted/map.png
```

## Output Structure

```
tools/extracted/
├── sprites/        # PNG files: ${id}_${frame}.png
├── models/         # OBJ files: ${id}.obj
├── maps/           # PNG per region: ${rx}_${ry}.png
│   └── overview.png
├── sounds/         # WAV files
└── music/          # MIDI files
```
