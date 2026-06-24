# Fishing GDD — Status: DRAFT

## 1. Mechanic Overview

Fishing is a gathering skill where players use fishing equipment at fishing spots (NPC objects in the world) to catch raw fish. Each attempt takes 5 game ticks (3.0 seconds) and always succeeds — unlike Woodcutting and Mining, there is no per-tick success roll. The player must hold the correct tool and any required bait in their inventory.
Source: https://oldschool.runescape.wiki/w/Fishing

## 2. XP/Reward Formula

```
fish_caught = true  (no success roll; guaranteed after ticksPerAttempt)
xp_awarded = spot.xp  (flat value per fish, no modifiers)
```

One fish is always caught after 5 ticks at a valid spot. No guild bonus, no outfit bonus (deferred).
Source: https://oldschool.runescape.wiki/w/Fishing#Experience_table

## 3. Content List

| Spot | Level | XP | Method | Tool ID | Bait ID | Fish ID | Members |
|------|-------|----|--------|---------|---------|---------|---------|
| SHRIMP | 1 | 10.0 | SMALL_NET | 303 | 0 | 317 | No |
| SARDINE | 5 | 20.0 | BAIT | 307 | 313 | 327 | No |
| HERRING | 10 | 30.0 | BAIT | 307 | 313 | 345 | No |
| ANCHOVIES | 15 | 40.0 | SMALL_NET | 303 | 0 | 321 | No |
| MACKEREL | 16 | 20.0 | BIG_NET | 305 | 0 | 353 | No |
| TROUT | 20 | 50.0 | FLY_FISH | 309 | 314 | 335 | No |
| COD | 23 | 45.0 | BIG_NET | 305 | 0 | 341 | No |
| PIKE | 25 | 60.0 | BAIT | 307 | 313 | 349 | No |
| SALMON | 30 | 70.0 | FLY_FISH | 309 | 314 | 331 | No |
| TUNA | 35 | 80.0 | HARPOON | 311 | 0 | 359 | No |
| LOBSTER | 40 | 90.0 | CAGE | 301 | 0 | 377 | No |
| BASS | 46 | 100.0 | BIG_NET | 305 | 0 | 363 | No |
| SWORDFISH | 50 | 100.0 | HARPOON | 311 | 0 | 371 | No |
| MONKFISH | 62 | 120.0 | SMALL_NET | 303 | 0 | 7944 | Yes |
| SHARK | 76 | 110.0 | HARPOON | 311 | 0 | 383 | Yes |
| ANGLERFISH | 82 | 120.0 | SANDWORMS | 25129 | 25195 | 13439 | Yes |
| DARK_CRAB | 85 | 130.0 | LOBSTER_POT | 301 | 0 | 11934 | Yes |

Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_spots
XP source: https://oldschool.runescape.wiki/w/Fishing#Experience_table

## 4. Level Requirements

Minimum level 1 (Shrimps). Maximum gated: Dark Crab at level 85. Full table above.
Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_spots

## 5. Required Items/Tools

A fishing tool must be in the player's inventory (fishing tools are not wielded). Some spots also require bait consumed per catch.

| Method | Tool | Tool ID | Bait | Bait ID |
|--------|------|---------|------|---------|
| SMALL_NET | Small fishing net | 303 | None | — |
| BIG_NET | Big fishing net | 305 | None | — |
| BAIT | Fishing rod | 307 | Fishing bait | 313 |
| FLY_FISH | Fly fishing rod | 309 | Feather | 314 |
| CAGE | Lobster pot | 301 | None | — |
| HARPOON | Harpoon | 311 | None | — |
| SANDWORMS | Fishing rod (sandworms) | 25129 | Sandworm | 25195 |
| LOBSTER_POT | Lobster pot | 301 | None | — |

Source: https://oldschool.runescape.wiki/w/Fishing#Fishing_equipment

## 6. Tick Rate

5 game ticks per catch (3.0 seconds).
Source: https://oldschool.runescape.wiki/w/Fishing#Mechanics

Note: 3-tick fishing (tick manipulation) is a legitimate OSRS mechanic and must not be blocked by the engine. It works by cancelling and re-clicking the fishing action on a 3-tick cycle. The server-tick.md rule explicitly states tick manipulation must be supported.
Source: https://oldschool.runescape.wiki/w/Tick_manipulation#Fishing

## 7. Special Mechanics

**Heron pet**: 1/257,803 per catch (flat rate, does not scale with level).
Source: https://oldschool.runescape.wiki/w/Heron#Drop_rate

**Bait consumption**: One bait item consumed per successful catch (BAIT, FLY_FISH, SANDWORMS methods). If bait runs out mid-session, fishing stops.
Source: https://oldschool.runescape.wiki/w/Fishing#Bait

**Barbarian Fishing** (Otto's Grotto): Leaping fish require Agility/Strength prerequisites and award bonus Agility and Strength XP in addition to Fishing XP. Deferred to Phase 5 — requires multi-skill XP award system.
Source: https://oldschool.runescape.wiki/w/Barbarian_Fishing

**Infernal Eel** (Mor Ul Rek): Level 80 required; smashing yields Tokkul, lava scale shards, or onyx bolt tips. Deferred to Phase 5.
Source: https://oldschool.runescape.wiki/w/Infernal_eel

**Aerial Fishing** (Molch Island): Uses Cormorant with fish chunks; catches multiple fish types by region. Deferred to Phase 5.
Source: https://oldschool.runescape.wiki/w/Aerial_fishing

**Dark Crab bait**: Requires dark fishing bait (item ID 11948) consumed per catch. Currently stubbed as bait_item_id=0 until Wilderness world-state is implemented.
Source: https://oldschool.runescape.wiki/w/Dark_fishing_bait

## 8. Parity Target

90% — XP values, level requirements, bait consumption, tool checks, and pet roll must be exact.
All 17 spot definitions are data-driven from fishing.yaml with wiki citations per value.

## 9. Edge Cases

- No tool in inventory → fishing stops immediately before starting (level gate checked first, then tool check)
- Inventory full → action stops before fish is added (checked first in FishingAction.process)
- Bait runs out mid-session → action stops at the next tick when bait check fails
- Level boost from potions applies (`getBoostedLevel` used for level gate check in FishingAction)
- 3-tick fishing must not be blocked by artificial cooldowns (server-tick.md rule)
- Heron pet check skipped if inventory is full (prevents silent pet loss)
- Fishing spots share right-click options: SMALL_NET and BIG_NET both use "Net"; CAGE and LOBSTER_POT both use "Cage"
- Object IDs are empty IntArray until /load-osrs-cache-full; FishingPlugin skips registration for empty arrays

## 10. Wiki Citation

https://oldschool.runescape.wiki/w/Fishing
