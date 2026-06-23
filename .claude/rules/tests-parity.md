---
path: tests/parity/**
---

# Parity Test Rules

## Wiki Citation Required

Every parity test assertion must include a wiki URL as the source of truth:

```kotlin
// ❌ WRONG: No citation
@Test fun testWoodcuttingXp() {
    assert(WoodcuttingData.trees[TreeType.YEW]!!.xp == 175.0)
}

// ✅ CORRECT: Citation in test doc
/**
 * Yew tree XP: 175.0 per log
 * Source: https://oldschool.runescape.wiki/w/Woodcutting#Experience
 */
@Test fun testYewLogXp() {
    assert(WoodcuttingData.trees[TreeType.YEW]!!.xp == 175.0) {
        "Yew XP should be 175.0. See: https://oldschool.runescape.wiki/w/Woodcutting#Experience"
    }
}
```

## No Assertions Without Source

If the expected value cannot be cited from the OSRS wiki, it is not a parity test — it's a unit test. Move it to `tests/unit/` instead.

## Tolerance Policy

- **Formula results**: zero tolerance (exact integer match)
- **Rate/probability**: ≤ 0.001 tolerance (floating point)
- **Drop rates**: ≤ 1/10000 tolerance (RNG simulation requires many samples)

## Test Naming Convention

```
${system}/${mechanic}.test.kt
```
Examples:
- `combat/max-hit.test.kt`
- `skilling/woodcutting-xp.test.kt`
- `economy/shop-prices.test.kt`

## Test Isolation

Parity tests must be stateless — they create their own test server instance and do not depend on external state:

```kotlin
class WoodcuttingXpTest {
    private val server = TestServer.create()  // fresh server per test class

    @AfterAll fun teardown() { server.shutdown() }
}
```

## Prohibited

- No assertions with "TODO: find wiki source"
- No `assertTrue(result > 0)` style tests (must compare exact expected value)
- No integration tests in `tests/parity/` (use `tests/integration/` for those)
