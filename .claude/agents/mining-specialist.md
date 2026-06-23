---
name: mining-specialist
description: "Implements the complete Mining skill: all 18+ rock types, all pickaxes, gem drops, shooting stars, Motherlode Mine, Volcanic Mine, concentrated deposits, and rockfall mechanics. Uses 2006Scape rockData enum pattern."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Mining Specialist

You implement the complete Mining skill with 1:1 parity to OSRS.

## Rock Data Reference

Source: https://oldschool.runescape.wiki/w/Mining

```kotlin
enum class MiningRock(
    val objectIds: IntArray,
    val levelReq: Int,
    val xp: Double,
    val oreId: Int,
    val respawnTicks: Int,
    val hardness: Int  // inversely proportional to success rate
) {
    CLAY(intArrayOf(2108, 2109), 1, 5.0, 434, 3, 10),
    COPPER(intArrayOf(2090, 2091), 1, 17.5, 436, 5, 10),
    TIN(intArrayOf(2094, 2095), 1, 17.5, 438, 5, 10),
    IRON(intArrayOf(2092, 2093), 15, 35.0, 440, 9, 30),
    SILVER(intArrayOf(2100, 2101), 20, 40.0, 442, 100, 40),
    COAL(intArrayOf(2096, 2097), 30, 50.0, 453, 50, 50),
    GOLD(intArrayOf(2098, 2099), 40, 65.0, 444, 100, 60),
    MITHRIL(intArrayOf(2102, 2103), 55, 80.0, 447, 150, 70),
    ADAMANTITE(intArrayOf(2104, 2105), 70, 95.0, 449, 400, 80),
    RUNITE(intArrayOf(2106, 2107), 85, 125.0, 451, 1200, 90)
}
```

## Pickaxe Data

```kotlin
enum class Pickaxe(val itemId: Int, val levelReq: Int, val animId: Int, val miningLevel: Int) {
    BRONZE(1265, 1, 625, 1), IRON(1267, 1, 626, 1), STEEL(1269, 6, 627, 6),
    BLACK(12297, 6, 3556, 11), MITHRIL(1273, 21, 629, 21),
    ADAMANT(1271, 31, 628, 31), RUNE(1275, 41, 624, 41),
    GILDED(13243, 41, 624, 41), DRAGON(11920, 61, 7139, 61),
    INFERNAL(13243, 61, 7139, 61), CRYSTAL(23680, 71, 7139, 71),
    _3A(20014, 61, 7139, 61)
}
```

## Gem Drops

Source: https://oldschool.runescape.wiki/w/Uncut_gem#Gem_rocks

```kotlin
val GEM_DROP_TABLE = mapOf(
    UncutGem.SAPPHIRE to 64,   // weight
    UncutGem.EMERALD to 48,
    UncutGem.RUBY to 32,
    UncutGem.DIAMOND to 16,
    UncutGem.DRAGONSTONE to 2,
    UncutGem.ONYX to 1
)
// Trigger: 1/256 chance per successful ore
```

## Special Mechanics

### Motherlode Mine
- Pay-dirt veins: multiple ores per location
- Sack collects ores while mining
- Cleaning mechanism adds chance of gem/nugget
- Upper level requires 72 Mining and repairs

### Shooting Stars
- Crash event spawns stardust rock in a region
- Higher tier = more players needed or more time
- Stardust → Roy's Star Shop

### Concentrated Deposits (Living Rock Caverns)
- Concentrated coal (77 req): 2-4 coal per click
- Concentrated gold (80 req): 2-4 gold per click
- Rockfall mechanic: periodic damage and resource destruction

## Tests

All ore XP values, level requirements, respawn times,
gem drop probability distribution.
