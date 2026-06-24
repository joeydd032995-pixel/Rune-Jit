# Mining GDD — Status: DRAFT

## 1. Mechanic Overview

Mining is a gathering skill where players use pickaxes to extract ores from rocks. Each attempt takes 3 game ticks (1.8 seconds). Success depends on the player's Mining level, the pickaxe's mining bonus, and the rock's difficulty rating.
Source: https://oldschool.runescape.wiki/w/Mining

## 2. XP/Reward Formula

```
success = random(0..254) < floor((miningLevel * 2 + pickaxe.miningBonus) / rock.difficulty)
```
On success: player receives one ore and the rock's flat XP value.
Source: https://oldschool.runescape.wiki/w/Mining#Rolling_for_success

## 3. Content List

| Rock | Level | XP | Ore ID | Difficulty | Members |
|------|-------|----|--------|------------|---------|
| Clay | 1 | 5.0 | 434 | 6 | No |
| Copper | 1 | 17.5 | 436 | 32 | No |
| Tin | 1 | 17.5 | 438 | 32 | No |
| Limestone | 10 | 26.5 | 1779 | 32 | Yes |
| Iron | 15 | 35.0 | 440 | 40 | No |
| Silver | 20 | 40.0 | 442 | 64 | No |
| Coal | 30 | 50.0 | 453 | 80 | No |
| Gold | 40 | 65.0 | 444 | 100 | No |
| Gem rock | 40 | 65.0 | 1623 | 64 | Yes |
| Mithril | 55 | 80.0 | 447 | 150 | No |
| Adamantite | 70 | 95.0 | 449 | 200 | No |
| Runite | 85 | 125.0 | 451 | 255 | Yes |
| Amethyst | 92 | 240.0 | 21347 | 255 | Yes |

Source: https://oldschool.runescape.wiki/w/Mining#Experience

## 4. Level Requirements

Minimum level 1 (Clay/Copper/Tin). Maximum gated: Amethyst at level 92. See table above.

## 5. Required Items/Tools

A pickaxe in inventory or equipped (weapon slot). Best usable pickaxe auto-selected.

| Pickaxe | Level | Mining Bonus |
|---------|-------|-------------|
| Bronze | 1 | 1 |
| Iron | 1 | 10 |
| Steel | 6 | 20 |
| Black | 11 | 22 |
| Mithril | 21 | 30 |
| Adamant | 31 | 40 |
| Rune | 41 | 50 |
| Dragon/Infernal/3rd age | 61 | 60 |
| Crystal | 71 | 80 |

Source: https://oldschool.runescape.wiki/w/Pickaxe

## 6. Tick Rate

3 game ticks per attempt (1.8 seconds).
Source: https://oldschool.runescape.wiki/w/Mining

## 7. Special Mechanics

**Gem drops**: 1/256 chance per successful mine → uncut gem (sapphire:emerald:ruby:diamond = 4:2:1:1).
Source: https://oldschool.runescape.wiki/w/Uncut_gem

**Rock Golem pet**: Base rate ~1/741,600 at level 99, scales with level.
Source: https://oldschool.runescape.wiki/w/Rock_golem

**3-tick mining**: Legitimate tick-manipulation technique; deferred pending tick-manipulation framework.

**Rock depletion**: Graceful-absent — rocks remain mineable until world object state system is implemented.

## 8. Parity Target

90% — XP values, pickaxe selection, success formula, gem drops, pet rolls must be exact.

## 9. Edge Cases

- No pickaxe → mining stops immediately
- Inventory full → action stops before ore is added
- Level boost from potions applies (`getBoostedLevel`)
- Infernal pickaxe 1/3 smelt chance → deferred
- Sandstone/Granite variable XP → deferred
- 3-tick method must not be blocked by artificial cooldowns

## 10. Wiki Citation

https://oldschool.runescape.wiki/w/Mining
