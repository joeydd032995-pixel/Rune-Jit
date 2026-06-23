# Revision Changelog: [OLD_REV] → [NEW_REV]

**Date Jagex Released**: YYYY-MM-DD
**Date Adapted**: YYYY-MM-DD
**Scope ID**: [openrs2 scope ID]
**Breaking Changes**: Yes/No

---

## Summary

[2-3 sentences describing the major changes in this update]

---

## New Content

| Content | Type | Notes |
|---------|------|-------|
| | Quest/Item/NPC/Skill/Area | |

---

## Changed Formulas

| Formula | Old Value | New Value | Source |
|---------|-----------|----------|--------|
| | | | [wiki URL] |

---

## Packet Changes

| Packet | Change Type | Old Opcode | New Opcode | Notes |
|--------|------------|-----------|-----------|-------|
| | ADDED/REMOVED/OPCODE_CHANGED/SIZE_CHANGED | | | |

---

## Cache Index Changes

| Index | Change | Notes |
|-------|--------|-------|
| | New files / Removed files / Modified | |

---

## Client Mappings Changes

| Class/Field | Old Obfuscated Name | New Obfuscated Name | Type |
|------------|--------------------|--------------------|------|
| | | | class/field/method |

---

## Required Server Changes

| Change | Priority | Agent | Status |
|--------|----------|-------|--------|
| Update packet opcodes in protocol-defs.yaml | HIGH | revision-update-adapter | 🔲 |
| Update mappings.yaml | HIGH | reverse-engineer | 🔲 |
| [specific feature change] | MEDIUM | [agent] | 🔲 |

---

## Parity Impact

**Estimated parity delta from this update**: [+/-X]%
**New content to implement**: [N] items
**Changed mechanics to re-verify**: [list]

---

## Migration Script

```bash
# Auto-patch steps (run after downloading new revision cache):
/setup-revision-lock-and-pin [NEW_REV]
/load-osrs-cache-full
/revision-update-adapter
```

---

## Notes

[Any unusual changes, deprecated features, or known issues with this revision]
