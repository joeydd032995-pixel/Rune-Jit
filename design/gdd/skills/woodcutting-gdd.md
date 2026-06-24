# Woodcutting GDD

**Status**: DRAFT
**Phase**: 1 (Design)
**Parity Target**: 90%
**Last Updated**: 2026-06-24
**Note**: Animation IDs are unverified — marked TODO pending `/load-osrs-cache-full`. This GDD cannot be marked FINAL until animation IDs are confirmed from cache index 12.

---

## 1. Mechanic Overview

Woodcutting is a gathering skill where players chop trees to receive logs. Each chop attempt resolves on a 4-tick cycle (2.4 seconds); the server rolls a success check each tick using the player's Woodcutting level, the equipped axe's bonus, and the target tree's difficulty constant. On success, the player receives a log (or direct XP for Blisterwood), and the tree may become a stump requiring a respawn timer before it can be chopped again.

Source: https://oldschool.runescape.wiki/w/Woodcutting

---

## 2. XP Formula

XP is a flat value per tree type — there is no per-session scaling or formula variation. The server awards `tree.xp` each time a log is received.

```
XP_awarded = tree.xp
```

Source: https://oldschool.runescape.wiki/w/Woodcutting#Experience

### Bonus XP Sources

| Source | Bonus | Condition |
|--------|-------|-----------|
| Woodcutting Guild passive | +7% all WC XP | Inside guild boundaries (level 60 WC required to enter) |
| Lumberjack hat | +0.4% | Wearing hat |
| Lumberjack top | +0.8% | Wearing top |
| Lumberjack legs | +0.6% | Wearing legs |
| Lumberjack boots | +0.2% | Wearing boots |
| Lumberjack full set bonus | +0.5% extra | All 4 pieces worn (total +2.5%) |

Source: https://oldschool.runescape.wiki/w/Lumberjack_outfit
Source: https://oldschool.runescape.wiki/w/Woodcutting_Guild

**Stacking**: Guild boost and Lumberjack outfit stack multiplicatively. Full Lumberjack set inside the guild yields approximately +9.675% XP boost.

---

## 3. Content List

### Trees

| Tree | Level | XP/Log | Log Item ID | Respawn (ticks) | Members | Wiki |
|------|-------|--------|-------------|-----------------|---------|------|
| Normal | 1 | 25.0 | 1511 | 8 | No | https://oldschool.runescape.wiki/w/Tree |
| Oak | 15 | 37.5 | 1521 | 23 | No | https://oldschool.runescape.wiki/w/Oak_tree |
| Willow | 30 | 67.5 | 1519 | 13 | No | https://oldschool.runescape.wiki/w/Willow_tree |
| Teak | 35 | 85.0 | 6333 | 27 | Yes | https://oldschool.runescape.wiki/w/Teak_tree |
| Maple | 45 | 100.0 | 1517 | 47 | Yes | https://oldschool.runescape.wiki/w/Maple_tree |
| Mahogany | 50 | 125.0 | 6332 | 37 | Yes | https://oldschool.runescape.wiki/w/Mahogany_tree |
| Arctic Pine | 54 | 40.0 | 10810 | 32 | Yes | https://oldschool.runescape.wiki/w/Arctic_pine_tree |
| Hollow | 45 | 82.5 | 3239 (bark) | 23 | Yes | https://oldschool.runescape.wiki/w/Hollow_tree |
| Blisterwood | 62 | 76.0 | N/A | null (permanent) | Yes | https://oldschool.runescape.wiki/w/Blisterwood_tree |
| Yew | 60 | 175.0 | 1515 | 165 | No | https://oldschool.runescape.wiki/w/Yew_tree |
| Magic | 75 | 250.0 | 1513 | 200 | Yes | https://oldschool.runescape.wiki/w/Magic_tree |
| Redwood | 90 | 380.0 | 19669 | 267 | Yes | https://oldschool.runescape.wiki/w/Redwood_tree |

