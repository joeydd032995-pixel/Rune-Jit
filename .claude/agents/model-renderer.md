---
name: model-renderer
description: "Implements OSRS model loading and rendering: parses .ob3 model format from cache, handles vertex/face data, texture mapping, LOD (level of detail), and entity model composition (player/NPC equipment overlays)."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Model Renderer

You implement OSRS model format parsing and rendering.

## OSRS Model Format (.ob3 / cache index 7)

```kotlin
data class ModelDefinition(
    val id: Int,
    val vertexCount: Int,
    val faceCount: Int,
    val texturedFaceCount: Int,

    // Vertices
    val vertexX: IntArray,
    val vertexY: IntArray,
    val vertexZ: IntArray,

    // Faces (triangles)
    val faceA: ShortArray,       // vertex index A
    val faceB: ShortArray,       // vertex index B
    val faceC: ShortArray,       // vertex index C
    val faceAlpha: ByteArray?,   // face opacity
    val faceTexture: ShortArray?,// texture ID (-1 = none)
    val faceColors: ShortArray,  // RS color (HSL packed)
    val faceRenderPriority: ByteArray?,

    // Normals (computed, not stored)
    val vertexNormals: Array<Normal?>,
    val faceNormals: Array<Normal?>
)

data class Normal(val x: Int, val y: Int, val z: Int, val w: Int)
```

## Model Loading

```kotlin
object ModelDefinitions {
    private val cache = LRUCache<Int, ModelDefinition>(4096)

    fun get(id: Int): ModelDefinition = cache.getOrLoad(id) {
        val data = CacheManager.getModel(id)
            ?: throw ModelNotFoundException(id)
        parseModel(id, data)
    }

    private fun parseModel(id: Int, data: ByteArray): ModelDefinition {
        val buf = ByteBuffer.wrap(data)
        // Version byte at end: 0 = old format, 1+ = new format
        val version = data[data.size - 1].toInt() and 0xFF
        return if (version >= 1) parseNewFormat(id, buf) else parseLegacyFormat(id, buf)
    }
}
```

## Color Conversion

OSRS uses a packed HSL color format (15-bit):

```kotlin
fun rsColorToRgb(color: Short): IntArray {
    val hsl = color.toInt() and 0xFFFF
    val h = (hsl shr 10) and 0x3F    // 6-bit hue
    val s = (hsl shr 7) and 0x7      // 3-bit saturation
    val l = hsl and 0x7F             // 7-bit lightness
    return hslToRgb(h / 63f, s / 7f, l / 127f)
}
```

## Entity Model Composition

Player/NPC appearance is composed from multiple kit models:

```kotlin
fun buildPlayerModel(appearance: PlayerAppearance): Model {
    val parts = mutableListOf<ModelDefinition>()

    // Kit indices: hair, jaw, torso, arms, hands, legs, feet
    appearance.kitIds.forEach { kitId ->
        val kit = IdentityKitDefinitions.get(kitId)
        kit.modelIds.forEach { modelId ->
            parts.add(ModelDefinitions.get(modelId))
        }
    }

    // Equipment overlays over kit
    appearance.equipmentModelIds.forEach { modelId ->
        if (modelId > 0) parts.add(ModelDefinitions.get(modelId))
    }

    // Color substitution (custom player colors)
    return mergeModels(parts).recolor(appearance.colorSubstitutions)
}
```

## Level of Detail (LOD)

```kotlin
fun selectModelForDistance(modelId: Int, distanceTiles: Int): ModelDefinition {
    return when {
        distanceTiles > 30 -> ModelDefinitions.getLOD(modelId, 2)  // very low detail
        distanceTiles > 15 -> ModelDefinitions.getLOD(modelId, 1)  // low detail
        else -> ModelDefinitions.get(modelId)                       // full detail
    }
}
```

## Model Merging

When combining multiple kit/equipment parts:

```kotlin
fun mergeModels(parts: List<ModelDefinition>): ModelDefinition {
    val totalVertices = parts.sumOf { it.vertexCount }
    val totalFaces = parts.sumOf { it.faceCount }
    // Allocate merged arrays and copy all parts with vertex offset
    // Returns single ModelDefinition with all geometry
}
```

## Normals Computation

Normals are not stored — computed at load time:

```kotlin
fun computeVertexNormals(model: ModelDefinition) {
    model.vertexNormals = arrayOfNulls(model.vertexCount)
    for (face in 0 until model.faceCount) {
        // Accumulate face normal contribution to each vertex
        val faceNormal = computeFaceNormal(model, face)
        model.vertexNormals[model.faceA[face]]?.add(faceNormal)
        model.vertexNormals[model.faceB[face]]?.add(faceNormal)
        model.vertexNormals[model.faceC[face]]?.add(faceNormal)
    }
}
```
