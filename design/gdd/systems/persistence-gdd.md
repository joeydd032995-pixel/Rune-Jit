# Player Persistence GDD

## 1. Mechanic Overview

The player persistence system saves and loads player state between sessions. On each save, the complete player snapshot (position, all 23 skill XP values, prayer points, inventory slots, and equipment slots) is serialised to JSON and atomically written to `data/players/[username].json`. On login, the file is loaded, schema-migrated if necessary, and deserialised onto the player entity before the player enters the world.

## 2. XP/Reward Formula

No XP formula — persistence is infrastructure, not a skill. All XP values are stored and restored as exact doubles with no transformation. The maximum XP cap per skill is 200,000,000 (enforced by `SkillSet.addXp`).

Source: https://oldschool.runescape.wiki/w/Experience

## 3. Content List

| Field | Type | Description | Slot Count |
|-------|------|-------------|------------|
| `schemaVersion` | Int | Schema version for migration | — |
| `username` | String | Player account name | — |
| `x` | Int | World tile X coordinate | — |
| `y` | Int | World tile Y coordinate | — |
| `plane` | Int | Floor level (0=ground, 1–3=upper) | — |
| `skillXp` | Map<String, Double> | XP per skill, keyed by Skill.name | 23 skills |
| `prayerPoints` | Int | Current prayer points at time of save | — |
| `inventory` | List<SavedItem?> | Item stacks by slot; null=empty | 28 slots |
| `equipment` | List<Int> | Item ID per slot; -1=empty | 14 slots |
| `currentHp` | Int | Hitpoints at save time (informational) | — |

## 4. Level Requirements

No level requirement — persistence applies to all players at all levels.

## 5. Required Items/Tools

No items or tools required for the persistence system itself.

- Save directory: `data/players/` (created automatically by `PlayerStore.save()`)
- Temporary file: `data/players/[username].json.tmp` (atomic write intermediary)
- Final file: `data/players/[username].json`

## 6. Tick Rate

Auto-save fires every **200 ticks** (200 × 600ms = 120 seconds), matching the OSRS Constants `SAVE_TIMER = 120`.

The save itself is executed asynchronously off the main tick thread to avoid blocking the tick budget.

Source: 2006Scape Constants.SAVE_TIMER = 120 (server-persistence.md)

## 7. Special Mechanics

### Atomic Write Protocol

Saves are written atomically to prevent file corruption on process crash:

1. Serialise player to JSON string.
2. Write to `[username].json.tmp`.
3. `Files.move(tmp, target, ATOMIC_MOVE, REPLACE_EXISTING)` — OS-level atomic rename.

If the process crashes between steps 2 and 3, the `.tmp` file is incomplete and the previous `.json` is intact. The incomplete `.tmp` is overwritten on the next save attempt.

Source: server-persistence.md — Atomic Writes

### Schema Versioning and Migration

Every breaking schema change requires:
1. Incrementing `PlayerSave.CURRENT_VERSION`.
2. Implementing `SaveMigration` with `from` and `to` version numbers.
3. Registering the migration in `MigrationRunner.migrations`.

Migrations are applied in ascending version order during `PlayerStore.load()`. Old save files are always forward-compatible.

Current schema version: **1** (initial schema, no migrations yet).

Source: server-persistence.md — Save Format Versioning

### Inventory Slot Preservation

Inventory items are restored to their exact original slot indices using `Inventory.setSlot(index, itemId, quantity)`. This preserves the visual layout of the player's inventory exactly as it was at save time, matching OSRS behaviour.

Source: https://oldschool.runescape.wiki/w/Inventory

### Prayer Points Restoration

`PrayerSet.prayerPoints` has a private setter and is restored via `PrayerSet.restorePoints(amount)`, which clamps the restored value to the player's current Prayer level. Skill XP (and therefore Prayer level) is restored before prayer points to ensure correct clamping.

Source: https://oldschool.runescape.wiki/w/Prayer#Points

### Backup Rotation (deferred)

Planned: keep last 5 save files per player (`*.json.bak1` … `*.json.bak5`). Rotate on each successful save to protect against data corruption from bad migrations.

Source: server-persistence.md — Backup Retention

## 8. Parity Target

**95%** — persistence is infrastructure; correctness is binary for most fields (either the value survives the round-trip or it does not). The 5% margin covers edge cases such as HP restoration on login (currently informational only) and bank/quest state (deferred).

## 9. Edge Cases

### Process crash between write and rename

Mitigation: atomic rename ensures either the old `.json` or the fully-written `.tmp` survives. The `.tmp` is cleaned up on the next save cycle.

### XP delta vs. absolute restore

`SkillSet.addXp()` adds a delta rather than setting a value. Deserialisation computes `savedXp - currentXp` before calling `addXp`. For fresh players this is always `savedXp - 0 = savedXp`. For a player already partially restored (e.g. double-load), the delta would be 0 and no XP would be added, preventing double-crediting.

### Unknown skill names in old saves

If a save file contains a `skillXp` key that does not match any `Skill.entries` value (e.g. a renamed skill), the unknown key is silently ignored. Known skills that are missing from the save file are left at 0 XP (fresh).

### Prayer points exceed Prayer level after schema migration

If a schema migration adds a new `prayerPoints` field with a value higher than the player's Prayer level, `restorePoints()` will clamp it to the Prayer level — matching OSRS behaviour (prayer points cannot exceed Prayer level × 1).

Source: https://oldschool.runescape.wiki/w/Prayer#Points

### data/players/ is gitignored

Player save files must never be committed to version control. The `data/players/` directory is listed in `.gitignore` per server-persistence.md rules.

### No saving to cache/ or data/osrsbox/

Save files are written exclusively to `data/players/`. Writing to `cache/` or `data/osrsbox/` is explicitly prohibited by server-persistence.md.

## 10. Wiki Citation

Primary source: https://oldschool.runescape.wiki/w/Account

Supporting sources:
- https://oldschool.runescape.wiki/w/Experience (XP cap, XP table)
- https://oldschool.runescape.wiki/w/Inventory (28 slots, slot ordering)
- https://oldschool.runescape.wiki/w/Equipment (14 equipment slots)
- https://oldschool.runescape.wiki/w/Prayer#Points (prayer point cap = Prayer level)
- https://oldschool.runescape.wiki/w/Skills (23 OSRS skills)
- https://oldschool.runescape.wiki/w/Chunk (tile coordinate system, plane values)
