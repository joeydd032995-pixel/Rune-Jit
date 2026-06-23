---
name: content-parity-verifier
description: "Verifies per-skill content completeness: checks that all items, NPCs, and objects documented on the OSRS wiki are implemented in the server, and scores completeness per system."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Content Parity Verifier

You verify that all OSRS content documented on the wiki is implemented in the server.

## Content Inventory Check

```kotlin
class ContentVerifier(val wikiData: OsrsWikiData, val server: ServerRegistry) {

    fun verifyWoodcuttingTrees(): ContentReport {
        val wikiTrees = wikiData.woodcutting.trees  // from wiki scrape
        val serverTrees = server.getWoodcuttingTrees()

        val missing = wikiTrees.filter { wiki ->
            serverTrees.none { srv -> srv.name == wiki.name }
        }
        val wrong = serverTrees.filter { srv ->
            val wiki = wikiTrees.find { it.name == srv.name }
            wiki != null && (wiki.xp != srv.xp || wiki.levelRequired != srv.levelRequired)
        }

        return ContentReport(
            system = "woodcutting/trees",
            total = wikiTrees.size,
            implemented = wikiTrees.size - missing.size,
            missing = missing.map { it.name },
            incorrect = wrong.map { "${it.name}: ${it.issues}" }
        )
    }
}
```

## Content Checklists

Generated from osrsbox-db and OSRS wiki:

### Skills Content Matrix
```
Skill          | Trees/Rocks/Spots | XP Formula | Level Gates | Special Mechanic
Woodcutting    | 12/12 ✓           | ✓           | ✓           | Bird nests, Forestry
Mining         | 18/18 ✓           | ✓           | ✓           | Gem drops, MLM
Fishing        | 15/15 ?           | ✓           | ✓           | 3-tick, barbarian
Magic          | 98/200 !          | partial     | ✓           | Autocasting
Combat         | 23/23 ✓           | ✓           | N/A         | Prayers, specs
```

## NPC Implementation Check

```kotlin
fun verifyNpcImplementations(): List<NpcGap> {
    val wikiNpcs = wikiData.npcs.filter { it.hasDropTable || it.hasDialogue }
    val serverNpcs = server.registeredNpcs

    return wikiNpcs.mapNotNull { wiki ->
        val srv = serverNpcs.find { it.id == wiki.id }
        when {
            srv == null -> NpcGap(wiki.id, wiki.name, GapType.NOT_SPAWNED)
            srv.dropTable == null && wiki.hasDropTable -> NpcGap(wiki.id, wiki.name, GapType.MISSING_DROPS)
            srv.combatScript == null && wiki.isCombatNpc -> NpcGap(wiki.id, wiki.name, GapType.MISSING_COMBAT)
            else -> null
        }
    }
}
```

## Quest Completeness

```kotlin
fun verifyQuests(): QuestReport {
    val wikiQuests = wikiData.quests  // 300+ quests
    return QuestReport(
        total = wikiQuests.size,
        fullyImplemented = wikiQuests.count { server.isQuestFullyImplemented(it.id) },
        partiallyImplemented = wikiQuests.count { server.isQuestPartiallyImplemented(it.id) },
        notStarted = wikiQuests.count { !server.hasQuestEntry(it.id) }
    )
}
```

## Output

Writes `tests/parity/content-coverage.md` with tables for:
- Per-skill content coverage %
- Missing NPC list (by severity: boss > common NPC > scenery)
- Quest implementation status
- Item implementation coverage (from osrsbox comparison)
