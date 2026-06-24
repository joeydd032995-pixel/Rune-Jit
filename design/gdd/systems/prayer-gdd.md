# Prayer System GDD

**Status**: IMPLEMENTED (Standard Prayer Book)
**Parity Target**: 95%
**Wiki Source**: https://oldschool.runescape.wiki/w/Prayer

---

## 1. Mechanic Overview

Prayer is a combat skill that allows players to activate temporary divine bonuses by consuming prayer points. Players gain prayer points equal to their Prayer level (1 point per level, max 99). When prayers are active they drain points over time at a rate determined by the prayer's drain effect and the player's equipment prayer bonus. When prayer points reach 0 all active prayers deactivate automatically.

---

## 2. XP / Reward Formula

Prayer points do not involve XP during use. Prayer points are determined directly by Prayer level:

```
max_prayer_points = player.skills.getLevel(Skill.PRAYER)
```

Source: https://oldschool.runescape.wiki/w/Prayer#Prayer_points

**Drain rate formula:**
```
threshold = 600 * (1 + floor(equipment_prayer_bonus / 30))
accumulator += sum(drain_effect for each active prayer)  [every tick]
when accumulator >= threshold: prayer_points -= 1, accumulator -= threshold
```

Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate

Example: Piety (drain_effect=24) at 0 prayer bonus:
- threshold = 600
- ticks per point = 600 / 24 = 25 ticks = 15 seconds

---

## 3. Content List

All 30 standard prayers with level requirements, drain effects, and bonuses:

| Prayer | Level | Drain Effect | Bonus |
|--------|-------|-------------|-------|
| Thick Skin | 1 | 3 | +5% Defence |
| Burst of Strength | 4 | 3 | +5% Strength |
| Clarity of Thought | 7 | 3 | +5% Attack |
| Rock Skin | 10 | 6 | +10% Defence |
| Superhuman Strength | 13 | 6 | +10% Strength |
| Improved Reflexes | 16 | 6 | +10% Attack |
| Rapid Restore | 19 | 2 | 2x stat restore rate |
| Rapid Heal | 22 | 3 | 2x HP restore rate |
| Protect Item | 25 | 3 | Keep 1 extra item on death |
| Hawk Eye | 26 | 6 | +5% Ranged |
| Mystic Will | 27 | 6 | +5% Magic |
| Steel Skin | 28 | 12 | +15% Defence |
| Ultimate Strength | 31 | 12 | +15% Strength |
| Incredible Reflexes | 34 | 12 | +15% Attack |
| Protect from Summoning | 35 | 12 | Overhead: Summoning |
| Protect from Magic | 37 | 12 | Overhead: Magic |
| Protect from Ranged | 40 | 12 | Overhead: Ranged |
| Protect from Melee | 43 | 12 | Overhead: Melee |
| Eagle Eye | 44 | 12 | +10% Ranged |
| Mystic Lore | 45 | 12 | +10% Magic |
| Retribution | 46 | 3 | Explode on death in PvP |
| Redemption | 49 | 6 | Heal from prayer points when near death |
| Smite | 52 | 18 | Drain opponent prayer on hit |
| Preserve | 55 | 6 | Extends stat boost duration by 50% |
| Chivalry | 60 | 24 | +15% Attack, +18% Strength, +20% Defence |
| Mystic Might | 45 | 18 | +15% Magic |
| Wrath | 59 | 3 | Melee damage on death |
| Piety | 70 | 24 | +20% Attack, +23% Strength, +25% Defence |
| Rigour | 74 | 24 | +20% Ranged, +23% Ranged Str, +25% Defence |
| Augury | 77 | 24 | +25% Magic, +25% Magic Def, +25% Defence |

Source: https://oldschool.runescape.wiki/w/Prayer#Standard_prayers

---

## 4. Level Requirements

| Prayer | Level Required |
|--------|----------------|
| Thick Skin | 1 |
| Burst of Strength | 4 |
| Clarity of Thought | 7 |
| Rock Skin | 10 |
| Superhuman Strength | 13 |
| Improved Reflexes | 16 |
| Rapid Restore | 19 |
| Rapid Heal | 22 |
| Protect Item | 25 |
| Hawk Eye | 26 |
| Mystic Will | 27 |
| Steel Skin | 28 |
| Ultimate Strength | 31 |
| Incredible Reflexes | 34 |
| Protect from Summoning | 35 |
| Protect from Magic | 37 |
| Protect from Ranged | 40 |
| Protect from Melee | 43 |
| Eagle Eye | 44 |
| Mystic Lore | 45 |
| Retribution | 46 |
| Redemption | 49 |
| Smite | 52 |
| Preserve | 55 |
| Chivalry | 60 |
| Mystic Might | 45 |
| Wrath | 59 |
| Piety | 70 |
| Rigour | 74 |
| Augury | 77 |

Additionally: Chivalry and Piety require completion of King's Ransom; Rigour requires The Fremennik Exiles; Augury requires Dragon Slayer II (all deferred — no quest system yet).

Source: https://oldschool.runescape.wiki/w/Prayer#Standard_prayers

---

## 5. Required Items / Tools

- **Altar**: Recharges prayer points to maximum. Any altar in a chapel, POH, or the Lumbridge Castle chapel.
- **Bones / Ashes**: Offered at altars or on a gilded altar for Prayer XP. Not used during prayer activation.
- **Equipment with Prayer Bonus**: Items such as Proselyte armour, Holy symbol (+8), Berserker ring (i) (+4) increase the drain threshold.
- **Prayer potions / Super restore**: Restore prayer points (not yet wired — stub via `PrayerSet.restorePoints()`).

