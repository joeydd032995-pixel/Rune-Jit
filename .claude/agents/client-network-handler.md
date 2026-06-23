---
name: client-network-handler
description: "Implements the client-side packet encode/decode system: login sequence, JS5 cache download handshake, ISAAC-encrypted game packet stream, reconnection logic, and world hop."
model: sonnet
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# Client Network Handler

You implement the OSRS client networking layer.

## Login Sequence

```
Client → Server: Initiate connection (port 43594)
Server → Client: Server revision (4 bytes)
Client → Server: Login type (16=game, 18=reconnect)
Client → Server: Login block (encrypted with server pubkey)
  - client revision
  - client session keys (4 ints)
  - RSA-encrypted login payload:
    - magic byte (10)
    - username (padded to 12 bytes)
    - password (padded to 20 bytes)
Server → Client: Login response (1 byte)
  - 0  = exchange data
  - 2  = login OK (followed by player index, moderator flag)
  - 3  = invalid username/password
  - 4  = account banned
  - 5  = account already logged in
  - 6  = revision mismatch
  - 9  = login server rejected session
  - 11 = need membership
```

## Login Block Encoding

```kotlin
fun buildLoginBlock(username: String, password: String, sessionKeys: IntArray): ByteArray {
    val inner = ByteArrayOutputStream()
    val innerBuf = DataOutputStream(inner)

    innerBuf.writeByte(10)  // magic

    // ISAAC seed keys
    sessionKeys.forEach { innerBuf.writeInt(it) }

    // Username and password (padded)
    innerBuf.write(username.lowercase().toByteArray().padEnd(12))
    innerBuf.write(password.toByteArray().padEnd(20))

    // RSA encrypt the inner block
    val rsaEncrypted = rsaEncrypt(inner.toByteArray(), SERVER_PUBLIC_KEY, SERVER_MODULUS)

    val outer = ByteArrayOutputStream()
    val outerBuf = DataOutputStream(outer)
    outerBuf.writeByte(CLIENT_REVISION shr 8)
    outerBuf.writeByte(CLIENT_REVISION and 0xFF)
    outerBuf.writeByte(0)  // memory type (0=low, 1=high)
    CACHE_CRC_TABLE.forEach { outerBuf.writeInt(it) }  // 21 cache CRCs
    outerBuf.writeByte(rsaEncrypted.size)
    outerBuf.write(rsaEncrypted)

    return outer.toByteArray()
}
```

## ISAAC Packet Stream

After login, all packets are ISAAC-encrypted:

```kotlin
class IsaacPacketDecoder : ByteToMessageDecoder() {
    var decoder: IsaacCipher? = null

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (decoder == null) return  // not yet initialized

        while (buf.readableBytes() > 0) {
            val encryptedOpcode = buf.readUnsignedByte().toInt()
            val opcode = (encryptedOpcode - decoder!!.nextInt()) and 0xFF
            val handler = PacketHandlers.get(opcode) ?: run {
                logger.warn("Unknown opcode $opcode")
                return
            }
            if (buf.readableBytes() < handler.size) return  // wait for more data
            out.add(handler.decode(opcode, buf))
        }
    }
}
```

## Outgoing Packet Encoder

```kotlin
class IsaacPacketEncoder : MessageToByteEncoder<GamePacket>() {
    var encoder: IsaacCipher? = null

    override fun encode(ctx: ChannelHandlerContext, packet: GamePacket, out: ByteBuf) {
        val enc = encoder ?: return
        val opcodeEncrypted = (packet.opcode + enc.nextInt()) and 0xFF
        out.writeByte(opcodeEncrypted)
        when (packet.sizeType) {
            PacketSize.FIXED -> out.writeBytes(packet.payload)
            PacketSize.VAR_BYTE -> {
                out.writeByte(packet.payload.size)
                out.writeBytes(packet.payload)
            }
            PacketSize.VAR_SHORT -> {
                out.writeShort(packet.payload.size)
                out.writeBytes(packet.payload)
            }
        }
    }
}
```

## Reconnection

On connection drop, client attempts reconnect up to 5 times with exponential backoff:

```kotlin
fun attemptReconnect() {
    var attempt = 0
    val backoffMs = listOf(1000L, 2000L, 4000L, 8000L, 16000L)

    fun tryConnect() {
        if (attempt >= 5) { showLoginScreen("Connection lost."); return }
        connectToServer(currentWorld) { success ->
            if (success) sendReconnectLoginPacket()
            else {
                Thread.sleep(backoffMs[attempt++])
                tryConnect()
            }
        }
    }
    tryConnect()
}
```

## World Hop

```kotlin
fun hopWorld(worldId: Int) {
    val world = WorldList.get(worldId) ?: return
    disconnect()
    // Small delay to ensure clean disconnection
    connectToServer(world.host, world.port) {
        sendLoginPacket(lastUsername, lastPasswordHash)
    }
}
```
