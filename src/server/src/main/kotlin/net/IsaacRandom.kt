package net

/**
 * Matched encode/decode ISAAC cipher pair for one player session.
 *
 * The OSRS login protocol establishes session keys on login. These keys are used
 * to create two independent ISAAC ciphers:
 * - decode: initialized with raw session keys (client→server opcode decryption)
 * - encode: initialized with session keys + 50 (server→client opcode encryption)
 *
 * Offsetting the encode keys by +50 ensures the two ciphers diverge immediately,
 * so eavesdroppers cannot trivially reverse-engineer one from the other.
 *
 * This pair persists for the entire player session; the cipher state is never reset.
 * Each packet opcode is XORed with the next value from the appropriate cipher.
 *
 * Source: OSRS login protocol, https://github.com/RuneStar/cs2
 */
class IsaacRandom(sessionKeys: IntArray) {
    init { require(sessionKeys.size == 4) { "Session keys must be 4 ints" } }

    val encode = IsaacCipher(IntArray(4) { i -> sessionKeys[i] + 50 })
    val decode = IsaacCipher(sessionKeys)
}
