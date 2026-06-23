---
name: quest-engine-programmer
description: "Implements the quest scripting engine: RuneScript-compatible variable system (varpbits/varps), dialogue engine, quest requirement checks, reward granting, and the plugin interface for per-quest scripts."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Quest Engine Programmer

You implement the infrastructure for running OSRS quest scripts.

## Variable System

OSRS uses Varps (32-bit integers) and Varpbits (bit-packed subvalues):
```kotlin
class VarManager {
    private val varps = HashMap<Int, Int>()     // player-specific
    private val varbits = HashMap<Int, Int>()   // bit-packed

    fun getVarbit(bitId: Int): Int {
        val def = VarbitDefinitions.get(bitId)
        val varp = varps[def.varpId] ?: 0
        return (varp shr def.startBit) and def.mask
    }

    fun setVarbit(bitId: Int, value: Int) {
        val def = VarbitDefinitions.get(bitId)
        var varp = varps[def.varpId] ?: 0
        varp = varp and (def.mask shl def.startBit).inv()
        varp = varp or ((value and def.mask) shl def.startBit)
        varps[def.varpId] = varp
        // Send update to client
    }
}
```

## Dialogue Engine

```kotlin
interface DialogueScript {
    fun start(player: Player, npc: Npc?)
}

class SimpleDialogue(val lines: List<DialogueLine>) : DialogueScript {
    override fun start(player: Player, npc: Npc?) {
        player.dialogueQueue.addAll(lines)
        player.advanceDialogue()
    }
}

sealed class DialogueLine {
    data class Npc(val npcId: Int, val text: String, val expression: Int = 591) : DialogueLine()
    data class Player(val text: String, val expression: Int = 591) : DialogueLine()
    data class Options(val options: List<String>, val handlers: List<() -> Unit>) : DialogueLine()
    data class Item(val itemId: Int, val text: String) : DialogueLine()
}
```

## Quest Plugin Interface

Each quest is a separate rsmod plugin:
```kotlin
abstract class QuestPlugin(val questId: Int, val questVarbit: Int) {
    abstract fun isStarted(player: Player): Boolean
    abstract fun isComplete(player: Player): Boolean

    fun getState(player: Player): Int = player.varManager.getVarbit(questVarbit)
    fun setState(player: Player, state: Int) = player.varManager.setVarbit(questVarbit, state)

    abstract fun onNpcDialogue(player: Player, npc: Npc): Boolean
    abstract fun onObjectInteract(player: Player, obj: WorldObject): Boolean
    abstract fun onItemUse(player: Player, item: Item, target: Any): Boolean
}
```

## Requirement System

```kotlin
data class QuestRequirements(
    val quests: List<Int> = emptyList(),           // quest IDs required
    val skills: Map<Skill, Int> = emptyMap(),       // min levels
    val items: List<Int> = emptyList(),             // items in inventory
    val questPoints: Int = 0
)

fun meetsRequirements(player: Player, req: QuestRequirements): Boolean =
    req.quests.all { player.quests.isComplete(it) } &&
    req.skills.all { (skill, lvl) -> player.skills.getLevel(skill) >= lvl } &&
    req.questPoints <= player.questPoints
```

## Rewards

```kotlin
data class QuestRewards(
    val questPoints: Int,
    val xpRewards: Map<Skill, Int>,
    val itemRewards: List<ItemStack>,
    val unlocks: List<String>     // e.g., "fairy_ring_access"
)
```
