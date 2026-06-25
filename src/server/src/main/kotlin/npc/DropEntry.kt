package npc

/**
 * One entry in an NPC's drop table, parsed from osrsbox-db.
 * Source: https://oldschool.runescape.wiki/w/Drop_rate
 */
data class DropEntry(
    val itemId: Int,
    val minQty: Int,
    val maxQty: Int,
    val rarity: Double,
    val noted: Boolean,
    val rolls: Int,
)
