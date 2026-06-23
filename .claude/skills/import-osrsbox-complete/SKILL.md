---
name: import-osrsbox-complete
description: "Downloads and imports the complete osrsbox-db dataset (23k+ items, 5k+ NPCs, prayers) into the server data layer. Validates data integrity and generates Kotlin data files."
argument-hint: ""
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /import-osrsbox-complete

Imports the complete osrsbox-db dataset into the Rune-Jit server data layer.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Python 3.10+ available | Yes |
| pip/pipx available | Yes |
| data/osrsbox/ in .gitignore | Yes |
| Internet access to PyPI/GitHub | Yes |

## Phase 2: Download osrsbox-db

```bash
# Install osrsbox Python library
pip install osrsbox

# Or fetch JSON directly from GitHub
mkdir -p data/osrsbox
curl -L "https://github.com/osrsbox/osrsbox-db/raw/master/docs/items-complete.json" \
     -o data/osrsbox/items-complete.json
curl -L "https://github.com/osrsbox/osrsbox-db/raw/master/docs/monsters-complete.json" \
     -o data/osrsbox/monsters-complete.json
```

## Phase 3: Validate Raw Data

Spawn `data-schema-engineer` to validate:
- `items-complete.json` has ≥ 20,000 entries
- `monsters-complete.json` has ≥ 4,000 entries
- No malformed JSON (schema validation)

## Phase 4: Import to Server Data Layer

Spawn `content-importer`:
- Import all items → `ItemDefinitions`
- Import all NPCs → `NpcDefinitions`
- Import prayers → `PrayerDefinitions`
- Report: X items, Y NPCs, Z prayers imported

## Phase 5: Generate Kotlin Constants

Spawn `data-schema-engineer` to generate:
- `src/server/data/ItemIds.kt` — item ID constants (e.g., `ABYSSAL_WHIP = 4151`)
- `src/server/data/NpcIds.kt` — NPC ID constants
- `src/server/data/ObjectIds.kt` — object ID constants

## Phase 6: Validation

```kotlin
// Verify key items exist
assert(ItemDefinitions.get(ItemIds.ABYSSAL_WHIP).name == "Abyssal whip")
assert(ItemDefinitions.get(ItemIds.DRAGON_BONES).highalch == 720)
assert(NpcDefinitions.get(NpcIds.COW).hitpoints == 8)
```

## Phase 7: Session State Update

Write `production/session-state/import-status.yaml`:
```yaml
osrsbox_imported: true
import_date: YYYY-MM-DD
items_count: 23000
npcs_count: 5000
```

## Error Recovery

| Error | Recovery |
|-------|---------|
| Network failure | Retry 3x with 5s backoff |
| GitHub rate limit | Use PyPI release instead |
| Malformed JSON | Report line number, skip bad entries |
| Item count < 20k | Warn: may be incomplete dataset |

## Nuances

- osrsbox-db is community-maintained and may lag behind latest OSRS revision by 1-2 weeks
- Some items in osrsbox have `null` values for optional fields — handle gracefully
- `data/osrsbox/` is gitignored; every developer must run this command on fresh checkout
- Do not use osrsbox for cache-resident data (sprites, models) — use cache directly

## Next Steps

After import:
1. Run `/implement-tick-engine-core` to start server core
2. Run `/implement-skill-action-framework woodcutting` for first skill
