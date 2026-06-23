# Cache Loading Report

**Date**: YYYY-MM-DD
**OSRS Revision**: [revision]
**OpenRS2 Scope ID**: [scope_id]
**Cache Location**: `cache/`

---

## Download Summary

| Item | Status | Details |
|------|--------|---------|
| Cache files | ✅/❌ | [X] indices, [Y]MB total |
| XTEA keys | ✅/❌ | [X]% region coverage |
| Checksum validation | ✅/❌ | [pass/fail details] |
| cache-names applied | ✅/❌ | [X] files named |

---

## Cache Index Coverage

| Index | Name | Files | Size | Status |
|-------|------|-------|------|--------|
| 0 | Animations | | | ✅/❌ |
| 1 | Skeletons | | | |
| 2 | Configs | | | |
| 3 | Interfaces | | | |
| 4 | Sound Effects | | | |
| 5 | Maps | | | ⚠️ XTEA needed |
| 6 | Music | | | |
| 7 | Models | | | |
| 8 | Sprites | | | |
| 9 | Textures | | | |
| 255 | Reference | | | |

---

## XTEA Key Coverage

**Total mapsquares**: [X]
**Keys available**: [Y] ([Z]%)
**Missing regions**: [list or "none"]

Missing regions will appear black/empty on the minimap. Run `/load-osrs-cache-full` to retry XTEA key fetch.

---

## Checksums

| File | Expected CRC | Actual CRC | Match? |
|------|-------------|-----------|--------|
| main_file_cache.dat2 | | | |
| main_file_cache.idx255 | | | |

---

## Revision Pin

```yaml
# Recorded in .claude/docs/revision.yaml
revision: [revision]
scope_id: [scope_id]
downloaded_at: YYYY-MM-DD
gamepack_hash: [sha256]
```

---

## Notes

[Any anomalies, missing indices, or special notes about this revision's cache.]
