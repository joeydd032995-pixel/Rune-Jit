---
name: implement-skill-action-framework
description: "Implements a specific OSRS skill or the full skill action framework. Pass skill name as argument (woodcutting, mining, fishing, agility, etc.) to implement that skill's plugin."
argument-hint: "[skill: woodcutting|mining|fishing|agility|herblore|cooking|crafting|smithing|runecraft|thieving|slayer|farming|construction|all]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /implement-skill-action-framework [skill]

Implements an OSRS skill plugin for the rsmod server.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Tick engine implemented | Yes (`src/server/engine/TickLoop.kt`) |
| osrsbox data imported | Yes |
| Skill GDD exists | Recommended |
| data/skills/[skill].yaml exists | Recommended |

## Phase 2: Load Skill Context

Spawn `wiki-researcher` to fetch:
- All actions for this skill (tools, resources, XP values)
- Level requirements
- Tick rates
- Special mechanics (pets, random events, bonus XP)

Spawn `skill-decomposer` to:
- Load 2006Scape reference implementation (Java enum patterns)
- Convert to Kotlin data classes
- Create `data/skills/[skill].yaml` with all values + wiki citations

## Phase 3: Implement Skill Plugin

Spawn the appropriate specialist agent:

| Skill | Agent |
|-------|-------|
| woodcutting | `woodcutting-specialist` |
| mining | `mining-specialist` |
| fishing | `fishing-specialist` |
| magic | `magic-spell-caster` |
| other | `skill-decomposer` |

The specialist implements:
1. `src/server/skills/[skill]/[Skill]Plugin.kt` — plugin registration
2. `src/server/skills/[skill]/[Skill]Action.kt` — action handler
3. `src/server/skills/[skill]/[Skill]Data.kt` — data definitions

## Phase 4: Verify Data-Driven Pattern

Verify no hardcoded values in skill logic (rule: `server-skills.md`):

```bash
grep -n '\b(25\.0|50\.0|87\.5|100\.0)\b' src/server/skills/[skill]/ || echo "No hardcoded XP found"
```

## Phase 5: Add Parity Tests

Write `tests/parity/skilling/[skill]-xp.test.kt`:
- XP per action matches wiki exactly
- Level requirements match wiki
- Tick rates match OSRS timing
- Special mechanics (pet rates, bonus XP) within 1%

## Phase 6: Run Parity Tests

```bash
./gradlew test --tests "*[Skill]Parity*"
```

Target: 90%+ on first implementation.

## Phase 7: Session State Update

```yaml
# production/session-state/skills-progress.yaml
[skill]:
  status: IMPLEMENTED
  parity_score: [X]%
  implemented_at: YYYY-MM-DD
  pending_items: []
```

## Error Recovery

| Error | Recovery |
|-------|---------|
| Missing data file | Spawn wiki-researcher to create it |
| Parity test failures | Route to specialist agent with specific test output |
| Compilation error | Check for missing ItemIds constants; run /import-osrsbox-complete |

## Nuances

- 3-tick woodcutting: animation reset on axe swap is intentional, must be supported
- Mining: gem drops are separate from ore; do not skip gem drop roll
- Fishing: barbarian fishing gives herblore and strength XP alongside fishing XP
- Magic: each spell has a distinct animation and projectile ID (from cache)
- Construction: room-building mechanic is complex; use /construction-room-builder skill

## Next Steps

After skill implementation:
- Run `/verify-mechanic-parity-1to1 [skill]` to get parity score
- Repeat for next skill: `/implement-skill-action-framework [next-skill]`
