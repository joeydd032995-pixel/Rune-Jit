---
name: content-importer
description: "Bulk-imports osrsbox-db JSON data into the server data layer: items, monsters, prayers, and objects. Validates data integrity and generates Kotlin data files."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Content Importer

You import osrsbox-db content into the Rune-Jit server data layer.

## osrsbox-db Data Files

After running `/import-osrsbox-complete`, these files exist in `data/osrsbox/`:

```
data/osrsbox/
├── items-complete.json       # 23,000+ items with all properties
├── monsters-complete.json    # 5,000+ NPC entries with drop tables
├── prayers-complete.json     # All 30 prayers with drain rates
└── items-icons/              # PNG icons (not committed — too large)
```

## Item Import

```kotlin
class ItemImporter {
    fun importAll(jsonPath: Path): ImportResult {
        val raw = jsonPath.readText()
        val items = Json.decodeFromString<Map<String, OsrsboxItem>>(raw)

        var imported = 0
        var skipped = 0

        items.values.forEach { item ->
            try {
                val def = mapToItemDefinition(item)
                ItemDefinitions.register(def)
                imported++
            } catch (e: Exception) {
                logger.warn("Skipped item ${item.id} (${item.name}): ${e.message}")
                skipped++
            }
        }
        return ImportResult(imported, skipped, items.size)
    }

    private fun mapToItemDefinition(item: OsrsboxItem): ItemDefinition {
        return ItemDefinition(
            id = item.id,
            name = item.name,
            stackable = item.stackable ?: false,
            noted = item.noted ?: false,
            noteable = item.noteable ?: false,
            linkedIdItem = item.linkedIdItem,
            linkedIdNoted = item.linkedIdNoted,
            tradeable = item.tradeable ?: false,
            equipable = item.equipable ?: false,
            cost = item.cost ?: 1,
            lowalch = item.lowalch,
            highalch = item.highalch,
            weight = item.weight ?: 0.0,
            equipmentStats = item.equipment?.let { mapEquipmentStats(it) }
        )
    }
}
```

## Monster/NPC Import

```kotlin
class MonsterImporter {
    fun importAll(jsonPath: Path): ImportResult {
        val raw = jsonPath.readText()
        val monsters = Json.decodeFromString<Map<String, OsrsboxMonster>>(raw)

        monsters.values.forEach { monster ->
            val npcDef = NpcDefinition(
                id = monster.id,
                name = monster.name,
                combatLevel = monster.combatLevel ?: 0,
                hitpoints = monster.hitpoints ?: 0,
                maxHit = monster.maxHit ?: 0,
                attackSpeed = monster.attackSpeed ?: 4,
                attackStyle = monster.attackType?.map { mapAttackStyle(it) } ?: emptyList(),
                aggressive = monster.aggressive ?: false,
                poisonous = monster.poisonous ?: false,
                immuneToPoison = monster.immuneToPoison ?: false,
                immuneToVenom = monster.immuneToVenom ?: false,
                slayerLevel = monster.slayerLevel,
                slayerXp = monster.slayerXp,
                dropTable = buildDropTable(monster.drops)
            )
            NpcDefinitions.register(npcDef)
        }
    }
}
```

## Prayer Import

```kotlin
class PrayerImporter {
    fun importAll(jsonPath: Path) {
        val prayers = Json.decodeFromString<List<OsrsboxPrayer>>(jsonPath.readText())
        prayers.forEach { p ->
            PrayerDefinition(
                id = p.id,
                name = p.name,
                description = p.description,
                drainRate = p.drainEffect,  // prayer points per tick × 1000
                levelRequired = p.prayerLevel,
                bonuses = mapPrayerBonuses(p)
            ).also { PrayerDefinitions.register(it) }
        }
    }
}
```

## Import Validation

After import, validate:
1. No missing item IDs referenced by equipment (weapon/armor/ammo)
2. All NPC drop tables reference valid item IDs
3. Prayer drain rates match wiki values within 1%
4. Total item count ≥ 20,000 (sanity check)

```kotlin
fun validateImport(): ValidationReport {
    val errors = mutableListOf<String>()
    if (ItemDefinitions.count() < 20000) errors.add("Only ${ItemDefinitions.count()} items imported (expected 20k+)")
    if (NpcDefinitions.count() < 4000) errors.add("Only ${NpcDefinitions.count()} NPCs imported (expected 4k+)")
    return ValidationReport(errors.isEmpty(), errors)
}
```
