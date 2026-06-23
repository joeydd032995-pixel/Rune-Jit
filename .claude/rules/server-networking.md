---
path: src/server/net/**
---

# Server Networking Rules

## Server-Authoritative

**The server is the authority on all game state.** The client sends intentions (walk here, attack this, use item); the server validates and executes them. Never trust client-supplied game state values.

## Packet ID Versioning

All packet IDs must be defined in `src/shared/protocol-defs.yaml` and versioned by OSRS revision. Never hardcode packet opcodes in handler code:

```kotlin
// ❌ WRONG: Hardcoded opcode
@PacketHandler(opcode = 73)  // breaks on revision update
fun handleWidgetAction(buf: ByteBuf) { ... }

// ✅ CORRECT: Loaded from protocol definitions
@PacketHandler(name = "WIDGET_ACTION")
fun handleWidgetAction(buf: ByteBuf) { ... }
```

## ISAAC Cipher Mandatory

All game packets after login must be ISAAC-encrypted. The cipher is initialized with session keys from the login block:

```kotlin
// Both encode (client→server) and decode (server→client) ciphers required
val decodeCipher = IsaacCipher(sessionKeys)
val encodeCipher = IsaacCipher(sessionKeys.map { it + 50 }.toIntArray())
```

## Packet Size Validation

Before reading packet fields, validate the buffer has sufficient bytes:

```kotlin
fun decode(opcode: Int, buf: ByteBuf): GamePacket? {
    val def = PacketDefinitions.get(opcode) ?: return null
    if (def.size > 0 && buf.readableBytes() < def.size) return null  // wait for more data
    return def.handler.decode(buf)
}
```

## Flood Protection

Rate-limit incoming packets per player:
- Max 50 packets per tick per player
- Any player exceeding this is disconnected
- Login attempts: max 5 per IP per minute

## Prohibited

- No plaintext packet opcodes after login (must use ISAAC)
- No accepting walk coordinates from client without server-side pathability check
- No trusting client-reported inventory state
- No storing session keys in logs
