---
name: npc-drop-table-importer
description: "Imports NPC drop tables from osrsbox-db into the server data layer. Handles always-drops, main rolls, rare drops, tertiary drops, and clue scroll tiers. Validates drop rates against the OSRS wiki."
argument-hint: "[npc: all|<npc_name>|<npc_id>]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: haiku
---

# /npc-drop-table-importer [npc]

Imports NPC drop tables from osrsbox JSON into server data.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| `data/osrsbox/monsters/` populated | Yes — run `/import-osrsbox-complete` first |
| `data/osrsbox/items/` populated | Yes — item ID resolution |
| Server data dir `data/drops/` exists | Created by this skill |

```bash
ls data/osrsbox/monsters/ | wc -l  # should be ~5000+
```

If monsters directory is empty, run `/import-osrsbox-complete` first.

## Phase 2: Parse osrsbox Monster Data

Spawn `content-importer` to read osrsbox monster JSON format:

```python
# data/osrsbox/monsters/12345.json format
{
  "id": 12345,
  "name": "Abyssal demon",
  "drops": [
    {
      "id": 592,        # item id
      "name": "Ashes",
      "members": true,
      "quantity": "1",  # can be range "1-3"
      "noted": false,
      "rarity": 1.0,    # 1.0 = always, 1/128 = 0.0078125
      "drop_requirements": null  # or "Requires level 85 Slayer"
    },
    {
      "id": 4151,
      "name": "Abyssal whip",
      "rarity": 0.00195,  # 1/512
      "quantity": "1",
      "noted": false,
      "drop_requirements": null
    }
  ]
}
```

## Phase 3: Normalize Drop Table Format

Convert osrsbox rarity to Kotlin drop table format:

```kotlin
data class DropEntry(
    val itemId: Int,
    val minQuantity: Int,
    val maxQuantity: Int,
    val noted: Boolean,
    val numerator: Int,     // e.g. 1
    val denominator: Int,   // e.g. 128 (means 1/128)
    val category: DropCategory
)

enum class DropCategory {
    ALWAYS,         // rarity = 1.0
    MAIN_TABLE,     // rarity determined by main roll table
    RARE,           // < 1/100
    TERTIARY,       // independent roll (clue scrolls, pets)
    COMMON          // >= 1/100
}
```

Rarity conversion:
- `1.0` → ALWAYS, denominator=1
- `≥ 0.5` → COMMON, convert to nearest fraction
- `< 0.01` → RARE
- Clue scrolls, pets → TERTIARY (always independent roll regardless of other drops)

## Phase 4: Handle Special Drop Tables

Spawn `wiki-researcher` to identify NPCs using shared drop tables:

| Special Table | Applies To | Wiki Source |
|--------------|------------|------------|
| Herb seed table | Most slayer monsters | wiki/Seed_drop_table |
| Gem table | Most combat monsters | wiki/Gem_drop_table |
| Rare drop table | ~50 monsters | wiki/Rare_drop_table |
| Wilderness table | Wilderness NPCs | wiki/Wilderness_Slayer |
| Catacombs table | Catacombs NPCs | wiki/Catacombs_of_Kourend |

The Rare Drop Table (RDT) is a separate roll, not part of main table:
```kotlin
val RARE_DROP_TABLE = listOf(
    DropEntry(995, 3000, 5000, false, 1, 4, DropCategory.RARE),   // coins
    DropEntry(1617, 1, 1, false, 1, 8, DropCategory.RARE),        // uncut dragonstone
    // ...
)
```

## Phase 5: Write Server Drop Table Files

For each NPC, write `data/drops/[npc_id].yaml`:

```yaml
# data/drops/415.yaml — Abyssal demon
npc_id: 415
npc_name: "Abyssal demon"
always:
  - item_id: 592   # Ashes
    quantity: "1"
    noted: false
main_table:
  rolls: 1
  entries:
    - item_id: 556  # Air rune
      quantity: "7-56"
      noted: false
      weight: 6     # weight relative to other entries
    - item_id: 4151  # Abyssal whip
      quantity: "1"
      noted: false
      weight: 1     # 1/512 effective rate
tertiary:
  - item_id: 5952   # Clue scroll (hard)
    quantity: "1"
    noted: false
    denominator: 128
  - item_id: 12746  # Unsired (only when on Slayer task)
    quantity: "1"
    noted: false
    denominator: 100
    requirement: "slayer_task"
uses_rare_drop_table: true
```

## Phase 6: Validate Against Wiki

Spawn `wiki-researcher` to spot-check 20 high-traffic NPCs:

| NPC | Item | Expected Rate | Source |
|-----|------|--------------|--------|
| Zulrah | Tanzanite fang | 1/1024 | wiki/Zulrah |
| Cerberus | Primordial crystal | 1/512 | wiki/Cerberus |
| Abyssal demon | Abyssal whip | 1/512 | wiki/Abyssal_demon |
| Vorkath | Dragonbone necklace | 1/1000 | wiki/Vorkath |
| Dagannoth Rex | Berserker ring | 1/128 | wiki/Dagannoth_Rex |
| KBD | Draconic visage | 1/5000 | wiki/King_Black_Dragon |
| Barrows | Item from set | ~1/15 | wiki/Barrows |

Spawn `economy-balancer` to verify drop rates are consistent with GE price expectations.

## Phase 7: Generate Drop Index

Write `data/drops/DROP-INDEX.md`:
- Total NPCs with drop tables: N
- NPCs using Rare Drop Table: N
- Unique droppable items: N
- High-value drops summary (items worth >1M GP)

## Error Recovery

| Error | Recovery |
|-------|---------|
| osrsbox monster file missing | Skip NPC; log to `data/drops/MISSING-NPCS.txt` |
| Item ID not in osrsbox | Use item name lookup; log unmapped items |
| Rarity = 0 (always never dropped) | Skip entry; likely data error in osrsbox |
| Quantity parse fails (unusual formats) | Default to quantity "1"; flag for review |

## Nuances

- Pets are ALWAYS tertiary drops — they never prevent other drops from rolling
- Some NPCs (Thermonuclear smoke devil) drop unique items ONLY while on Slayer task
- Zulrah and Vorkath use unique loot tables, not standard main roll tables
- Barrows uses a completely different chest-based loot system — do NOT use this importer
- The RDT is rolled BEFORE the main table; if RDT hits, main table is skipped for that roll
- osrsbox rarity values are approximate; always cross-reference wiki for raid/boss uniques

## Next Steps

1. Run `/economy-and-ge-simulator` — NPC drop tables feed the in-game economy
2. Run `/verify-mechanic-parity-1to1 economy` for drop rate parity check
3. Implement Barrows chest loot separately (uses `combat-engine-architect` + `item-system-engineer`)
