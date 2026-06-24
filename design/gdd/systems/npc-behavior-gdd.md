# NPC Behavior GDD

**Status**: IMPLEMENTED (wander, aggro, melee combat, protection prayer block)
**Parity Target**: 90%
**Wiki Source**: https://oldschool.runescape.wiki/w/Non-player_character

---

## 1. Mechanic Overview

NPCs (Non-Player Characters) are server-controlled entities that populate the OSRS world. Each NPC operates as a state machine with three primary behaviours: wandering near its spawn point, detecting and aggressing nearby players, and engaging in melee combat once a target is acquired. NPCs are driven by the server tick engine (600 ms per tick) using per-NPC `TickQueue` instances — identical in architecture to the player's scheduler.

---

## 2. XP / Reward Formula

NPC combat does not itself award XP. XP is awarded to the player from damage dealt TO the NPC, using the same formulas as player vs player combat. Deferred to the NPC death handler (pending item).

Player XP when hitting an NPC (melee):
```
attack_xp    = damage * 4.0  (Accurate style)
strength_xp  = damage * 4.0  (Aggressive style)
defence_xp   = damage * 4.0  (Defensive style)
hp_xp        = damage * (4.0 / 3.0)  (all melee styles)
```
Source: https://oldschool.runescape.wiki/w/Hitpoints#Experience

---

## 3. Content List

| NPC Category | Wander | Aggro | Melee | Ranged | Magic | Notes |
|---|---|---|---|---|---|---|
| Standard melee NPC (Goblin, Guard, etc.) | Yes | Conditional | Yes | No | No | Implemented |
| Aggressive NPC (Moss Giant, etc.) | Yes | Yes (combat level check) | Yes | No | No | Implemented (level check deferred) |
| Boss NPCs (General Graardor, KBD, Zulrah) | No | Yes | Yes | Yes/No | Yes/No | Phase transitions deferred |
| Ranged NPCs (Dark Ranger, etc.) | Yes | Conditional | No | Yes | No | Ranged attacks deferred |
| Magic NPCs (Wizard, etc.) | Yes | Conditional | No | No | Yes | Magic attacks deferred |
| Slayer-only NPCs (Abyssal Demon, etc.) | Yes | Task-only | Yes | No | No | Slayer task check deferred |

Source: https://oldschool.runescape.wiki/w/Non-player_character

---

## 4. Level Requirements

NPCs have no player-facing level requirements (they are server entities, not player skills). The relevant level mechanics are:

**NPC aggression check** (combat level):
```
is_aggressive = npc.combatLevel > player.combatLevel * 2 - 1
```
NPCs whose combat level is more than double the player's combat level are aggressive to that player.
Source: https://oldschool.runescape.wiki/w/Aggressive

**10-minute aggression timer**: After a player has been in the same area for 10 minutes (600 ticks), aggressive NPCs de-aggro and ignore that player.
Source: https://oldschool.runescape.wiki/w/Aggressive#Mechanics
Status: Deferred (pending items).

---

## 5. Required Items / Tools

NPC behaviour requires no player items. Server-side dependencies:

| Component | Status |
|---|---|
| `TickQueue` / `TickQueueImpl` | Implemented |
| `BreadthFirstSearch` pathfinder | Implemented |
| `CombatFormulas.maxMeleeHit` | Implemented |
| `PrayerSet.isProtectedFrom` | Implemented |
| NPC definition data (osrsbox) | Deferred — `/import-osrsbox-complete` |
| NPC update block protocol packets | Deferred — `/protocol-packet-engine` |

---

## 6. Tick Rate

| Action | Tick Rate | Seconds | Source |
|---|---|---|---|
| Wander decision | 1 tick (with 1-in-8 move chance) | 0.6 s | https://oldschool.runescape.wiki/w/Non-player_character#Wandering |
| Aggro scan | 1 tick per check | 0.6 s | https://oldschool.runescape.wiki/w/Aggressive |
| NPC melee attack | 4 ticks (default) | 2.4 s | https://oldschool.runescape.wiki/w/Combat#Attack_speed |
| NPC movement (follow) | 1 tile per tick | 0.6 s | https://oldschool.runescape.wiki/w/Non-player_character#Following |

Attack speed varies by NPC; the default of 4 ticks applies to most melee NPCs. Fast NPCs (e.g. cave crawlers: 3 ticks) and slow NPCs (e.g. Jad: 8 ticks) require per-NPC definition loading from osrsbox.

---

## 7. Special Mechanics

**Protection Prayers (overhead) in PvM — 100% block**
In PvM, overhead protection prayers (Protect from Melee, Protect from Missiles, Protect from Magic) grant 100% damage reduction against the matching attack style.
Source: https://oldschool.runescape.wiki/w/Protect_from_Melee
Source: https://oldschool.runescape.wiki/w/Prayer#Protection_prayers

