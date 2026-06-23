---
name: implement-client-rendering-pipeline
description: "Implements the full OSRS client rendering pipeline: model loading, scene rendering, sprite batching, minimap, animations, and HD plugin toggle."
argument-hint: ""
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: opus
---

# /implement-client-rendering-pipeline

Implements the complete OSRS client rendering system.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Cache downloaded | Yes (models, sprites, textures) |
| RuneLite fork set up | Yes |
| LWJGL/OpenGL available | Yes (for HD mode) |
| Gamepack loaded (hooks working) | Yes |

## Phase 2: Model Loading Pipeline

Spawn `model-renderer` to implement:
- `ModelDefinition` parsing from cache index 7
- LRU cache for loaded models (4096 entries)
- Entity composition (player kit + equipment models merged)
- Color substitution for custom player colors

## Phase 3: Software Renderer (Default)

Spawn `rendering-pipeline-engineer` to implement `src/client/render/SoftwareRenderer.kt`:
- CPU-based rasterization for tiles, models, and sprites
- Z-buffer depth sorting
- Draw distance culling (25-tile default)
- Fog implementation (linear depth fade)

## Phase 4: Animation System

Spawn `animation-system-engineer` to implement:
- `SequenceDefinition` loading from cache index 0
- `FrameDefinition` loading from skeleton cache
- Per-entity animation state machine
- Walk/run/idle/combat animation transitions
- Animation blending for interleaved animations

## Phase 5: Sprite Rendering System

Spawn `sprite-renderer` to implement:
- Sprite extraction from cache index 8
- Sprite batching system
- HiDPI scaling support
- Alpha compositing (keyed and blended)

## Phase 6: Minimap

Spawn `minimap-renderer` to implement:
- Region map image loading from cache
- Camera-yaw-rotated map display
- Player/NPC/item dots
- Compass orientation indicator

## Phase 7: HD Plugin (Optional, requires OpenGL)

Spawn `hd-graphics-handler` to implement:
- OpenGL 4.1 scene shader pipeline
- Shadow mapping
- Anisotropic texture filtering
- Runtime toggle (no restart required)

## Phase 8: Integration Test

Spawn `integration-tester` for rendering validation:
- Verify models load and display for standard player
- Verify animation plays when walking
- Verify minimap updates as player moves
- Verify HD toggle switches renderer without crash

## Error Recovery

| Error | Recovery |
|-------|---------|
| Model ID not found | Render fallback (invisible/placeholder) |
| OpenGL init failure | Fall back to software renderer |
| Cache not loaded | Show error splash screen |

## Nuances

- OSRS uses a custom 15-bit RSColor format for model colors; must convert to RGB
- Model normals are not stored; must be computed from face data
- The OSRS software renderer uses integer-only arithmetic (no floats)
- HD plugin requires LWJGL; software renderer requires only AWT/Java2D
- Entity highlights (hovering) use additive blending on top of base colors

## Next Steps

1. Run `/paoloka-interface-integrator` (widget rendering)
2. Run `/protocol-packet-engine` (connect client to server)
3. Run `/verify-mechanic-parity-1to1 all` (full parity check)
