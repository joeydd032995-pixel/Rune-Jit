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

    /**
     * Binds an NPC-interaction handler to one or more NPC IDs.
     * An empty [npcIds] array registers a wildcard handler for all NPC IDs.
     * [handler] receives the interacting player and the NPC slot index.
     */
    fun onNpcInteract(option: String, npcIds: IntArray, handler: (Player, Int) -> Unit)

    /**
     * Binds an item-interaction handler to one or more item IDs.
     * [handler] receives the interacting player and the item ID.
     */
    fun onItemInteract(option: String, itemIds: IntArray, handler: (Player, Int) -> Unit)

    /**
     * Binds an item-use-on-item handler. Fires when the player uses any item in
     * [primaryItemIds] on any item in [targetItemIds] (or vice versa — order is
     * normalised by the dispatcher). [handler] receives the player, the matched
     * primary item ID, and the matched target item ID.
     */
    fun onItemUseOnItem(
        primaryItemIds: IntArray,
        targetItemIds: IntArray,
        handler: (Player, Int, Int) -> Unit,
    )

    /**
     * Binds a widget-action handler to a specific interface/component pair.
     * [handler] receives the interacting player.
     */
    fun onWidgetAction(interfaceId: Int, componentId: Int, option: String, handler: (Player) -> Unit)
}
