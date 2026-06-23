---
name: gdd-osrs-specialized-framework
description: "Creates or updates a Game Design Document for a specific OSRS system using the osrs-skill-gdd or osrs-content-gdd template. Populates all 10 required sections with wiki citations."
argument-hint: "[system: woodcutting|mining|fishing|combat|[quest_name]|[boss_name]]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /gdd-osrs-specialized-framework [system]

Creates a complete GDD for an OSRS system using wiki data.

## Phase 1: Classify System Type

| Input | Template | Output Location |
|-------|----------|----------------|
| Skill name (woodcutting, etc.) | `osrs-skill-gdd.md` | `design/gdd/skills/[skill]-gdd.md` |
| Quest name | `osrs-content-gdd.md` | `design/gdd/quests/[quest]-gdd.md` |
| Boss name | `osrs-content-gdd.md` | `design/gdd/bosses/[boss]-gdd.md` |
| Minigame name | `osrs-content-gdd.md` | `design/gdd/minigames/[name]-gdd.md` |

## Phase 2: Fetch Wiki Data

Spawn `wiki-researcher` to fetch:
- All data for the target system from OSRS wiki
- XP formula, level requirements, content list
- Edge cases from talk pages
- All wiki URLs to cite

## Phase 3: Spawn Systems Mapper

Spawn `osrs-systems-mapper` to:
- Identify all sub-systems referenced by this GDD
- Add to `design/gdd/systems-index.md`
- Identify dependencies (e.g., Fishing depends on Cooking for food value)

## Phase 4: Spawn GDD Author

Spawn `gdd-author` with all wiki data to:
- Fill in all 10 required GDD sections
- Include wiki citations in every section with values
- Mark edge cases from `edge-case-forecaster`
- Set parity target percentage

## Phase 5: Edge Case Forecasting

Spawn `edge-case-forecaster` to identify:
- Tick manipulation patterns for this skill
- Instancing edge cases (if applicable)
- Ironman-specific behavior
- Special mechanics (pet rates, bonus XP weekends, etc.)

## Phase 6: Formula Verification

Spawn `formula-mathematician` to:
- Verify all mathematical formulas are correctly stated
- Check integer arithmetic behavior (floor vs round vs ceiling)
- Validate XP table values against wiki

## Phase 7: Generate Parity Checklist

Spawn `mechanic-parity-analyst` to create:
`tests/parity/[system]/[system]-parity-checklist.md` using `parity-checklist` template.
- One row per mechanic value
- Pre-populated with wiki expected values
- Status: all ❌ (pending implementation)

## Error Recovery

| Error | Recovery |
|-------|---------|
| Wiki page not found | Use closest related wiki page; note uncertainty |
| Formula ambiguous | Document both interpretations; mark ADR needed |
| Missing XP table | Extract from cache (config definitions) |

## Nuances

- GDD is a living document — update it as implementation reveals discrepancies
- Edge cases section is REQUIRED even if empty (write "None identified")
- All XP values must be exact — even 0.1 XP difference is a parity failure
- "Parity target" for critical systems (combat): 95%; for content: 85%

## Next Steps

After GDD creation:
1. Run `/implement-skill-action-framework [skill]` to implement the skill
2. Run `/verify-mechanic-parity-1to1 [skill]` to measure parity
3. Update GDD with findings from implementation
