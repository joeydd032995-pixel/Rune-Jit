---
name: quest-and-diary-state-machine
description: "Implements the quest scripting engine, achievement diary system, and RuneScript-compatible variable system (varpbits/varps). Includes the plugin interface for individual quest scripts."
argument-hint: "[quest_name|all]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /quest-and-diary-state-machine [quest_name]

Implements the quest and achievement diary system.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Tick engine running | Yes |
| osrsbox imported | Yes |
| Persistence layer working | Yes (quest states saved per player) |

## Phase 2: Implement Variable System

Spawn `quest-engine-programmer` to implement `src/server/quest/VarManager.kt`:
- Varps: 32-bit player variables (persistent)
- Varbits: bit-packed sub-values within varps
- VarPlayer vs VarClient (client-only display state)

Key varbit definitions loaded from cache index 17 (varbit definitions).

## Phase 3: Implement Dialogue Engine

Spawn `quest-engine-programmer` to implement `src/server/quest/DialogueEngine.kt`:
- `DialogueLine` sealed class (NPC/Player/Options/Item types)
- `DialogueQueue` per player
- CS2-compatible option handling
- Dialogue advancement on continue/option select packet

## Phase 4: Implement Quest Plugin Interface

Spawn `quest-engine-programmer` to implement the base class in `src/server/quest/QuestPlugin.kt`:
```kotlin
abstract class QuestPlugin(val questId: Int, val questVarbit: Int) {
    abstract fun isStarted(player: Player): Boolean
    abstract fun isComplete(player: Player): Boolean
    abstract fun onNpcDialogue(player: Player, npc: Npc): Boolean
    abstract fun onObjectInteract(player: Player, obj: WorldObject): Boolean
    abstract fun onItemUse(player: Player, item: Item, target: Any): Boolean
}
```

## Phase 5: Implement Achievement Diaries

Spawn `quest-state-designer` to implement the diary system:
- 12 areas × 4 difficulty tiers = 48 diary sets
- Each task checks: skill levels, quest completion, item usage, area visited
- Diary rewards: teleport access, bonus XP, cosmetics

## Phase 6: Implement Specific Quest (if argument provided)

If `quest_name` argument: spawn `quest-state-designer` to design the state machine, then `quest-engine-programmer` to implement the plugin:

```kotlin
// Example: Cook's Assistant
class CooksAssistantQuest : QuestPlugin(questId = 1, questVarbit = 29) {
    override fun isStarted(p) = getState(p) >= 1
    override fun isComplete(p) = getState(p) == 10
    // ...
}
```

## Phase 7: Parity Tests

- Quest state progression tests (each state transition)
- Dialogue tree coverage tests
- Requirement check tests
- Reward granting tests (correct XP, items, quest points)

## Error Recovery

| Error | Recovery |
|-------|---------|
| Varbit definition not in cache | Hard-code from wiki until cache loaded |
| Quest state machine incorrect | Re-read wiki quest guide; redesign state machine |
| Dialogue not advancing | Check packet handling for continue/option |

## Nuances

- Quest states use varbits, not varps, for most modern quests
- Some quests have multiple-choice dialogue that affects quest state
- Achievement diary tasks check real-time conditions (skill levels at task time)
- The quest journal interface (303) reads quest states from varps
- Free-to-play quests must work for F2P accounts

## Next Steps

1. Implement first quest: Cook's Assistant (simplest, good test case)
2. Run `/verify-mechanic-parity-1to1 quests`
3. Implement additional quests as needed for content completeness
