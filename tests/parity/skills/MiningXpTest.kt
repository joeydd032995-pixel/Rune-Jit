package parity.skills

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import skills.mining.MiningConfig
import skills.mining.MiningLoader
import java.nio.file.Path

/**
 * Mining XP values and mechanic parity tests.
 * All expected values sourced from the OSRS wiki per tests-parity.md rules.
 * Primary source: https://oldschool.runescape.wiki/w/Mining
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MiningXpTest {

    private lateinit var config: MiningConfig

    @BeforeAll
    fun loadData() {
        config = MiningLoader.load(Path.of("data/skills/mining.yaml"))
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * 3 ticks per mining attempt (1.8 seconds).
     * Source: https://oldschool.runescape.wiki/w/Mining#Mechanics
     */
    @Test
    fun `ticks per attempt is 3`() {
        assertEquals(3, config.meta.ticksPerAttempt,
            "Mining ticks_per_attempt must be 3. See: https://oldschool.runescape.wiki/w/Mining#Mechanics")
    }

    // -------------------------------------------------------------------------
    // Rock XP values — Source: https://oldschool.runescape.wiki/w/Mining#Experience
    // -------------------------------------------------------------------------

    /**
     * Clay XP is 5.0.
     * Source: https://oldschool.runescape.wiki/w/Clay
     */
    @Test
    fun `clay xp is 5_0`() {
        assertEquals(5.0, config.rocks["CLAY"]!!.xp,
            "Clay XP should be 5.0. See: https://oldschool.runescape.wiki/w/Clay")
    }

    /**
     * Copper XP is 17.5.
     * Source: https://oldschool.runescape.wiki/w/Copper_ore
     */
    @Test
    fun `copper xp is 17_5`() {
        assertEquals(17.5, config.rocks["COPPER"]!!.xp,
            "Copper XP should be 17.5. See: https://oldschool.runescape.wiki/w/Copper_ore")
    }

    /**
     * Tin XP is 17.5.
     * Source: https://oldschool.runescape.wiki/w/Tin_ore
     */
    @Test
    fun `tin xp is 17_5`() {
        assertEquals(17.5, config.rocks["TIN"]!!.xp,
            "Tin XP should be 17.5. See: https://oldschool.runescape.wiki/w/Tin_ore")
    }

    /**
     * Iron XP is 35.0.
     * Source: https://oldschool.runescape.wiki/w/Iron_ore
     */
    @Test
    fun `iron xp is 35_0`() {
        assertEquals(35.0, config.rocks["IRON"]!!.xp,
            "Iron XP should be 35.0. See: https://oldschool.runescape.wiki/w/Iron_ore")
    }

    /**
     * Silver XP is 40.0.
     * Source: https://oldschool.runescape.wiki/w/Silver_ore
     */
    @Test
    fun `silver xp is 40_0`() {
        assertEquals(40.0, config.rocks["SILVER"]!!.xp,
            "Silver XP should be 40.0. See: https://oldschool.runescape.wiki/w/Silver_ore")
    }

    /**
     * Coal XP is 50.0.
     * Source: https://oldschool.runescape.wiki/w/Coal
     */
    @Test
    fun `coal xp is 50_0`() {
        assertEquals(50.0, config.rocks["COAL"]!!.xp,
            "Coal XP should be 50.0. See: https://oldschool.runescape.wiki/w/Coal")
    }

    /**
     * Gold XP is 65.0.
     * Source: https://oldschool.runescape.wiki/w/Gold_ore
     */
    @Test
    fun `gold xp is 65_0`() {
        assertEquals(65.0, config.rocks["GOLD"]!!.xp,
            "Gold XP should be 65.0. See: https://oldschool.runescape.wiki/w/Gold_ore")
    }

    /**
     * Mithril XP is 80.0.
     * Source: https://oldschool.runescape.wiki/w/Mithril_ore
     */
    @Test
    fun `mithril xp is 80_0`() {
        assertEquals(80.0, config.rocks["MITHRIL"]!!.xp,
            "Mithril XP should be 80.0. See: https://oldschool.runescape.wiki/w/Mithril_ore")
    }

    /**
     * Adamantite XP is 95.0.
     * Source: https://oldschool.runescape.wiki/w/Adamantite_ore
     */
    @Test
    fun `adamantite xp is 95_0`() {
        assertEquals(95.0, config.rocks["ADAMANTITE"]!!.xp,
            "Adamantite XP should be 95.0. See: https://oldschool.runescape.wiki/w/Adamantite_ore")
    }

    /**
     * Runite XP is 125.0.
     * Source: https://oldschool.runescape.wiki/w/Runite_ore
     */
    @Test
    fun `runite xp is 125_0`() {
        assertEquals(125.0, config.rocks["RUNITE"]!!.xp,
            "Runite XP should be 125.0. See: https://oldschool.runescape.wiki/w/Runite_ore")
    }

    /**
     * Amethyst XP is 240.0.
     * Source: https://oldschool.runescape.wiki/w/Amethyst
     */
    @Test
    fun `amethyst xp is 240_0`() {
        assertEquals(240.0, config.rocks["AMETHYST"]!!.xp,
            "Amethyst XP should be 240.0. See: https://oldschool.runescape.wiki/w/Amethyst")
    }

    // -------------------------------------------------------------------------
    // Rock level requirements — Source: https://oldschool.runescape.wiki/w/Mining#Ores
    // -------------------------------------------------------------------------

    @Test fun `clay requires level 1`() = assertRockLevel("CLAY", 1)
    @Test fun `copper requires level 1`() = assertRockLevel("COPPER", 1)
    @Test fun `tin requires level 1`() = assertRockLevel("TIN", 1)
    @Test fun `limestone requires level 10`() = assertRockLevel("LIMESTONE", 10)
    @Test fun `iron requires level 15`() = assertRockLevel("IRON", 15)
    @Test fun `silver requires level 20`() = assertRockLevel("SILVER", 20)
    @Test fun `coal requires level 30`() = assertRockLevel("COAL", 30)
    @Test fun `gold requires level 40`() = assertRockLevel("GOLD", 40)
    @Test fun `gem requires level 40`() = assertRockLevel("GEM", 40)
    @Test fun `mithril requires level 55`() = assertRockLevel("MITHRIL", 55)
    @Test fun `adamantite requires level 70`() = assertRockLevel("ADAMANTITE", 70)
    @Test fun `runite requires level 85`() = assertRockLevel("RUNITE", 85)
    @Test fun `amethyst requires level 92`() = assertRockLevel("AMETHYST", 92)

    // -------------------------------------------------------------------------
    // Rock respawn ticks — Source: https://oldschool.runescape.wiki/w/Mining
    // -------------------------------------------------------------------------

    @Test fun `clay respawn ticks is 3`() = assertRespawnTicks("CLAY", 3)
    @Test fun `iron respawn ticks is 5`() = assertRespawnTicks("IRON", 5)
    @Test fun `silver respawn ticks is 100`() = assertRespawnTicks("SILVER", 100)
    @Test fun `coal respawn ticks is 50`() = assertRespawnTicks("COAL", 50)
    @Test fun `gold respawn ticks is 100`() = assertRespawnTicks("GOLD", 100)
    @Test fun `mithril respawn ticks is 200`() = assertRespawnTicks("MITHRIL", 200)
    @Test fun `adamantite respawn ticks is 400`() = assertRespawnTicks("ADAMANTITE", 400)
    @Test fun `runite respawn ticks is 1200`() = assertRespawnTicks("RUNITE", 1200)

    // -------------------------------------------------------------------------
    // Gem drop config — Source: https://oldschool.runescape.wiki/w/Uncut_gem#Gem_rocks
    // -------------------------------------------------------------------------

    /**
     * 1/256 base chance per ore to roll gem drop table.
     * Source: https://oldschool.runescape.wiki/w/Uncut_gem#Gem_rocks
     */
    @Test
    fun `gem drop base chance is 256`() {
        assertEquals(256, config.gemDrops.baseDropChance,
            "Gem drop base_drop_chance must be 256 (1/256). See: https://oldschool.runescape.wiki/w/Uncut_gem#Gem_rocks")
    }

    /**
     * Gem drop table contains 4 gem types.
     * Source: https://oldschool.runescape.wiki/w/Uncut_gem#Gem_rocks
     */
    @Test
    fun `gem drop table has 4 entries`() {
        assertEquals(4, config.gemDrops.gems.size,
            "Gem drop table must have 4 entries. See: https://oldschool.runescape.wiki/w/Uncut_gem#Gem_rocks")
    }

    // -------------------------------------------------------------------------
    // Pickaxe mining bonuses — Source: https://oldschool.runescape.wiki/w/Pickaxe
    // -------------------------------------------------------------------------

    @Test fun `bronze pickaxe mining bonus is 1`() = assertPickaxeBonus("BRONZE_PICKAXE", 1)
    @Test fun `iron pickaxe mining bonus is 10`() = assertPickaxeBonus("IRON_PICKAXE", 10)
    @Test fun `steel pickaxe mining bonus is 20`() = assertPickaxeBonus("STEEL_PICKAXE", 20)
    @Test fun `black pickaxe mining bonus is 22`() = assertPickaxeBonus("BLACK_PICKAXE", 22)
    @Test fun `mithril pickaxe mining bonus is 30`() = assertPickaxeBonus("MITHRIL_PICKAXE", 30)
    @Test fun `adamant pickaxe mining bonus is 40`() = assertPickaxeBonus("ADAMANT_PICKAXE", 40)
    @Test fun `rune pickaxe mining bonus is 50`() = assertPickaxeBonus("RUNE_PICKAXE", 50)
    @Test fun `dragon pickaxe mining bonus is 60`() = assertPickaxeBonus("DRAGON_PICKAXE", 60)
    @Test fun `crystal pickaxe mining bonus is 80`() = assertPickaxeBonus("CRYSTAL_PICKAXE", 80)

    // -------------------------------------------------------------------------
    // Pickaxe wield levels — Source: https://oldschool.runescape.wiki/w/Pickaxe
    // -------------------------------------------------------------------------

    @Test fun `bronze pickaxe wield level is 1`() = assertPickaxeWieldLevel("BRONZE_PICKAXE", 1)
    @Test fun `steel pickaxe wield level is 6`() = assertPickaxeWieldLevel("STEEL_PICKAXE", 6)
    @Test fun `black pickaxe wield level is 11`() = assertPickaxeWieldLevel("BLACK_PICKAXE", 11)
    @Test fun `mithril pickaxe wield level is 21`() = assertPickaxeWieldLevel("MITHRIL_PICKAXE", 21)
    @Test fun `rune pickaxe wield level is 41`() = assertPickaxeWieldLevel("RUNE_PICKAXE", 41)
    @Test fun `dragon pickaxe wield level is 61`() = assertPickaxeWieldLevel("DRAGON_PICKAXE", 61)
    @Test fun `crystal pickaxe wield level is 71`() = assertPickaxeWieldLevel("CRYSTAL_PICKAXE", 71)

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun assertRockLevel(rockName: String, expectedLevel: Int) {
        val rock = config.rocks[rockName]
            ?: error("Rock '$rockName' not found in mining.yaml")
        assertEquals(expectedLevel, rock.levelRequired,
            "$rockName level_required should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Mining#Ores")
    }

    private fun assertRespawnTicks(rockName: String, expectedTicks: Int) {
        val rock = config.rocks[rockName]
            ?: error("Rock '$rockName' not found in mining.yaml")
        assertEquals(expectedTicks, rock.respawnTicks,
            "$rockName respawn_ticks should be $expectedTicks. See: https://oldschool.runescape.wiki/w/Mining")
    }

    private fun assertPickaxeBonus(pickaxeName: String, expectedBonus: Int) {
        val pick = config.pickaxes.firstOrNull { it.name == pickaxeName }
            ?: error("Pickaxe '$pickaxeName' not found in mining.yaml")
        assertEquals(expectedBonus, pick.miningBonus,
            "$pickaxeName mining_bonus should be $expectedBonus. See: https://oldschool.runescape.wiki/w/Pickaxe")
    }

    private fun assertPickaxeWieldLevel(pickaxeName: String, expectedLevel: Int) {
        val pick = config.pickaxes.firstOrNull { it.name == pickaxeName }
            ?: error("Pickaxe '$pickaxeName' not found in mining.yaml")
        assertEquals(expectedLevel, pick.wieldLevel,
            "$pickaxeName wield_level should be $expectedLevel. See: https://oldschool.runescape.wiki/w/Pickaxe")
    }
}
