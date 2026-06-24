package world.door

import world.Coordinate
import world.CollisionMap

/**
 * Abstraction for toggling dynamic door/gate obstacles in the collision map.
 *
 * Real door wiring (object IDs, open/closed id swap, orientation from ObjectDefinition,
 * 3-tick auto-close timer) is DEFERRED — needs ObjectDefinitions from cache index 2
 * which don't exist yet.
 */
interface DoorHandler {
    fun open(door: DoorState)
    fun close(door: DoorState)
}

/**
 * Minimal clip-flag-based door toggler.
 * Removes/restores the door's wall clip mask from [clip] when opened/closed.
 */
class ClipDoorHandler(private val clip: CollisionMap) : DoorHandler {
    override fun open(door: DoorState) {
        clip.removeFlag(door.tile, door.wallMask)
        door.open = true
    }

    override fun close(door: DoorState) {
        clip.addFlag(door.tile, door.wallMask)
        door.open = false
    }
}

/**
 * Mutable state for a single door/gate.
 *
 * @param tile      World tile the door occupies.
 * @param wallMask  [world.ClipFlag] bitmask representing this door's wall direction(s).
 * @param open      Whether the door is currently open (clip flags removed).
 */
data class DoorState(
    val tile: Coordinate,
    val wallMask: Int,
    var open: Boolean = false,
)
