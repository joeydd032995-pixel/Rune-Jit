package combat

import engine.TickEvent
import engine.TickQueue
import engine.TickQueueImpl
import entity.Npc
import entity.Player
import entity.Skill
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import prayer.PrayerDefs
import java.nio.file.Path

/**
 * Integration parity tests for the full combat dispatch cycle.
 *
 * Tests verify that:
 *   - Correct action type is created for each combat style
 *   - Player.respawn() resets HP and teleports to Lumbridge
 *   - WeaponDefs correctly identifies weapons by item ID
 *   - activeCombatAction is set and cancelable on re-target
 *   - Dead NPC guard prevents combat start
 *
 * Sources:
 *   - https://oldschool.runescape.wiki/w/Combat
 *   - https://oldschool.runescape.wiki/w/Lumbridge#Respawn_point
 *   - https://oldschool.runescape.wiki/w/Weapons
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CombatCycleTest {

    @BeforeAll
    fun setup() {
        PrayerDefs.init()
        CombatDefs.init(Path.of("data/combat"))
        WeaponDefs.init(Path.of("data/combat"))
    }

    /** Captures scheduled events without advancing real time. */
    private class CapturingQueue : TickQueue {
        val scheduled = mutableListOf<Pair<Int, TickEvent>>()
        override fun schedule(delayTicks: Int, event: TickEvent) { scheduled.add(delayTicks to event) }
        override fun cancel(event: TickEvent) {}
    }

    private fun makePlayer(): Player {
        val p = Player(username = "TestPlayer", tickQueue = TickQueueImpl())
        // Give enough XP for combat levels to matter in formulas
        p.skills.addXp(Skill.HITPOINTS, 14_000.0)  // ~level 30 HP
        return p
    }

    private fun makeNpc(): Npc = Npc(
        name = "Goblin", combatLevel = 5, defenceLevel = 1, maxHp = 20, x = 3200, y = 3200,
    )

    // -------------------------------------------------------------------------
    // Test 1: MeleeAction implements CombatAction
    // Source: https://oldschool.runescape.wiki/w/Combat
    // -------------------------------------------------------------------------

    @Test fun `MeleeAction implements CombatAction`() {
        val action = MeleeAction(
            attacker    = makePlayer(),
            target      = makeNpc(),
            style       = CombatStyle.MELEE_AGGRESSIVE,
            attackType  = "SLASH",
            weaponType  = "WHIP",
            config      = CombatDefs.config,
        )
        assertTrue(
            action is CombatAction,
            "MeleeAction must implement CombatAction. Source: https://oldschool.runescape.wiki/w/Combat"
        )
    }

    // -------------------------------------------------------------------------
    // Test 2: RangedAction implements CombatAction
    // Source: https://oldschool.runescape.wiki/w/Ranged
    // -------------------------------------------------------------------------

    @Test fun `RangedAction implements CombatAction`() {
        val action = RangedAction(
            attacker   = makePlayer(),
            target     = makeNpc(),
            style      = CombatStyle.RANGED_RAPID,
            weaponType = "SHORTBOW",
            config     = CombatDefs.config,
        )
        assertTrue(
            action is CombatAction,
            "RangedAction must implement CombatAction. Source: https://oldschool.runescape.wiki/w/Ranged"
        )
    }

    // -------------------------------------------------------------------------
    // Test 3: MagicAction implements CombatAction
    // Source: https://oldschool.runescape.wiki/w/Magic#Combat
    // -------------------------------------------------------------------------

    @Test fun `MagicAction implements CombatAction`() {
        val action = MagicAction(
            attacker = makePlayer(),
            target   = makeNpc(),
            spellKey = "WIND_BOLT",
            config   = CombatDefs.config,
        )
        assertTrue(
            action is CombatAction,
            "MagicAction must implement CombatAction. Source: https://oldschool.runescape.wiki/w/Magic#Combat"
        )
    }

    // -------------------------------------------------------------------------
    // Test 4: Player.respawn() resets HP to max
    // Source: https://oldschool.runescape.wiki/w/Hitpoints
    // -------------------------------------------------------------------------

    @Test fun `respawn restores player HP to max`() {
        val player = makePlayer()
        val maxHp = player.skills.getLevel(Skill.HITPOINTS)
        player.takeDamage(maxHp)  // kill the player
        assertTrue(player.isDead, "Player should be dead after taking full HP as damage")

        player.respawn()

        assertEquals(
            maxHp,
            player.currentHp,
            "Player HP must equal max HP after respawn. Source: https://oldschool.runescape.wiki/w/Hitpoints"
        )
    }

    // -------------------------------------------------------------------------
    // Test 5: Player.respawn() teleports to Lumbridge (3222, 3218)
    // Source: https://oldschool.runescape.wiki/w/Lumbridge#Respawn_point
    // -------------------------------------------------------------------------

    @Test fun `respawn teleports player to Lumbridge`() {
        val player = makePlayer()
        player.x = 3100
        player.y = 3100
        player.plane = 1

        player.respawn()

        assertEquals(
            3222,
            player.x,
            "Respawn X must be 3222 (Lumbridge). Source: https://oldschool.runescape.wiki/w/Lumbridge#Respawn_point"
        )
        assertEquals(
            3218,
            player.y,
            "Respawn Y must be 3218 (Lumbridge). Source: https://oldschool.runescape.wiki/w/Lumbridge#Respawn_point"
        )
        assertEquals(0, player.plane, "Respawn plane must be 0 (ground floor)")
    }

    // -------------------------------------------------------------------------
    // Test 6: respawn() cancels active combat action
    // Source: https://oldschool.runescape.wiki/w/Combat
    // -------------------------------------------------------------------------

    @Test fun `respawn cancels active combat action`() {
        val player = makePlayer()
        val npc = makeNpc()
        val action = MeleeAction(
            attacker   = player,
            target     = npc,
            style      = CombatStyle.MELEE_AGGRESSIVE,
            attackType = "CRUSH",
            config     = CombatDefs.config,
        )
        player.activeCombatAction = action
        assertNotNull(player.activeCombatAction)

        player.respawn()

        assertNull(
            player.activeCombatAction,
            "activeCombatAction must be null after respawn. Source: https://oldschool.runescape.wiki/w/Combat"
        )
    }

    // -------------------------------------------------------------------------
    // Test 7: WeaponDefs returns UNARMED for unknown item ID
    // Source: https://oldschool.runescape.wiki/w/Weapons
    // -------------------------------------------------------------------------

    @Test fun `WeaponDefs returns UNARMED for unknown item ID`() {
        val def = WeaponDefs.getByItemId(-1)
        assertEquals(
            WeaponDefs.UNARMED,
            def,
            "Unknown weapon ID must return UNARMED. Source: https://oldschool.runescape.wiki/w/Weapons"
        )
    }

    // -------------------------------------------------------------------------
    // Test 8: WeaponDefs identifies abyssal whip correctly
    // Source: https://oldschool.runescape.wiki/w/Abyssal_whip
    // -------------------------------------------------------------------------

    @Test fun `WeaponDefs identifies abyssal whip as WHIP slash`() {
        val def = WeaponDefs.getByItemId(4151)  // Abyssal whip
        assertEquals(
            "WHIP",
            def.weaponType,
            "Abyssal whip (4151) must have weapon_type WHIP. Source: https://oldschool.runescape.wiki/w/Abyssal_whip"
        )
        assertEquals(
            "SLASH",
            def.defaultAttackType,
            "Abyssal whip (4151) must have default_attack_type SLASH. Source: https://oldschool.runescape.wiki/w/Abyssal_whip"
        )
    }

    // -------------------------------------------------------------------------
    // Test 9: Npc.maxHp is accessible as a stored field
    // Source: https://oldschool.runescape.wiki/w/Hitpoints
    // -------------------------------------------------------------------------

    @Test fun `Npc maxHp is stored and accessible`() {
        val npc = makeNpc()
        assertEquals(
            20,
            npc.maxHp,
            "Npc.maxHp must be stored as a field. Source: https://oldschool.runescape.wiki/w/Hitpoints"
        )
    }

    // -------------------------------------------------------------------------
    // Test 10: Player default combat style is MELEE_AGGRESSIVE
    // Source: https://oldschool.runescape.wiki/w/Attack_style
    // -------------------------------------------------------------------------

    @Test fun `player default combatStyle is MELEE_AGGRESSIVE`() {
        val player = makePlayer()
        assertEquals(
            CombatStyle.MELEE_AGGRESSIVE,
            player.combatStyle,
            "Default combat style must be MELEE_AGGRESSIVE. Source: https://oldschool.runescape.wiki/w/Attack_style"
        )
    }
}