**Notes**:
- Blisterwood: does not drop logs; XP granted directly each tick; tree never depletes. Requires completion of Sins of the Father quest.
- Hollow tree: drops Bark (item 3239) rather than logs.
- Redwood: restricted to Woodcutting Guild. Has two clickable interaction spots (trunk left, trunk right) — same tree object.
- Arctic Pine: found on Neitiznot/Jatizso only.

### Canonical Success Rate Formula

```
success = rand(255) < floor((player_wc_level * 2 + axe.woodcutting_bonus) / tree.difficulty)
```

Source: https://oldschool.runescape.wiki/w/Woodcutting#Success_chance

The `tree.difficulty` constant is stored in `data/skills/woodcutting.yaml` per tree. Higher difficulty = lower success chance.

---

## 4. Level Requirements

| Level | Unlock |
|-------|--------|
| 1 | Normal tree, Bronze/Iron axe |
| 5 | Steel axe (wield) |
| 10 | Black axe (wield) |
| 15 | Oak tree |
| 20 | Mithril axe (wield) |
| 30 | Willow tree, Adamant axe (wield) |
| 35 | Teak tree |
| 40 | Rune axe (wield) |
| 45 | Maple tree, Hollow tree |
| 50 | Mahogany tree |
| 54 | Arctic Pine tree |
| 60 | Yew tree, Woodcutting Guild access, Dragon axe (wield) |
| 61 | Infernal axe (wield) |
| 62 | Blisterwood tree (also requires Sins of the Father) |
| 71 | Crystal axe (wield) |
| 75 | Magic tree |
| 90 | Redwood tree |

Source: https://oldschool.runescape.wiki/w/Woodcutting#Trees

**Note**: Axes can be used from the inventory without the wield level requirement. Wield level only affects equipped use. A player with level 1 Attack can use a Rune axe from their inventory if they meet the WC level.

---

## 5. Required Items / Tools

### Axes

All axes can be wielded in the weapon slot or carried in the inventory. The server checks the equipped weapon first, then scans the inventory for the highest-tier available axe.

| Axe | Item ID | Wield Level (Attack) | WC Bonus | Notes |
|-----|---------|----------------------|----------|-------|
| Bronze axe | 1351 | 1 | 1 | — |
| Iron axe | 1349 | 1 | 2 | — |
| Steel axe | 1353 | 5 | 3 | — |
| Black axe | 1361 | 10 | 4 | — |
| Mithril axe | 1355 | 20 | 5 | — |
| Adamant axe | 1357 | 30 | 6 | — |
| Rune axe | 1359 | 40 | 7 | — |
| Dragon axe | 6739 | 60 | 8 | Has special attack (+3 WC level boost) |
| 3rd Age axe | 20011 | 60 | 8 | No special attack |
| Infernal axe | 13241 | 61 | 8 | Burns ~1/3 of logs received for FM XP |
| Crystal axe | 23674 | 71 | 9 | Degrades; must be recharged at Ilfeen/Eluned |

Source: https://oldschool.runescape.wiki/w/Axe

All item IDs verified against `data/skills/woodcutting.yaml` which cites osrsbox-db.

---

## 6. Tick Rate

- **Base cycle**: 4 ticks per chop attempt (2.4 seconds at 600ms/tick)
- **3-tick woodcutting**: Players can manipulate the tick cycle to receive a log every 3 ticks instead of 4 by using specific inventory actions (e.g., dropping an item) on specific sub-tick windows. This is an intentional OSRS mechanic and **must be supported** — do not add anti-exploit code that prevents it.

Source: https://oldschool.runescape.wiki/w/Woodcutting#3-tick_woodcutting
Source: https://oldschool.runescape.wiki/w/Tick_manipulation

**Implementation note**: The 4-tick interval is set in `meta.ticks_per_attempt: 4` in `data/skills/woodcutting.yaml`. The server-tick rule in `.claude/rules/server-tick.md` explicitly requires tick manipulation to be supported.

---

## 7. Special Mechanics

### 7a. Bird Nests

