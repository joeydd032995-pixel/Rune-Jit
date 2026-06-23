---
name: cache-unpack-extract-assets
description: "Extracts all visual and audio assets from the OSRS cache: sprites to PNG, models to OBJ, mapsquares to overview PNG, sounds to WAV, and music to MIDI. Useful for inspection and tooling."
argument-hint: "[type: sprites|models|maps|sounds|music|all]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /cache-unpack-extract-assets [type]

Extracts assets from the OSRS cache for inspection and tooling.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Cache downloaded | Yes |
| XTEA keys present | Yes (for maps) |
| `tools/extracted/` in .gitignore | Yes |
| Sufficient disk space (~5GB for all) | Yes |

## Phase 2: Setup Output Directory

```bash
mkdir -p tools/extracted/{sprites,models,maps,sounds,music}
echo "tools/extracted/" >> .gitignore  # ensure extracted assets not committed
```

## Phase 3: Asset Extraction

Spawn `cache-unpacker` based on argument:

| Type | Cache Index | Output | Count |
|------|------------|--------|-------|
| sprites | 8 | `tools/extracted/sprites/*.png` | ~6000 |
| models | 7 | `tools/extracted/models/*.obj` | ~60000 |
| maps | 5 | `tools/extracted/maps/*.png` | ~1000 regions |
| sounds | 4 | `tools/extracted/sounds/*.wav` | ~4000 |
| music | 6 | `tools/extracted/music/*.midi` | ~600 |
| all | 0-21 | All above | ~70000+ files |

## Phase 4: Map Overview Generation

If `type == maps` or `type == all`:
- Spawn `cache-unpacker` to render full world map overview
- Output: `tools/extracted/maps/overview.png` (~6400×6400px)
- Shows entire OSRS world from bird's eye

## Phase 5: Generate Asset Index

Write `tools/extracted/ASSET-INDEX.md`:
- Count per type
- File size breakdown
- Sample list of key sprites/models (inventory backgrounds, player model IDs)

## Phase 6: Validation

Spot-check key assets exist:
```bash
ls tools/extracted/sprites/178*.png  # stats background sprite
ls tools/extracted/models/23*.obj    # player male base model
ls tools/extracted/maps/49_50.png    # Lumbridge region
```

## Error Recovery

| Error | Recovery |
|-------|---------|
| XTEA key missing for region | Skip region; mark as missing in index |
| Model parse error | Skip model; log ID for investigation |
| Disk full | Extract in batches; use `--range` flag |
| Too slow | Run in background: `nohup ./gradlew extractAll &` |

## Nuances

- `tools/extracted/` must be gitignored — extracted assets can be multi-GB
- Models are in OSRS format (.ob3); OBJ export is for inspection only
- Map images use palette-based colors matching OSRS minimap colors
- Some sprites are sprite sheets (multiple frames per ID)
- Music tracks are MIDI with a custom soundfont — plain playback sounds different from OSRS

## Next Steps

After extraction:
- Use map overview for region identification and NPC placement verification
- Use sprite extraction to verify UI sprite IDs
- Use model extraction to verify NPC/object model IDs
