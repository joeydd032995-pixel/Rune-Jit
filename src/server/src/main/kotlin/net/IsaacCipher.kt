package net

/**
 * ISAAC (Indirection, Shift, Accumulate, Add, Count) cipher.
 * Cryptographically secure PRNG used by OSRS for packet opcode encryption.
 *
 * Each session receives a 4-int seed from the login block; this seed is extended
 * to 256 ints and used to initialize the cipher state. The cipher produces 256
 * 32-bit values per generation cycle, and those values are consumed one at a time
 * via nextInt(). When exhausted, nextInt() triggers a new generation cycle.
 *
 * Source: https://burtleburtle.net/bob/rand/isaacafa.html
 * Reference Implementation: Bob Jenkins's C source (isaacafa.c)
 * OSRS Protocol: https://github.com/RuneStar/cs2
 */
class IsaacCipher(seed: IntArray) {
    init { require(seed.size == 4) { "ISAAC seed must be 4 ints" } }

    private val results = IntArray(256)
    private val mem = IntArray(256)
    private var count = 0  // Next index to read from results[]
    private var a = 0      // Accumulator
    private var b = 0      // Secondary state variable
    private var c = 0      // Counter

    init {
        initState(seed)
    }

    /**
     * Consumes the next 32-bit random value from the cipher.
     * If the results buffer is exhausted, triggers a new generation cycle.
     */
    fun nextInt(): Int {
        if (count == 0) {
            generate()
            count = 256
        }
        return results[--count]
    }

    /**
     * Initialize the ISAAC cipher state from a 4-int seed using the Bob Jenkins
     * two-pass initialization (randinit with flag=1).
     *
     * The seed (4 ints, zero-padded to 256) is placed in a key array k[]. Eight
     * working variables (aa..hh) are initialized to the golden ratio and scrambled
     * 4 times. Then two passes fold k[] and mem[] into mem[] using the same 8-var
     * mix. This ensures even small seed differences (e.g. +50 per key for
     * encode vs decode) produce completely uncorrelated cipher streams.
     *
     * Source: https://burtleburtle.net/bob/rand/isaacafa.html § Initialization (randinit, flag=1)
     * OSRS reference: https://github.com/RuneStar/cs2 (IsaacRandom.java)
     */
    private fun initState(seed: IntArray) {
        // k[] = seed zero-padded to 256 ints (key material for Jenkins randinit)
        val k = IntArray(256)
        for (i in seed.indices) k[i] = seed[i]

        // Eight working variables initialized to the golden ratio constant
        var aa = 0x9e3779b9.toInt()
        var bb = aa; var cc = aa; var dd = aa
        var ee = aa; var ff = aa; var gg = aa; var hh = aa

        // Scramble the golden ratio 4 times with the 8-variable mix
        repeat(4) {
            aa = aa xor (bb shl 11); dd += aa; bb += cc
            bb = bb xor (cc ushr  2); ee += bb; cc += dd
            cc = cc xor (dd shl  8); ff += cc; dd += ee
            dd = dd xor (ee ushr 16); gg += dd; ee += ff
            ee = ee xor (ff shl 10); hh += ee; ff += gg
            ff = ff xor (gg ushr  4); aa += ff; gg += hh
            gg = gg xor (hh shl  8); bb += gg; hh += aa
            hh = hh xor (aa ushr  9); cc += hh; aa += bb
        }

        // First pass: fold k[] (seed) into mem[]
        var i = 0
        while (i < 256) {
            aa += k[i  ]; bb += k[i+1]; cc += k[i+2]; dd += k[i+3]
            ee += k[i+4]; ff += k[i+5]; gg += k[i+6]; hh += k[i+7]
            aa = aa xor (bb shl 11); dd += aa; bb += cc
            bb = bb xor (cc ushr  2); ee += bb; cc += dd
            cc = cc xor (dd shl  8); ff += cc; dd += ee
            dd = dd xor (ee ushr 16); gg += dd; ee += ff
            ee = ee xor (ff shl 10); hh += ee; ff += gg
            ff = ff xor (gg ushr  4); aa += ff; gg += hh
            gg = gg xor (hh shl  8); bb += gg; hh += aa
            hh = hh xor (aa ushr  9); cc += hh; aa += bb
            mem[i  ] = aa; mem[i+1] = bb; mem[i+2] = cc; mem[i+3] = dd
            mem[i+4] = ee; mem[i+5] = ff; mem[i+6] = gg; mem[i+7] = hh
            i += 8
        }

        // Second pass: fold mem[] back into itself for full avalanche
        i = 0
        while (i < 256) {
            aa += mem[i  ]; bb += mem[i+1]; cc += mem[i+2]; dd += mem[i+3]
            ee += mem[i+4]; ff += mem[i+5]; gg += mem[i+6]; hh += mem[i+7]
            aa = aa xor (bb shl 11); dd += aa; bb += cc
            bb = bb xor (cc ushr  2); ee += bb; cc += dd
            cc = cc xor (dd shl  8); ff += cc; dd += ee
            dd = dd xor (ee ushr 16); gg += dd; ee += ff
            ee = ee xor (ff shl 10); hh += ee; ff += gg
            ff = ff xor (gg ushr  4); aa += ff; gg += hh
            gg = gg xor (hh shl  8); bb += gg; hh += aa
            hh = hh xor (aa ushr  9); cc += hh; aa += bb
            mem[i  ] = aa; mem[i+1] = bb; mem[i+2] = cc; mem[i+3] = dd
            mem[i+4] = ee; mem[i+5] = ff; mem[i+6] = gg; mem[i+7] = hh
            i += 8
        }

        generate()
        count = 256
    }

    /**
     * Generate 256 new random values into the results array.
     *
     * This is the core ISAAC algorithm: each iteration produces one result and
     * updates the internal state (a, b, mem[]). The result at index i depends on
     * the indirection mem[x >> 2], making the output unpredictable without full
     * state knowledge.
     *
     * The shift amounts for 'a' are determined by i % 4:
     * - i≡0 (mod 4): a ^= (a << 13)
     * - i≡1 (mod 4): a ^= (a >> 6)
     * - i≡2 (mod 4): a ^= (a << 2)
     * - i≡3 (mod 4): a ^= (a >> 16)
     *
     * This creates 4-way data dependency: results[i] depends on a, b, mem[i],
     * mem[(x>>2)%256], mem[(y>>10)%256], and the indirection addresses change
     * with every output.
     *
     * Source: https://burtleburtle.net/bob/rand/isaacafa.html § Generation
     */
    private fun generate() {
        c++
        b += c

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
