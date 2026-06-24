# Woodcutting Parity Checklist

**GDD Reference**: `design/gdd/skills/woodcutting-gdd.md`
**Data Source**: `data/skills/woodcutting.yaml`
**Primary Wiki**: https://oldschool.runescape.wiki/w/Woodcutting
**Status Legend**: ✅ implemented / ❌ missing / ⚠️ wrong or unconfirmed

---

## Tree XP Values

Source: https://oldschool.runescape.wiki/w/Woodcutting#Experience

| # | Tree | Expected XP | Actual | Status | Notes |
|---|------|-------------|--------|--------|-------|
| 1 | Normal | 25.0 | — | ❌ | — |
| 2 | Oak | 37.5 | — | ❌ | — |
| 3 | Willow | 67.5 | — | ❌ | — |
| 4 | Teak | 85.0 | — | ❌ | — |
| 5 | Maple | 100.0 | — | ❌ | — |
| 6 | Mahogany | 125.0 | — | ❌ | — |
| 7 | Arctic Pine | 40.0 | — | ❌ | — |
| 8 | Hollow (bark) | 82.5 | — | ❌ | — |
| 9 | Blisterwood | 76.0 | — | ❌ | Direct XP (no log drop) |
| 10 | Yew | 175.0 | — | ❌ | — |
| 11 | Magic | 250.0 | — | ❌ | — |
| 12 | Redwood | 380.0 | — | ❌ | — |

---

## Tree Level Requirements

Source: https://oldschool.runescape.wiki/w/Woodcutting#Trees

| # | Tree | Expected Level | Actual | Status | Notes |
|---|------|---------------|--------|--------|-------|
| 13 | Normal | 1 | — | ❌ | — |
| 14 | Oak | 15 | — | ❌ | — |
| 15 | Willow | 30 | — | ❌ | — |
| 16 | Teak | 35 | — | ❌ | — |
| 17 | Maple | 45 | — | ❌ | — |
| 18 | Hollow | 45 | — | ❌ | — |
| 19 | Mahogany | 50 | — | ❌ | — |
| 20 | Arctic Pine | 54 | — | ❌ | — |
| 21 | Yew | 60 | — | ❌ | — |
| 22 | Blisterwood | 62 | — | ❌ | Also requires Sins of the Father |
| 23 | Magic | 75 | — | ❌ | — |
| 24 | Redwood | 90 | — | ❌ | WC Guild only |

---

## Tree Respawn Timers

Source: https://oldschool.runescape.wiki/w/Woodcutting#Trees (converted from seconds at 0.6s/tick)

| # | Tree | Expected (ticks) | Actual | Status | Notes |
|---|------|-----------------|--------|--------|-------|
| 25 | Normal | 8 | — | ❌ | ~5s |
| 26 | Oak | 23 | — | ❌ | ~14s |
| 27 | Willow | 13 | — | ❌ | ~8s |
| 28 | Teak | 27 | — | ❌ | ~16s |
| 29 | Maple | 47 | — | ❌ | ~28s |
| 30 | Hollow | 23 | — | ❌ | ~14s |
| 31 | Mahogany | 37 | — | ❌ | ~22s |
| 32 | Arctic Pine | 32 | — | ❌ | ~19s |
| 33 | Yew | 165 | — | ❌ | ~99s |
| 34 | Blisterwood | null (permanent) | — | ❌ | Never depletes |
| 35 | Magic | 200 | — | ❌ | ~120s |
| 36 | Redwood | 267 | — | ❌ | ~160s |

---

## Axe Bonuses (WC Bonus / Success Rate)

Source: https://oldschool.runescape.wiki/w/Axe

| # | Axe | Expected WC Bonus | Actual | Status | Notes |
|---|-----|------------------|--------|--------|-------|
| 37 | Bronze axe (1351) | 1 | — | ❌ | — |
| 38 | Iron axe (1349) | 2 | — | ❌ | — |
| 39 | Steel axe (1353) | 3 | — | ❌ | — |
| 40 | Black axe (1361) | 4 | — | ❌ | — |
| 41 | Mithril axe (1355) | 5 | — | ❌ | — |
| 42 | Adamant axe (1357) | 6 | — | ❌ | — |
| 43 | Rune axe (1359) | 7 | — | ❌ | — |
| 44 | Dragon axe (6739) | 8 | — | ❌ | — |
| 45 | 3rd Age axe (20011) | 8 | — | ❌ | — |
| 46 | Infernal axe (13241) | 8 | — | ❌ | — |
| 47 | Crystal axe (23674) | 9 | — | ❌ | — |

---

## Axe Wield Level Requirements

Source: https://oldschool.runescape.wiki/w/Axe

