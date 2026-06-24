package entity

/** 28-slot player inventory. */
class Inventory {
    data class Slot(val itemId: Int, var quantity: Int)

    private val slots = arrayOfNulls<Slot>(28)

    fun contains(itemId: Int): Boolean = slots.any { it?.itemId == itemId }

    fun addItem(itemId: Int, quantity: Int = 1): Boolean {
        val existing = slots.firstOrNull { it?.itemId == itemId }
        if (existing != null) {
            existing.quantity += quantity
            return true
        }
        val emptyIdx = slots.indexOfFirst { it == null }
        if (emptyIdx == -1) return false
        slots[emptyIdx] = Slot(itemId, quantity)
        return true
    }

    fun remove(itemId: Int, quantity: Int = 1): Boolean {
        val slot = slots.firstOrNull { it?.itemId == itemId } ?: return false
        if (slot.quantity < quantity) return false
        slot.quantity -= quantity
        if (slot.quantity == 0) slots[slots.indexOf(slot)] = null
        return true
    }

    fun isFull(): Boolean = slots.none { it == null }
}
