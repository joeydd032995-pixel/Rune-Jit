# Packet Definition: [PACKET_NAME]

**Opcode**: [decimal] (0x[hex])
**Direction**: Client → Server | Server → Client
**Size**: [fixed bytes] | VAR_BYTE | VAR_SHORT
**OSRS Revision Introduced**: [revision]
**Source**: [RuneStar leak reference or reverse-engineered]

---

## Description

[1-2 sentences: what does this packet do?]

---

## Field Layout

| Offset | Size | Type | Field Name | Description |
|--------|------|------|-----------|-------------|
| 0 | [N] | [type] | [name] | [description] |

**Field Types**: byte, short, int, long, string, varint

---

## Example

**Hex dump** (raw bytes, ISAAC-encrypted opcode not shown):
```
[hex bytes]
```

**Decoded**:
```
field1 = [value]
field2 = [value]
```

---

## Server Handler

```kotlin
// src/server/net/handlers/[PacketName]Handler.kt
@PacketHandler(name = "[PACKET_NAME]")
class [PacketName]Handler : ServerPacketHandler {
    override fun handle(player: Player, buf: ByteBuf) {
        val [field1] = buf.readShort()
        val [field2] = buf.readInt()
        // ... handle
    }
}
```

---

## Validation Rules

| Field | Validation | Action if Invalid |
|-------|-----------|------------------|
| [field] | [constraint] | [disconnect/ignore] |

---

## Notes

[Edge cases, version history, related packets]

**Related packets**:
- [RELATED_PACKET_1]
- [RELATED_PACKET_2]
