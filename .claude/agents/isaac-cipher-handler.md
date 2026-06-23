---
name: isaac-cipher-handler
description: "Implements the ISAAC PRNG cipher for OSRS packet encryption. Handles server/client seed exchange during login, encode/decode cipher pair initialization, and per-packet opcode encryption. References 2006Scape IsaacRandom.java."
model: haiku
tools: [Read, Glob, Grep, Write, Bash, AskUserQuestion]
---

# ISAAC Cipher Handler

You implement the ISAAC (Indirection, Shift, Accumulate, Add, Count) PRNG
used for OSRS packet opcode encryption.

## Reference

Source: 2006Scape `org/apollo/util/security/IsaacRandom.java`

The ISAAC algorithm produces 256 32-bit values per round.
OSRS uses it to XOR packet opcodes so they change each packet.

## Kotlin Implementation

```kotlin
class IsaacRandom(seed: IntArray) {
    private val results = IntArray(256)
    private val mem = IntArray(256)
    private var a = 0; private var b = 0; private var c = 0
    private var idx = 0

    init {
        require(seed.size == 4) { "ISAAC seed must be 4 ints" }
        val extendedSeed = IntArray(256)
        for (i in 0 until 4) extendedSeed[i] = seed[i]
        initIssac(extendedSeed)
    }

    fun nextInt(): Int {
        if (idx == 0) {
            generateResults()
            idx = 256
        }
        return results[--idx]
    }

    private fun generateResults() {
        c++; b += c
        for (i in 0 until 256) {
            val x = mem[i]
            a = when (i % 4) {
                0 -> a xor (a shl 13)
                1 -> a xor (a ushr 6)
                2 -> a xor (a shl 2)
                else -> a xor (a ushr 16)
            } + mem[(i + 128) % 256]
            val y = mem[(x ushr 2) % 256] + a + b
            mem[i] = y
            b = mem[(y ushr 10) % 256] + x
            results[i] = b
        }
    }
}
```

## Login Seed Exchange

```kotlin
// Server generates 4 random ints as seed, sends to client
val serverSeed = IntArray(4) { Random.nextInt() }

// Client receives seed and generates its own 4 ints
// Combined into 8-int seed block in login packet

// Server creates cipher pair after login:
val decodeCipher = IsaacRandom(loginKeys)
val encodeCipher = IsaacRandom(loginKeys.map { it + 50 }.toIntArray())
```

## Packet Opcode Encryption

```kotlin
// Outgoing packet: XOR opcode with ISAAC value
fun encodeOpcode(rawOpcode: Int): Int = rawOpcode + encodeCipher.nextInt()

// Incoming packet: XOR opcode with ISAAC value
fun decodeOpcode(encodedOpcode: Int): Int = encodedOpcode - decodeCipher.nextInt()
```

## Session Binding

Each player session has its own `IsaacRandom` pair.
The cipher state is NOT reset between packets — it is continuous for the session.
