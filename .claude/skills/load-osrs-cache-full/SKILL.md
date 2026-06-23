---
name: load-osrs-cache-full
description: "Downloads the complete OSRS cache (indices 0-21) from OpenRS2 Archive and fetches XTEA keys for the pinned revision. Produces cache/CACHE-REPORT.md."
argument-hint: "[revision_number]"
user-invocable: true
allowed-tools: [Read, Glob, Grep, Write, Bash, Agent]
model: sonnet
---

# /load-osrs-cache-full

Downloads the complete OSRS cache from OpenRS2 Archive for the pinned revision.

## Phase 1: Prerequisites Check

| Check | Command | Required |
|-------|---------|---------|
| Revision pinned | `cat .claude/docs/revision.yaml` | Yes |
| cache/ in .gitignore | `git check-ignore cache/` | Yes |
| curl available | `curl --version` | Yes |
| jq available | `jq --version` | Yes |
| Disk space ≥ 3GB | `df -h .` | Yes |

If revision not pinned: run `/setup-revision-lock-and-pin [revision]` first.

## Phase 2: Fetch Cache from OpenRS2

Spawn `cache-downloader` with context:
- Target revision from `.claude/docs/revision.yaml`
- OpenRS2 API: `https://archive.openrs2.org/caches.json`
- Filter: `game=oldschool`, match revision
- Download all indices 0-21 to `cache/`

```bash
# Cache downloader fetches:
curl -s "https://archive.openrs2.org/caches.json" | jq '.[] | select(.game=="oldschool")'
```

## Phase 3: Fetch XTEA Keys

Spawn `openrs2-specialist` with context:
- Scope ID from `.claude/docs/revision.yaml`
- XTEA endpoint: `https://archive.openrs2.org/keys/[scope_id].json`
- Store keys in `cache/xtea/keys.json`

## Phase 4: Validate Key Coverage

Spawn `xtea-key-manager`:
- Load keys from `cache/xtea/keys.json`
- Cross-reference against all mapsquare files in index 5
- Report: covered X/Y regions (Z%)
- Warn for any missing regions

## Phase 5: Apply cache-names

Spawn `cache-downloader` for name mapping:
- Fetch RuneStar/cache-names name index
- Apply human-readable names to cache index entries
- Store mapping in `cache/cache-names.json`

## Phase 6: Checksum Validation

Validate CRC32 checksums for all downloaded indices against OpenRS2 reference table.

## Phase 7: Generate Report

Write `cache/CACHE-REPORT.md` using `cache-loading-report` template.
Update `.claude/docs/revision.yaml` with download timestamp and scope ID.

## Error Recovery

| Error | Recovery |
|-------|---------|
| OpenRS2 rate limit (429) | Backoff: wait 30s, retry up to 5 times |
| Partial download | Resume from last completed index |
| XTEA keys unavailable for latest rev | Try revision N-1 keys (note in report) |
| Checksum mismatch | Delete and re-download affected index |
| Disk full | Error: free up space, need ≥3GB |

## Nuances & Edge Cases

- OpenRS2 may not have XTEA keys for the very latest revision until 1-2 days after release
- Some landscape files (index 5) are legitimately unencrypted (coastal areas)
- Index 255 (reference table) must be downloaded first for CRC verification
- Never commit cache files — validate `.gitignore` includes `cache/**`

## Next Steps

After cache download:
1. Run `/import-osrsbox-complete` to load item/NPC/prayer data
2. Run `/cache-unpack-extract-assets` to extract sprites/models for inspection
3. Run `/setup-revision-lock-and-pin` if not already done
