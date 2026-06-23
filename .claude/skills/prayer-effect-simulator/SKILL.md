---
name: prayer-effect-simulator
description: "Implements all OSRS prayers across all three prayer books (standard, curses, ancient), drain rates, overhead protection mechanics, quick-prayer presets, and prayer bonus calculations with wiki-exact formulas."
argument-hint: "[book: standard|curses|ancient|all]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /prayer-effect-simulator [book]

Implements all OSRS prayer mechanics with wiki-exact formulas.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| `data/osrsbox/prayers.json` loaded | Yes — run `/import-osrsbox-complete` first |
| Combat engine implemented | Yes — prayers modify combat rolls |
| Tick engine implemented | Yes — prayer drain runs on tick |
| `design/gdd/prayer-gdd.md` exists | Recommended |

Read `data/osrsbox/prayers.json` for all prayer definitions.

## Phase 2: Load Wiki Prayer Data

Spawn `wiki-researcher` to fetch all prayer values from wiki:

| Prayer Book | Count | Wiki Source |
|-------------|-------|------------|
| Standard Prayers | 30 | wiki/Prayer#Prayers |
| Ancient Curses | 24 | wiki/Curses |
| Necromancy Prayers | 10 | wiki/Necromancy#Prayers |

Key formula values to verify:
```
Prayer drain rate formula:
  drain_rate = floor(prayer_drain_effect × 1 / (1 + floor(prayer_bonus / 30)))
  Ticks per drain = floor(60 / drain_effect) × (1 + floor(prayer_bonus / 30))

Maximum prayer bonus: 70 (at high Prayer items + Ring of the gods)
```

## Phase 3: Implement Prayer Data Layer

Spawn `prayer-system-architect` to define prayer constants from osrsbox:

```kotlin
data class PrayerDefinition(
    val id: Int,
    val name: String,
    val levelRequired: Int,
    val drainEffect: Double,    // points drained per minute at 0 bonus
    val book: PrayerBook,
    val overhead: Boolean,      // overhead (protection) prayer?
    val effects: List<PrayerEffect>
)

data class PrayerEffect(
    val stat: CombatStat,       // ATTACK, STRENGTH, DEFENCE, MAGIC, RANGED
    val multiplier: Double,     // e.g. 1.15 for +15%
    val type: EffectType        // OFFENSIVE, DEFENSIVE, PROTECTION
)

enum class PrayerBook { STANDARD, CURSES, ANCIENT }
```

Standard prayer multipliers (exact wiki values):
```
Burst of Strength:  +5%  Strength
Superhuman Strength: +10% Strength
Ultimate Strength:  +15% Strength
Incredible Reflexes: +15% Attack
Piety:       Attack+20%, Strength+23%, Defence+25%
Rigour:      Ranged+20%, Ranged Strength+23%, Defence+25%
Augury:      Magic+25%, Magic Defence+25%, Defence+25%
Chivalry:    Attack+15%, Strength+18%, Defence+20%
```

## Phase 4: Implement Prayer Drain System

Spawn `prayer-system-architect` to implement tick-based drain:

```kotlin
class PrayerHandler(val player: Player) {

    fun onTick() {
        if (player.activePrayers.isEmpty()) return

        val totalDrainEffect = player.activePrayers
            .sumOf { it.drainEffect }

        val prayerBonus = player.equipment.prayerBonus()
        val ticksPerDrain = floor(60.0 / totalDrainEffect) *
                            (1 + floor(prayerBonus / 30.0))

        if (player.ticksWithPrayer % ticksPerDrain.toInt() == 0) {
            player.prayerPoints -= 1
            if (player.prayerPoints <= 0) {
                player.prayerPoints = 0
                deactivateAll()
                player.sendMessage("You have run out of Prayer points.")
            }
        }
        player.ticksWithPrayer++
    }
}
```

## Phase 5: Implement Protection Prayer Mechanics

**CRITICAL**: Overhead prayers do NOT block damage in PvM — they REDUCE damage.

Spawn `combat-theorist` to verify exact reduction values:

