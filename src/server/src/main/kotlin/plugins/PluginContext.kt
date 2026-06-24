package plugins

import entity.Player

/**
 * Registration surface provided to each plugin during server startup.
 * Satisfied by the rsmod plugin system (wired in /implement-tick-engine-core).
 */
interface PluginContext {
    /**
     * Binds an object-interaction handler to one or more object IDs.
     * [handler] receives the interacting player and the object ID.
     */
    fun onObjectInteract(option: String, objectIds: IntArray, handler: (Player, Int) -> Unit)
}