| # | Axe | Expected Attack Level | Actual | Status | Notes |
|---|-----|-----------------------|--------|--------|-------|
| 48 | Bronze | 1 | — | ❌ | — |
| 49 | Iron | 1 | — | ❌ | — |
| 50 | Steel | 5 | — | ❌ | — |
| 51 | Black | 10 | — | ❌ | — |
| 52 | Mithril | 20 | — | ❌ | — |
| 53 | Adamant | 30 | — | ❌ | — |
| 54 | Rune | 40 | — | ❌ | — |
| 55 | Dragon | 60 | — | ❌ | — |
| 56 | 3rd Age | 60 | — | ❌ | — |
| 57 | Infernal | 61 | — | ❌ | — |
| 58 | Crystal | 71 | — | ❌ | — |

---

## Tick Rate

Source: https://oldschool.runescape.wiki/w/Woodcutting

| # | Mechanic | Expected | Actual | Status | Notes |
|---|----------|---------|--------|--------|-------|
| 59 | Base chop interval | 4 ticks (2.4s) | — | ❌ | meta.ticks_per_attempt in YAML |
| 60 | 3-tick woodcutting | Supported (3 ticks via tick manipulation) | — | ❌ | Must not be blocked |

---

## Bird Nest Drop Rate

Source: https://oldschool.runescape.wiki/w/Bird_nest

| # | Mechanic | Expected | Actual | Status | Notes |
|---|----------|---------|--------|--------|-------|
| 61 | Bird nest base rate | 1/256 per log | — | ❌ | — |
| 62 | Nest is physical item | Always ground item (never noted) | — | ❌ | — |

---

## XP Boosts

Source: https://oldschool.runescape.wiki/w/Woodcutting_Guild | https://oldschool.runescape.wiki/w/Lumberjack_outfit

| # | Mechanic | Expected | Actual | Status | Notes |
|---|----------|---------|--------|--------|-------|
| 63 | WC Guild XP boost | +7% all WC XP inside guild | — | ❌ | Area-based, requires level 60 entry |
| 64 | Lumberjack hat | +0.4% | — | ❌ | — |
| 65 | Lumberjack top | +0.8% | — | ❌ | — |
| 66 | Lumberjack legs | +0.6% | — | ❌ | — |
| 67 | Lumberjack boots | +0.2% | — | ❌ | — |
| 68 | Lumberjack full set bonus | +0.5% extra (total +2.5%) | — | ❌ | Only when all 4 pieces worn |

---

## Special Axe Mechanics

Source: https://oldschool.runescape.wiki/w/Dragon_axe#Special_attack | https://oldschool.runescape.wiki/w/Infernal_axe | https://oldschool.runescape.wiki/w/Crystal_axe

| # | Mechanic | Expected | Actual | Status | Notes |
|---|----------|---------|--------|--------|-------|
| 69 | Dragon axe special attack | Boosts WC level by +3 for 100 ticks | — | ❌ | Costs 100% spec bar |
| 70 | Dragon axe spec affects success rate | Boosted level used in formula | — | ❌ | WC level in formula becomes level+3 |
| 71 | Infernal axe burn rate | ~1/3 logs burned | — | ❌ | Exact rate community-approximated |
| 72 | Infernal axe FM XP | 50% of log's FM XP on burn | — | ❌ | — |
| 73 | Infernal axe charge | 5000 charges; each burn = 1 charge | — | ❌ | — |
| 74 | Crystal axe degrades | Degrades over use; recharged at Ilfeen/Eluned | — | ❌ | — |

---

## Blisterwood Tree Special Behaviour

Source: https://oldschool.runescape.wiki/w/Blisterwood_tree

| # | Mechanic | Expected | Actual | Status | Notes |
|---|----------|---------|--------|--------|-------|
| 75 | Blisterwood no log drop | XP granted directly; no log item | — | ❌ | drops_log: false in YAML |
| 76 | Blisterwood permanent | Tree never depletes | — | ❌ | respawn_ticks: null |
| 77 | Blisterwood quest gate | Requires Sins of the Father | — | ❌ | — |

---

## Beaver Pet

Source: https://oldschool.runescape.wiki/w/Beaver

| # | Mechanic | Expected | Actual | Status | Notes |
|---|----------|---------|--------|--------|-------|
| 78 | Beaver drop rate at 99 | ~1/72,000 | — | ⚠️ | Unconfirmed — community approximation; Jagex has not published exact formula |
| 79 | Beaver scales with level | Rate improves at higher levels | — | ❌ | — |

---

## Success Rate Formula

Source: https://oldschool.runescape.wiki/w/Woodcutting#Success_chance

| # | Mechanic | Expected | Actual | Status | Notes |
|---|----------|---------|--------|--------|-------|
| 80 | Success formula | `rand(255) < floor((wc_level*2 + axe_bonus) / tree_difficulty)` | — | ❌ | Integer arithmetic, floor before comparison |

---

## Total Rows: 80

All rows are ❌ (not yet implemented) except row 78 which is ⚠️ (unconfirmed source value).

Implementation target: rows 59–80 complete = Woodcutting plugin ready for Phase 6 parity run.