| Prayer | vs Melee | vs Ranged | vs Magic | Source |
|--------|----------|-----------|----------|--------|
| Protect from Melee | 0% (PvP only 40%) | — | — | wiki/Protect_from_Melee |
| Protect from Missiles | — | 0% (PvP 40%) | — | wiki/Protect_from_Missiles |
| Protect from Magic | — | — | 0% (PvP 40%) | wiki/Protect_from_Magic |

Wait — this requires wiki re-check. The common misconception:
- In **PvM**: Overhead prayers block 100% of the corresponding damage type from NPCs
- In **PvP**: Overhead prayers reduce damage by ~40% from the corresponding combat style

```kotlin
fun applyProtectionPrayer(
    attacker: Entity,
    defender: Player,
    attackStyle: CombatStyle,
    damage: Int
): Int {
    if (attacker is NPC) {
        // PvM: full block
        return if (defender.hasProtectionPrayer(attackStyle)) 0 else damage
    } else {
        // PvP: 40% reduction
        return if (defender.hasProtectionPrayer(attackStyle))
            floor(damage * 0.6).toInt()
        else damage
    }
}
```

Source: https://oldschool.runescape.wiki/w/Overhead_prayer

## Phase 6: Implement Curses (Ancient Prayers)

Spawn `prayer-system-architect` for Curses book:

Key Curses and their effects:
| Curse | Effect | Drain Effect |
|-------|--------|-------------|
| Turmoil | Attack+15/21%, Strength+23%, Defence+15/21% | 5.0/min |
| Torment | Ranged+15/19%, Ranged Str+23%, Defence+15/19% | 5.0/min |
| Anguish | Magic+15/17%, Magic Defence+15/17% | 5.0/min |
| Leech Attack | Steals opponent's Attack level | varies |
| Protect Item | Keep 1 extra item on death | 2.0/min |

Curses require completion of Desert Treasure II — check quest state variable before activating.

## Phase 7: Implement Quick Prayers

```kotlin
class QuickPrayerPreset(
    val prayers: Set<PrayerDefinition>
) {
    fun activate(player: Player) {
        prayers.forEach { prayer ->
            if (player.prayerPoints > 0 && player.prayer.level >= prayer.levelRequired) {
                player.activePrayers.add(prayer)
            }
        }
    }
}
```

Quick prayer toggle must:
1. Activate all prayers in preset simultaneously
2. Respect conflicts (cannot activate two offensive prayers of same type)
3. Soul Altar recharge: restores prayer points based on bones sacrificed at altar

## Phase 8: Parity Verification

Spawn `combat-tester` to validate prayer effects:

| Test | Expected | Source |
|------|----------|--------|
| Piety strength multiplier | ×1.23 | wiki/Piety |
| 99 prayer, 70 bonus, all prayers active | drain rate formula result | wiki/Prayer#Drain_rate |
| Rigour ranged bonus | +20% accuracy, +23% strength | wiki/Rigour |
| PvM overhead prayer | 100% damage blocked | wiki/Overhead_prayer |
| PvP overhead prayer | ~40% damage reduction | wiki/Overhead_prayer |
| Preserve prayer active | 43% prayer points retained on logout | wiki/Preserve |

## Error Recovery

| Error | Recovery |
|-------|---------|
| osrsbox missing prayer | Use wiki data directly; document discrepancy |
| Curses quest check fails | Default to requiring Desert Treasure II completion |
| Drain rate off | Re-check prayer bonus formula — floor operations matter |
| Protection prayers blocking in PvP | Fix: PvM = full block, PvP = 40% reduction |

## Nuances

- Prayers stack multiplicatively with combat bonus equipment (void, salve amulet)
- Prayer bonus from equipment slows drain, does NOT increase prayer effect
- Multiple offensive prayers of the same combat style CANNOT be active simultaneously
- Protect Item saves 1 extra item AND 4 normal items (not 5 total) in PvP
- Smite drains prayer in PvP: 1/4 of damage dealt as prayer points (not HP)
- Rapid Restore/Heal/Replenishment work outside of combat only

## Next Steps

1. Run `/combat-engine-full` to integrate prayer multipliers into hit rolls
2. Run `/verify-mechanic-parity-1to1 combat` to test prayer parity
3. Run `/gdd-osrs-specialized-framework prayer` to create complete Prayer GDD