**Wander radius**
NPCs wander within 5 tiles (Chebyshev) of their spawn point. The random move rolls 1-in-8 each tick. If the cache is absent (graceful-absent pattern) movement bypasses BFS clip checks.
Source: https://oldschool.runescape.wiki/w/Non-player_character#Wandering

**Aggression radius**
Default 5-tile Chebyshev aggro radius. The nearest player within this radius is targeted.
Source: https://oldschool.runescape.wiki/w/Aggressive

**Combat follow distance**
NPCs give up pursuit and cancel combat when the target exceeds 10 tiles Chebyshev distance.
Source: https://oldschool.runescape.wiki/w/Non-player_character#Following

**Multi-combat zones**
In multi-combat areas multiple NPCs may attack the same player simultaneously. Single-combat zones restrict to one attacker. Deferred.
Source: https://oldschool.runescape.wiki/w/Multi-way_combat

**Boss phase transitions**
Bosses such as General Graardor increase attack frequency below 50% HP. Pattern-based bosses (Zulrah) cycle through defined forms. Deferred to Phase 5.
Source: https://oldschool.runescape.wiki/w/General_Graardor

**NPC respawn**
On death, NPCs respawn at their spawn point after a fixed timer (varies per NPC, typically 30–60 seconds). Deferred (requires world object tick queue).
Source: https://oldschool.runescape.wiki/w/Respawn

---

## 8. Parity Target

| Sub-system | Target | Notes |
|---|---|---|
| Wander (5-tile radius, 1-in-8 chance) | 90% | Clip checking deferred to cache presence |
| Aggro detection (5-tile Chebyshev) | 95% | Combat level comparison deferred to NpcDefinition |
| Melee combat (hit roll, max hit) | 90% | NPC stats simplified until osrsbox loaded |
| Protection prayer 100% block (PvM) | 100% | Hard requirement per CLAUDE.md |
| NPC pathfinding (BFS, 1 tile/tick) | 90% | Clip flags absent without cache |
| **Global weighted** | **90%** | Required to deploy |

---

## 9. Edge Cases

**Prayer flicking during NPC combat**
Players can flick Protect from Melee on and off within the same tick. The NPC attack rolls happen server-side after player prayer is evaluated, so if the prayer is active when the NPC's `NpcCombatAction.attack()` runs, the protection takes effect. This is correct OSRS behaviour.
Source: https://oldschool.runescape.wiki/w/Prayer#Prayer_flicking

**NPC size > 1 (multi-tile bosses)**
Bosses such as Kalphite Queen (2×2) and Olm (large) use `npc.size > 1`. The `BreadthFirstSearch.findPath` accepts a `size` parameter and checks multi-tile clearance for each step. The `Npc.size` field defaults to 1; real values come from NPC definitions (deferred).
Source: https://oldschool.runescape.wiki/w/Kalphite_Queen

**Spawning at (0, 0)**
`Npc.spawnX` and `spawnY` default to the constructor `x`/`y` values. If an NPC is created at coordinates (0, 0) (e.g. in unit tests) it is safe — the wander action simply wanders around (0, 0) without crashing.

**Dead NPC still scheduled**
If a combat action fires on the same tick the NPC dies, `isDead` is checked first and the action returns false immediately. This is a hard guarantee enforced in every action's `process()` method.

**Aggro re-scan after combat ends**
`NpcAggroAction` holds a reference to the launched `NpcCombatAction`. If combat ends (target dies, target logs out), the aggro action will re-scan on its next tick cycle and may re-aggro. Re-aggro after target death requires `isDead` guard on the player — implemented via `player.isDead` filter in `playerSource`.

---

## 10. Wiki Citation

Primary source: https://oldschool.runescape.wiki/w/Non-player_character

Supporting sources:
- Aggression mechanics: https://oldschool.runescape.wiki/w/Aggressive
- Protect from Melee: https://oldschool.runescape.wiki/w/Protect_from_Melee
- Protection prayers (100% PvM block): https://oldschool.runescape.wiki/w/Prayer#Protection_prayers
- Prayer flicking: https://oldschool.runescape.wiki/w/Prayer#Prayer_flicking
- Combat attack speed: https://oldschool.runescape.wiki/w/Combat#Attack_speed
- NPC respawn: https://oldschool.runescape.wiki/w/Respawn
- Multi-way combat: https://oldschool.runescape.wiki/w/Multi-way_combat
- General Graardor phases: https://oldschool.runescape.wiki/w/General_Graardor
- Max melee hit formula: https://oldschool.runescape.wiki/w/Maximum_melee_hit
- Hitpoints XP: https://oldschool.runescape.wiki/w/Hitpoints#Experience
