---
name: setup-revision-lock-and-pin
description: "Pins the OSRS client/server revision number, downloads the corresponding gamepack for mapping analysis, and records the revision in .claude/docs/revision.yaml."
argument-hint: "[revision_number]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /setup-revision-lock-and-pin [revision]

Pins the OSRS revision for this Rune-Jit installation.

## Phase 1: Prerequisites Check

| Check | Required |
|-------|---------|
| Java 17+ | Yes |
| curl, jq | Yes |
| Legal compliance OK | Yes (run legal-compliance-checker) |

If no revision argument provided: fetch latest from OpenRS2:
```bash
curl -s "https://archive.openrs2.org/caches.json" | \
  jq '[.[] | select(.game=="oldschool")] | sort_by(.id) | last | .id'
```

## Phase 2: Legal Compliance Check

Spawn `legal-compliance-checker`:
- Verify no Jagex-proprietary content will be committed
- Check that `.gitignore` covers cache/, gamepacks/, *.xtea
- Confirm private/educational use framing in CLAUDE.md

## Phase 3: Locate OpenRS2 Scope ID

Spawn `revision-pin-specialist`:
```bash
curl -s "https://archive.openrs2.org/caches.json" | \
  jq '.[] | select(.game=="oldschool" and .build.major==[REVISION])'
```

Extract: `scope_id`, `builds[].major`, `timestamp`.

## Phase 4: Download Gamepack

Spawn `gamepack-analyst`:
- Download gamepack JAR for target revision (if available on OpenRS2)
- Store in `gamepacks/` (gitignored)
- Compute SHA-256 hash

## Phase 5: Produce Mappings

Spawn `reverse-engineer`:
- Analyze gamepack to identify class/field/method mappings
- Output `src/shared/mappings.yaml`
- Output `src/shared/mappings-report.md`

## Phase 6: Write Revision File

Write `.claude/docs/revision.yaml`:
```yaml
revision: [REVISION]
scope_id: [SCOPE_ID]
pinned_at: YYYY-MM-DD
gamepack_hash: [SHA256]
openrs2_url: "https://archive.openrs2.org/caches/[scope_id]/disk.zip"
notes: "Pinned via /setup-revision-lock-and-pin"
```

## Phase 7: Validate Lock

```bash
# Verify revision file was written
cat .claude/docs/revision.yaml
# Expected: revision matches argument
```

## Error Recovery

| Error | Recovery |
|-------|---------|
| Revision not on OpenRS2 | Try adjacent revision (±1) |
| Gamepack download fails | Skip gamepack; note manual download needed |
| Mappings analysis fails | Create stub mappings.yaml for later completion |

## Nuances

- The revision number corresponds to the OSRS client JAR version (e.g., 225 → client revision 225)
- OpenRS2 scope IDs increment; a new scope is created for each revision
- Some revisions have multiple scopes (different server builds)
- The `gamepack_hash` is used to detect revision drift
- After pinning: all agents read revision from `.claude/docs/revision.yaml`

## Next Steps

1. Run `/load-osrs-cache-full` to download cache for this revision
2. Run `/import-osrsbox-complete` to load item data
3. Run `/protocol-packet-engine` to set up networking
