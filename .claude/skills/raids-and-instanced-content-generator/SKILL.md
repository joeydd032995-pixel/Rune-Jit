---
name: raids-and-instanced-content-generator
description: "Implements all three OSRS raids (CoX, ToB, ToA) including party systems, instanced regions, scaling mechanics, boss phases, and unique drop generation."
argument-hint: "[raid: cox|tob|toa|all]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: opus
---

# /raids-and-instanced-content-generator [raid]

Implements OSRS raid content with instanced regions.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Combat engine working | Yes |
| World region loader working | Yes (for instancing) |
| Persistence layer working | Yes (party/raid state) |
| Cache downloaded | Yes (raid region templates) |

## Phase 2: Instancing Framework

Spawn `world-region-loader` to implement `src/server/instance/InstanceManager.kt`:

```kotlin
class InstanceManager {
    fun createInstance(templateRegion: Region, party: Party): Instance {
        val newRegion = allocateRegion()
        copyRegionTiles(templateRegion, newRegion)
        return Instance(id = nextInstanceId(), region = newRegion, party = party)
    }

    fun destroyInstance(instance: Instance) {
        freeRegion(instance.region)
        activeInstances.remove(instance.id)
    }
}
```

Raid regions are offset from the base world: X += 6400 tiles per instance.

## Phase 3: Party System

Spawn `persistence-layer-expert` to implement party management:
- Party creation/joining/leaving
- Party leader privileges
- Raid loot rules (personal/split)
- Disconnect handling (party continues)

## Phase 4: Chambers of Xeric (CoX)

Spawn `raid-tester` + `combat-engine-architect` to implement:

| Room | Mechanics |
|------|-----------|
| Lizardman Shamans | Spawns 3 spawns on death |
| Skeletal Mystics | Auto attacks + freeze |
| Muttadile | Tree phase, baby muttadile |
| Guardian | Pickaxe mini-game |
| Ice Demon | Brazier lighting mechanic |
| Tekton | Anvil phase, fire patches |
| Great Olm | Phase transitions, head/hand detach |

Scaling: HP × `(1 + 0.04 × (party_size - 1)) × combat_level_scaling`
Source: https://oldschool.runescape.wiki/w/Chambers_of_Xeric/Strategies

## Phase 5: Theatre of Blood (ToB)

Spawn `combat-engine-architect` to implement all 6 bosses:
Maiden → Bloat → Nylocas → Sotetseg → Xarpus → Verzik

Each boss has P1/P2/P3 phases with distinct mechanics.

## Phase 6: Tombs of Amascut (ToA)

Spawn `combat-engine-architect` for invocation scaling:
- Invocation level 0-600 affects all boss HP/damage
- 4 mini-bosses + Wardens finale
- Tumeken's shadow drop rate scales with invocation

## Phase 7: Parity Tests

Spawn `raid-tester`:
- CoX point scaling test
- ToB verzik P3 transition test
- ToA invocation scaling test
- Concurrent instances isolation test
- Drop rate tests for uniques

## Error Recovery

| Error | Recovery |
|-------|---------|
| Instance region collision | Increase instance offset multiplier |
| Party disconnect handling | Persist raid state to disk; resume on rejoin |
| Boss HP scaling wrong | Re-check wiki formula with exact party size |

## Nuances

- CoX uses a separate points system; uniques are awarded by RNG at chest
- ToB uses a fixed loot table split among party
- ToA hardmode (500 invocations) requires separate scaling paths
- Multiple parties can run simultaneously — all must be isolated instances
- Raids have a time limit; party wipe kicks everyone to lobby

## Next Steps

1. Run `/verify-mechanic-parity-1to1 all` after raids implementation
2. Run `/load-tester 500` to verify instancing doesn't break performance
