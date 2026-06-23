# Parity Tests

Parity tests verify that the emulator's mechanics exactly match the live OSRS
game as documented on the OSRS wiki.

## Philosophy

Every assertion must have a source. No test may claim an expected value without
citing a wiki URL or another authoritative reference. This ensures parity tests
remain trustworthy and maintainable across revisions.

```kotlin
// CORRECT — source cited
@Test
fun `woodcutting yew log xp`() {
    // Source: https://oldschool.runescape.wiki/w/Woodcutting#Experience
    assertEquals(175.0, WoodcuttingData.YEW.xp)
}

// WRONG — no source
@Test
fun `woodcutting yew log xp`() {
    assertEquals(175.0, WoodcuttingData.YEW.xp) // where does 175 come from?
}
```

## Running Tests

```bash
# Run all parity tests
./gradlew test --tests "*.parity.*"

# Run a specific skill
./gradlew test --tests "*.parity.WoodcuttingParityTest"

# Generate parity score report
/automated-parity-testing-suite
```

## Parity Score

Each test suite reports a **parity score** — the percentage of mechanics that
match the wiki source of truth. Target: 95%+ before Phase 7 deployment.

See `.claude/docs/templates/parity-test-report.md` for the report format.

## Skills That Generate Tests

- `/verify-mechanic-parity-1to1 [system]` — generates parity tests for a system
- `/automated-parity-testing-suite` — runs all tests and generates report
- `/combat-engine-full` — includes combat parity test generation