Source: https://oldschool.runescape.wiki/w/Prayer#Prayer_bonus

---

## 6. Tick Rate

Prayer drain is processed every game tick (600ms).

```
Every tick:
  accumulator += sum(active_prayers.drain_effect)
  threshold = 600 * (1 + floor(prayerBonus / 30))
  while accumulator >= threshold:
      prayer_points -= 1
      accumulator -= threshold
```

The `PrayerDrainAction` is a `TickEvent` that reschedules itself with `tickQueue.schedule(1, this)` each tick. No `Thread.sleep`.

Source: https://oldschool.runescape.wiki/w/Prayer#Drain_rate

---

## 7. Special Mechanics

### Prayer Flicking
Players can activate and deactivate a prayer within the same game tick to gain the bonus for attacks processed that tick without any drain being applied. The `PrayerSet.tick()` method checks the active set at the time it is called — if the player has already deactivated all prayers before the tick fires, the drain is skipped.

This is an intentional OSRS mechanic and is NOT filtered or prevented.
Source: https://oldschool.runescape.wiki/w/Prayer#Prayer_flicking

### Overhead Protection Damage Reduction
- **In PvM (vs. NPCs)**: Protection prayers reduce incoming damage by **40%** (not full block).
- **In PvP (vs. players)**: Protection prayers reduce incoming damage to **0** for that attack style.

`PrayerSet.isProtectedFrom(CombatStyle)` returns true/false; the combat engine applies the reduction percentage separately.
Source: https://oldschool.runescape.wiki/w/Prayer#Overhead_prayers

### Mutual Exclusivity
Only one prayer per bonus category may be active simultaneously. Activating a new prayer automatically deactivates conflicting prayers in the same category. Combined prayers (Piety, Rigour, Augury, Chivalry) deactivate individual prayers in all categories they cover.

### Smite
Drains 1/4 of damage dealt from the opponent's prayer points. Implemented in `PrayerSet` structure; wired when NPC death handling is implemented.

### Redemption
When HP drops to 10% or below, converts remaining prayer points to HP (10 HP per prayer point). Not yet wired — requires HP threshold detection in combat.

### Preserve
Extends duration of stat boosts by 50%. Not yet wired — requires stat boost timer system.

---

## 8. Parity Target

**Target: 95%**

| Component | Status | Notes |
|-----------|--------|-------|
| Drain formula | IMPLEMENTED | All 30 prayers, equipment bonus modifier |
| Combat multipliers | IMPLEMENTED | Attack/Strength/Defence/Ranged/Magic wired |
| Overhead protection flags | IMPLEMENTED | `isProtectedFrom()` wired |
| Prayer points / fill / restore | IMPLEMENTED | `fillPoints()`, `restorePoints()` |
| Mutual exclusivity | IMPLEMENTED | All categories |
| Prayer flicking | IMPLEMENTED | No-drain on deactivate same tick |
| Smite prayer point drain | DEFERRED | Requires NPC death hooks |
| Redemption heal | DEFERRED | Requires HP threshold events |
| Preserve boost extension | DEFERRED | Requires stat boost timers |
| Ancient Curses (Turmoil etc.) | DEFERRED | Post-Desert Treasure II |
| Necromancy prayer book | DEFERRED | Separate book |
| Widget activation (prayer tab) | DEFERRED | Requires protocol interface IDs |

---

## 9. Edge Cases

- **Prayer flicking with multiple prayers**: Each prayer's bonus applies independently. A player can flick only one prayer while keeping others permanently active.
- **Smite in multi-combat**: Smite applies to each hit from all attackers simultaneously — this can rapidly drain prayer at a rate exceeding normal drain for multiple-attacker scenarios.
- **Protection in multi-combat PvM**: Each NPC's attack is checked independently against `isProtectedFrom()`. The 40% reduction applies per hit.
- **Prayer bonus rounding**: `floor(prayerBonus / 30)` — a prayer bonus of 29 gives the same threshold as bonus 0 (both give floor = 0). Bonus 30 doubles threshold.
- **Zero prayer points at login**: Players log in with 0 prayer points. `PrayerSet.activate()` returns false until `fillPoints()` or `restorePoints()` is called.
- **Rigour/Augury unlocks**: These prayers require specific quest completions not yet modelled. Currently unlocked at the required Prayer level only.
- **PvM 40% reduction vs. PvP 0 damage**: The combat engine must check `isProtectedFrom()` and apply the correct reduction (40% PvM, 100% PvP) — the `PrayerSet` provides only the boolean; the combat context determines the reduction amount.

Source: https://oldschool.runescape.wiki/w/Prayer

---

## 10. Wiki Citation

Primary reference: https://oldschool.runescape.wiki/w/Prayer

Supporting references:
- Drain rate: https://oldschool.runescape.wiki/w/Prayer#Drain_rate
- Prayer bonuses: https://oldschool.runescape.wiki/w/Prayer#Bonuses
- Overhead prayers: https://oldschool.runescape.wiki/w/Prayer#Overhead_prayers
- Prayer flicking: https://oldschool.runescape.wiki/w/Prayer#Prayer_flicking
- Prayer points: https://oldschool.runescape.wiki/w/Prayer#Prayer_points
- Chivalry: https://oldschool.runescape.wiki/w/Chivalry
- Piety: https://oldschool.runescape.wiki/w/Piety
- Rigour: https://oldschool.runescape.wiki/w/Rigour
- Augury: https://oldschool.runescape.wiki/w/Augury
