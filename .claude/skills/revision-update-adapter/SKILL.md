---
name: revision-update-adapter
description: "Detects a new OSRS revision, diffs obfuscated gamepack mappings against the pinned revision, identifies breaking packet or formula changes, and produces a migration plan to update the server."
argument-hint: "[new_revision: e.g. 226]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /revision-update-adapter [new_revision]

Adapts the server to a new OSRS gamepack revision.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| `.claude/docs/revision.yaml` exists | Yes (current pinned revision) |
| `tools/mappings/` directory with current mappings | Yes |
| New gamepack JAR available | Yes |
| `tools/cache-utils/` deobfuscation tooling | Yes |

Read `.claude/docs/revision.yaml` to know the current pinned revision and compare against `[new_revision]`.

```yaml
# .claude/docs/revision.yaml format
revision: 225
gamepack_hash: "sha256:abc123..."
cache_build: 225
pinned_at: "2024-01-01T00:00:00Z"
protocol_version: 225
```

## Phase 2: Download New Gamepack

Spawn `gamepack-analyst` to:
1. Fetch new gamepack from OpenRS2 Archive (`archive.openrs2.org/caches.json`)
2. Filter for `game=oldschool` and `build=[new_revision]`
3. Download to `gamepacks/rev-[new_revision].jar` (gitignored — listed in `.gitignore`)
4. Compute SHA-256 checksum

## Phase 3: Deobfuscate and Extract Mappings

Spawn `reverse-engineer` to diff new gamepack against old:

```kotlin
data class FieldMapping(
    val obfuscatedClass: String,
    val obfuscatedField: String,
    val deobfuscatedName: String,
    val multiplier: Long,  // Knuth multiplicative constant
    val type: String
)

data class MappingDiff(
    val added: List<FieldMapping>,
    val removed: List<FieldMapping>,
    val changed: List<Pair<FieldMapping, FieldMapping>>,  // old → new
    val multiplierChanges: List<Triple<String, Long, Long>>  // name, oldMultiplier, newMultiplier
)
```

Output diff to `tools/mappings/rev-[new_revision]-diff.yaml`.

## Phase 4: Protocol Diff Analysis

Spawn `protocol-sync-validator` to identify packet changes:

| Change Type | Impact | Action Required |
|-------------|--------|----------------|
| Packet opcode changed | HIGH — clients won't connect | Update protocol-defs.yaml |
| Packet size changed | HIGH — parsing fails | Update packet handler |
| New packet added | MEDIUM — unhandled opcode | Add stub handler |
| Packet removed | LOW | Remove dead handler |
| Formula constant changed | HIGH | Update formula, run parity tests |
| Cache index structure changed | HIGH | Update cache reading code |

Output: `tools/mappings/protocol-diff-rev[old]-to-rev[new].md`

## Phase 5: Cache Structure Diff

If cache revision changed:

```bash
# Compare archive.openrs2.org master index checksums
curl -s "https://archive.openrs2.org/caches.json" \
  | jq ".[] | select(.builds[].major == $NEW_REV and .game == \"oldschool\")" \
  > tools/mappings/cache-rev-${NEW_REV}.json
```

Spawn `cache-downloader` to:
- Download new cache to `cache/rev-[new_revision]/`
- Fetch updated XTEA keys from OpenRS2
- Diff master index checksums to find changed cache entries

## Phase 6: Generate Migration Plan

Write `docs/architecture/revision-migration-[old]-to-[new].md`:

```markdown
# Revision Migration: [old] → [new]

## Breaking Changes
- Packet opcode 123 → 145 (affects: login handler)
- Field `Player.level` multiplier: 0x12345 → 0x67890

## New Content
- [list new items/NPCs from cache diff]

## Required Code Changes
1. Update `src/server/net/PacketHandlers.kt`: opcode 123 → 145
2. Update `tools/mappings/current.yaml` multipliers
3. Re-run `/import-osrsbox-complete` for new item data

## Parity Risk
- Combat formula constants: LOW (unchanged)
- Skill XP tables: NONE (unchanged)
- NPC aggression ranges: MEDIUM (2 NPCs changed)
```

## Phase 7: Apply Safe Changes

Spawn `revision-update-adapter` agent to apply low-risk changes automatically:
- Update `tools/mappings/current.yaml` with new multipliers
- Update `src/shared/protocol-defs.yaml` with changed opcodes
- Update `.claude/docs/revision.yaml` with new revision number

Flag HIGH-risk changes for manual review with exact file:line references.

## Phase 8: Re-run Parity Tests

Spawn `regression-suite-runner` after changes applied:
- Run `/automated-parity-testing-suite` on combat + networking scope
- Any new failures = revision broke parity; document in migration notes

## Error Recovery

| Error | Recovery |
|-------|---------|
| New revision not on OpenRS2 yet | Wait 24h; OpenRS2 typically archives within hours |
| XTEA keys unavailable for new revision | Use n-1 revision cache; note missing regions |
| Deobfuscation fails on new revision | Obfuscation pattern changed; escalate to reverse-engineer agent |
| Multiplier extraction fails | Brute-force search ±2^32 range for Knuth constants |

## Nuances

- Jagex typically releases updates Wednesday mornings (GMT)
- Packet opcodes are randomly shuffled each revision — never hardcode them
- Multipliers also change each revision — always extract from gamepack
- New OSRS content is usually released as new cache entries without protocol changes
- XTEA keys for new regions may not appear on OpenRS2 for 1-7 days

## Next Steps

1. Run `/setup-revision-lock-and-pin [new_revision]` after migration complete
2. Run `/load-osrs-cache-full` to refresh cache for new revision
3. Run `/automated-parity-testing-suite all` to verify no regressions
4. Update `CONTRIBUTING.md` with new revision number