- **Drop rate**: 1 in 256 per log received (base chance; exact formula community-documented)
- **Drop type**: Random nest type (seeds, rings, empty)
- **Notes**: Always dropped as a physical ground item, never noted. Bird nests interrupt loot pickup if auto-pickup is active.
- **Nest item IDs**: Seed nest (5075), Ring nest (5070), Empty nest (5071)

Source: https://oldschool.runescape.wiki/w/Bird_nest

### 7b. Beaver Pet

- **Item ID**: 13322
- **Drop rate**: Approximately 1/72,000 at level 99 (community-approximated; Jagex has not published the exact formula)
- **Scaling**: Rate improves with higher Woodcutting level
- **Wiki**: https://oldschool.runescape.wiki/w/Beaver

**Parity note**: Exact per-level drop rate is unconfirmed — mark as `⚠️ unconfirmed` in parity checklist.

### 7c. Dragon Axe Special Attack

- **Special cost**: 100% special attack energy
- **Effect**: Boosts Woodcutting level by +3 visually and functionally for the duration
- **Duration**: 1 minute (100 ticks)
- **Impact on success rate**: The boosted WC level is used in the success rate formula, increasing chop speed

Source: https://oldschool.runescape.wiki/w/Dragon_axe#Special_attack

### 7d. Infernal Axe Burn Mechanic

- **Burn rate**: Approximately 1 in 3 logs received are burned (not dropped)
- **FM XP**: 50% of the log's Firemaking XP is granted when a log is burned by the axe
- **Log drop**: Burned logs do NOT appear in inventory or as ground item
- **Charge**: Infernal axe starts with 5,000 charges; each burn consumes 1 charge

Source: https://oldschool.runescape.wiki/w/Infernal_axe

### 7e. Forestry Events (Stub)

Forestry is a world event system layered on top of Woodcutting. Implementation is deferred — see `data/skills/woodcutting.yaml`:

```yaml
forestry_events:
  enabled: false  # stub — full implementation is a later phase
```

Source: https://oldschool.runescape.wiki/w/Forestry

### 7f. Woodcutting Guild Passive Boost

- **XP bonus**: +7% to all Woodcutting XP while inside the Woodcutting Guild boundaries
- **Entry requirement**: Level 60 Woodcutting (no item needed; passive area effect)
- **Stacking**: Stacks with Lumberjack outfit

Source: https://oldschool.runescape.wiki/w/Woodcutting_Guild

### 7g. Lumberjack Outfit

| Piece | Bonus |
|-------|-------|
| Lumberjack hat | +0.4% |
| Lumberjack top | +0.8% |
| Lumberjack legs | +0.6% |
| Lumberjack boots | +0.2% |
| Full set bonus | +0.5% extra |
| **Total (full set)** | **+2.5%** |

Source: https://oldschool.runescape.wiki/w/Lumberjack_outfit

---

## 8. Parity Target

**Overall target: 90%**

| Sub-system | Target | Priority |
|-----------|--------|---------|
| XP values (all 12 trees) | 100% exact | Critical — even 0.1 XP difference is a parity failure |
| Level requirements (all 12 trees) | 100% exact | Critical |
| Axe bonuses (all 11 axes) | 100% exact | Critical |
| Tick rate (4 ticks) | 100% exact | Critical |
| Bird nest rate (1/256) | 100% exact | High |
| WC Guild XP boost (+7%) | 100% exact | High |
| Lumberjack outfit (+2.5%) | 100% exact | High |
| Dragon axe special (+3 WC level) | 100% exact | High |
| Infernal axe burn rate (~1/3) | 95% | Medium — exact rate community-approximated |
| 3-tick woodcutting support | functional | Medium |
| Beaver pet drop rate | 80% | Low — exact formula unconfirmed |
| Forestry events | 0% (deferred) | Out of scope for Phase 2 |

The companion parity checklist is at `tests/parity/woodcutting-parity.md`.

---

## 9. Edge Cases

