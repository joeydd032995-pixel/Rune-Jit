---
name: combat-engine-full
description: "Implements the complete OSRS combat engine: melee/ranged/magic accuracy and damage formulas, prayer system, special attacks, poison/venom, and multiway combat zones."
argument-hint: ""
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: opus
---

# /combat-engine-full

Implements the complete OSRS combat system.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Tick engine running | Yes |
| osrsbox imported (prayers, monsters) | Yes |
| Combat formulas GDD | Recommended |

## Phase 2: Formula Verification

Spawn `combat-theorist` to validate all formulas against wiki:

| Formula | Wiki Source |
|---------|-------------|
| Melee max hit | https://oldschool.runescape.wiki/w/Maximum_melee_hit |
| Ranged max hit | https://oldschool.runescape.wiki/w/Maximum_ranged_hit |
| Magic max hit | https://oldschool.runescape.wiki/w/Maximum_magic_damage |
| Accuracy | https://oldschool.runescape.wiki/w/Accuracy |
| Prayer bonuses | https://oldschool.runescape.wiki/w/Prayer#Bonuses |

Key formulas to implement exactly:
```
Melee max hit: floor(0.5 + eff_str * (str_bonus + 64) / 640)
Accuracy (atk > def): 1 - (def + 2) / (2 * (atk + 1))
Accuracy (atk ≤ def): atk / (2 * (def + 1))
```

## Phase 3: Implement CombatEngine

Spawn `combat-engine-architect` to implement:
- `src/server/combat/CombatEngine.kt` — core attack/defence rolls
- `src/server/combat/MaxHitCalculator.kt` — per-style max hit
- `src/server/combat/AccuracyCalculator.kt` — hit/miss determination
- `src/server/combat/SpecialAttackHandler.kt` — per-weapon specs
- `src/server/combat/PoisonHandler.kt` — poison/venom/disease

## Phase 4: Implement Prayer System

Spawn `prayer-system-architect` to implement:
- `src/server/combat/PrayerHandler.kt` — drain rates, overhead protection, quick prayers
- Exact drain rates from wiki (e.g., Protect Melee: 1 point/60s = drain level 1/60 ticks)
- Overhead protection: reduces PvP damage; no reduction for PvM (common misconception)

## Phase 5: Multi-combat Implementation

- Multi-combat zones from cache clip flags
- NPC retaliation: retaliates to first attacker only
- Player-vs-player multi: all players in zone can pile
- Max attackers per target: wiki-documented per zone

## Phase 6: Parity Tests

Spawn `combat-tester` to write and run:
- Max hit tests for 20+ scenarios (Dharok, Void, Berserker, etc.)
- Accuracy tests for 10+ scenarios
- Prayer drain rate tests
- Special attack tests for 10+ weapons
- Poison/venom damage progression tests

```bash
./gradlew test --tests "*CombatParity*"
```

Target: 92%+ pass rate.

## Phase 7: Performance Gate

Combat must not add >20ms to tick budget for 2000 entities in combat.

## Error Recovery

| Error | Recovery |
|-------|---------|
| Formula mismatch | Spawn combat-theorist with specific test case |
| Prayer drain wrong | Check wiki drain rate formula exactly |
| Multi-combat zone issues | Verify clip flag parsing in world-region-loader |

## Nuances

- Prayer protection in PvM: overhead prayers do NOT block NPC attacks
- Dharok set effect: `max_hit *= (1 + (max_hp - current_hp) / 100)` — uses max_hp, not 99
- Ranged void: +10% effective attack; +10% damage (multiplicative with max hit)
- Twisted bow: scales with magic level of target — use target's magic attribute
- Scythe of vitur: hits 3 times; first hit full, second 50%, third 25%

## Next Steps

1. Run `/prayer-effect-simulator` to verify prayer formulas
2. Run `/verify-mechanic-parity-1to1 combat` for parity score
3. Run `/raids-and-instanced-content-generator` for raid combat
