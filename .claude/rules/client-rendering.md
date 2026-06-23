---
path: src/client/render/**
---

# Client Rendering Rules

## No Blocking I/O on Render Thread

The render thread runs at 50 FPS. Any I/O (file reads, cache access) must be done on a background thread and results cached:

```kotlin
// ❌ WRONG: Blocking file read during render
fun drawModel(modelId: Int) {
    val data = File("cache/models/$modelId.ob3").readBytes()  // BLOCKS render thread
    renderModel(parseModel(data))
}

// ✅ CORRECT: Pre-loaded via background thread
fun drawModel(modelId: Int) {
    val model = ModelDefinitions.getOrNull(modelId) ?: return  // already loaded
    renderModel(model)
}
```

## HD Toggle Runtime Switchable

The HD plugin (GPU rendering) must toggle at runtime without a restart:

```kotlin
// Toggle must complete in under 100ms
fun toggleHd() {
    renderPlugin.shutdown()           // release GPU resources
    renderPlugin = if (hdEnabled) GpuRenderPlugin() else SoftwareRenderPlugin()
    renderPlugin.startup(canvas)      // reinitialize
}
```

## Render State Isolation

Rendering code must not modify game state. Read game state; never write it from the render thread:

```kotlin
// ❌ WRONG: Modifying game state from render
fun renderPlayer(player: Player) {
    player.lastRenderedTick = currentTick  // NEVER modify state from render
    drawModel(player.model)
}

// ✅ CORRECT: Read-only access
fun renderPlayer(player: Player) {
    drawModel(player.model)
}
```

## Draw Distance Consistency

Software and HD modes must use the same tile draw distance (default 25 tiles). HD can optionally extend to 90 tiles with extended draw distance toggle.

## Prohibited

- No game logic in rendering code
- No blocking file/network I/O on the render thread
- No direct OpenGL calls outside `src/client/render/gpu/`
- No storing rendering state in game entities