| # | Edge Case | Expected Behavior | Source |
|---|-----------|-------------------|--------|
| 1 | 3-tick woodcutting | Player can manipulate tick cycle (using specific inventory actions on sub-tick windows) to receive a log every 3 ticks instead of 4. Must be supported intentionally. | https://oldschool.runescape.wiki/w/Tick_manipulation |
| 2 | Dragon axe special | Boosts WC level by 3 for 100 ticks. The boosted level is used in the success rate formula. Does NOT increase XP per log — only chop speed. | https://oldschool.runescape.wiki/w/Dragon_axe#Special_attack |
| 3 | Infernal axe burn | Burns approximately 1/3 of received logs. The burned log grants 50% of its Firemaking XP. The log does NOT appear in inventory. Burn consumes 1 charge. | https://oldschool.runescape.wiki/w/Infernal_axe |
| 4 | Multiple choppers | Multiple players can chop the same tree simultaneously. Each player rolls the success check independently. The tree may deplete from any chopper's successful attempt. | https://oldschool.runescape.wiki/w/Woodcutting |
| 5 | Redwood two interaction spots | Redwood has two clickable spot objects (trunk left, trunk right) that are different object IDs but correspond to the same tree entity. Both spots must trigger the same WC action on the same tree. | https://oldschool.runescape.wiki/w/Redwood_tree |
| 6 | Blisterwood no log drop | Blisterwood does not produce a log item. XP (76.0) is granted directly each tick for a successful attempt. The tree never depletes (no respawn timer). Requires Sins of the Father completion. | https://oldschool.runescape.wiki/w/Blisterwood_tree |
| 7 | WC Guild +7% XP | While inside Woodcutting Guild boundaries, all WC XP gains are boosted by 7%. This stacks with Lumberjack outfit. The boost applies to all trees including Redwood. Entry requires level 60 WC. | https://oldschool.runescape.wiki/w/Woodcutting_Guild |
| 8 | Crystal axe degrades | Crystal axe degrades over use and must be recharged at Ilfeen or Eluned. When fully degraded, it reverts to a crystal tool seed. The WC bonus (9) applies only while the axe has charges. | https://oldschool.runescape.wiki/w/Crystal_axe |
| 9 | Bird nests always physical | Bird nests are always dropped as physical ground items (never noted). They appear on the ground beneath the player and can be missed if inventory is full and the player doesn't notice. | https://oldschool.runescape.wiki/w/Bird_nest |

---

## 10. Wiki Citation

Primary reference:
**https://oldschool.runescape.wiki/w/Woodcutting**

Supporting references:
- Trees: https://oldschool.runescape.wiki/w/Woodcutting#Trees
- XP table: https://oldschool.runescape.wiki/w/Woodcutting#Experience
- Success chance: https://oldschool.runescape.wiki/w/Woodcutting#Success_chance
- Axe list: https://oldschool.runescape.wiki/w/Axe
- Bird nest: https://oldschool.runescape.wiki/w/Bird_nest
- Beaver pet: https://oldschool.runescape.wiki/w/Beaver
- Dragon axe spec: https://oldschool.runescape.wiki/w/Dragon_axe#Special_attack
- Infernal axe: https://oldschool.runescape.wiki/w/Infernal_axe
- Crystal axe: https://oldschool.runescape.wiki/w/Crystal_axe
- 3-tick method: https://oldschool.runescape.wiki/w/Tick_manipulation
- WC Guild: https://oldschool.runescape.wiki/w/Woodcutting_Guild
- Lumberjack outfit: https://oldschool.runescape.wiki/w/Lumberjack_outfit
- Forestry: https://oldschool.runescape.wiki/w/Forestry
- Blisterwood: https://oldschool.runescape.wiki/w/Blisterwood_tree
- Redwood: https://oldschool.runescape.wiki/w/Redwood_tree
- Hollow tree: https://oldschool.runescape.wiki/w/Hollow_tree
- Tick manipulation: https://oldschool.runescape.wiki/w/Tick_manipulation
