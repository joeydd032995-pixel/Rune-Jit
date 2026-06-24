package net.packets

import entity.Player
import net.GamePacket
import org.slf4j.LoggerFactory

/**
 * Handles incoming walk/run destination packets from the client.
 *
 * Packet layout (per protocol-defs.yaml WALK_TO):
 *   destX  — Short (2 bytes) — destination tile X
 *   destY  — Short (2 bytes) — destination tile Y
 *   ctrlKey — Byte (1 byte)  — 1 = run requested (run deferred pending energy/Agility)
 *
 * On receipt, runs a BFS path query from the player's current tile to the destination
 * and queues the resulting steps on the player's MovementQueue (1 tile/tick walking).
 *
 * Server-authoritative: the client's destination is trusted for coordinate only; the
 * server rejects moves through walls/objects via CollisionMap clip-flag checking.
 * Source: https://oldschool.runescape.wiki/w/Walking
 */
object WalkHandler : (Player, GamePacket) -> Unit {
    private val log = LoggerFactory.getLogger(WalkHandler::class.java)

    override fun invoke(player: Player, pkt: GamePacket) {
        // Defensive guard: World.init runs at server start; skip in unit-test contexts
        if (!world.World.isInitialized) return

        val destX = pkt.payload.readShort().toInt()
        val destY = pkt.payload.readShort().toInt()
        pkt.payload.readByte() // ctrlKey — run toggle deferred (run energy/Agility not yet implemented)

        val dest = world.Coordinate(destX, destY, player.plane)

        // Ensure both the player's region and the destination region are loaded
        world.World.regionStore.ensureRegion(player.coordinate.regionId)
        world.World.regionStore.ensureRegion(dest.regionId)

        val path = world.World.pathFinder.findPath(player.coordinate, dest)
        player.movement.walkTo(path)

        log.debug("${player.username} walk → ($destX, $destY) steps=${path.tiles.size} reached=${path.reachedTarget}")
    }
}
