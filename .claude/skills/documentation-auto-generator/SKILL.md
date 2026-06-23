---
name: documentation-auto-generator
description: "Auto-generates Architecture Decision Records from @ADR annotations in source code, updates GDDs from implementation discoveries, produces the API reference, and writes the final docs/architecture/ documentation set."
argument-hint: "[scope: adrs|gdds|api|all]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /documentation-auto-generator [scope]

Generates the complete documentation set from code annotations and session history.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| `src/` has implementation code | Yes — docs generated from code |
| `design/gdd/` has GDD stubs | Recommended |
| `.claude/logs/` has session history | Yes |
| `docs/architecture/` directory exists | Created if missing |

```bash
mkdir -p docs/architecture docs/api
```

## Phase 2: Extract @ADR Annotations

Spawn `documentation-generator` to scan source code for `@ADR` annotations:

```kotlin
// Example annotation usage in source:
// @ADR(id="001", title="BFS over A* for pathfinding", status="ACCEPTED")
// Decision: Use BFS (breadth-first search) for tile pathfinding.
// Rationale: OSRS uses BFS-equivalent logic; A* produces different paths
//            causing parity failures. Verified against wiki movement tests.
// Source: https://oldschool.runescape.wiki/w/Pathfinding
```

Scanner:
```kotlin
fun extractADRAnnotations(srcDir: Path): List<ADRAnnotation> {
    return srcDir.walk()
        .filter { it.extension == "kt" }
        .flatMap { file ->
            file.readLines()
                .windowed(5)
                .filter { it.first().contains("@ADR") }
                .map { parseADRBlock(it, file) }
        }
        .toList()
}
```

For each `@ADR` found, write `docs/architecture/adr-[id]-[slug].md` using the `osrs-adr` template.

## Phase 3: Discover Undocumented Decisions

Spawn `documentation-generator` to scan git log for undocumented decisions:

```bash
git log --oneline --since="30 days ago" \
  | grep -E "(formula|parity|tick|desync|OSRS|wiki)" \
  | head -50
```

For each significant commit message referencing a formula or wiki deviation, create a draft ADR in `docs/architecture/draft/` for human review.

## Phase 4: Update GDDs from Implementation

Spawn `gdd-author` to update existing GDDs based on implementation discoveries:

For each `design/gdd/skills/[skill]-gdd.md`:
1. Read current GDD
2. Read corresponding `src/server/skills/[skill]/` implementation
3. Compare GDD spec vs actual implementation
4. Update GDD sections where implementation differs from spec (with explanation)
5. Mark discrepancies as `STATUS: DEVIATED — ADR reference: [id]`

```markdown
<!-- Example GDD update -->
## XP Formula
**Spec**: floor(experience_table[level] * action_xp_multiplier)
**Implemented**: As spec ✅

## Special Mechanics
**Spec**: Bird nests fall with 1/256 chance per log chopped
**Implemented**: 1/256 base rate, modified by Woodcutting level beyond 99
**Status**: DEVIATED — see ADR-007 (level scaling not in spec; discovered during testing)
```

## Phase 5: Generate API Reference

Spawn `data-schema-engineer` to generate `docs/api/` from Kotlin interfaces:

Scan `src/server/` for public interfaces and data classes:

```kotlin
// @ApiDoc(since="phase-2")
interface SkillActionPlugin {
    fun onActionStart(player: Player, action: SkillAction): Boolean
    fun onTick(player: Player, action: SkillAction): TickResult
    fun onActionEnd(player: Player, action: SkillAction, result: EndReason)
}
```

Output `docs/api/skill-action-plugin.md`:
```markdown
# SkillActionPlugin

Plugin interface for implementing skill actions.

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `onActionStart` | player, action | Boolean | Called when player starts action. Return false to cancel. |
| `onTick` | player, action | TickResult | Called each tick while action active. |
| `onActionEnd` | player, action, result | void | Called when action ends for any reason. |

**Since**: Phase 2
```

## Phase 6: Generate Protocol Documentation

Spawn `protocol-sync-validator` to read `src/shared/protocol-defs.yaml` and generate:
`docs/api/packet-reference.md` — all packet opcodes, sizes, field layouts.

```markdown
# Packet Reference — Revision 225

## Server → Client Packets

### [43] Player Update (Variable Short)
Sends movement + appearance update for all visible players.
Fields: ...
```

## Phase 7: Produce Final docs/architecture/ Set

Spawn `documentation-generator` to write:

| File | Content |
|------|---------|
| `docs/architecture/README.md` | Architecture overview, diagram (ASCII), phase progress |
| `docs/architecture/performance-profile.md` | Latest profiling results from session logs |
| `docs/architecture/parity-status.md` | Current parity scores per system |
| `docs/architecture/revision.md` | Pinned revision, gamepack hash, XTEA coverage |
| `docs/architecture/systems-map.md` | Dependency graph of all OSRS systems |

## Phase 8: Validation

Verify documentation completeness:
```
docs/architecture/
├── README.md ✓
├── adr-001-*.md ✓ (at least 5 ADRs expected)
├── performance-profile.md ✓
├── parity-status.md ✓
├── revision.md ✓
└── systems-map.md ✓

design/gdd/
├── skills/ (at least 3 skill GDDs) ✓
├── systems-index.md ✓
└── templates/ ✓
```

## Error Recovery

| Error | Recovery |
|-------|---------|
| No @ADR annotations found | Create 5 mandatory ADRs for: pathfinding (BFS), tick engine, ISAAC, cache format, parity threshold |
| GDD not updated in months | Flag as stale; spawn `gdd-author` with latest implementation |
| API interface changed without annotation | Scan git diff for interface changes; create draft ADR |
| Session logs missing | Use git log as fallback for decision history |

## Nuances

- Documentation is generated from code, not written separately — the code IS the source of truth
- ADRs are immutable once in ACCEPTED state — create a NEW ADR to supersede, never edit old ones
- GDD "DEVIATED" status is not a failure — it documents intentional implementation choices
- Parity-status.md must reference the most recent parity test run timestamp
- All ADRs require a wiki source URL or a clear reason why no wiki source exists

## Next Steps

1. Review all `docs/architecture/draft/` ADRs and promote to ACCEPTED or REJECTED
2. Share `docs/architecture/README.md` with testers as system overview
3. Run `/automated-parity-testing-suite` and update `parity-status.md`
4. Set up docs auto-generation to run on each push (CI step)
