package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.Utils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

/**
 * 26.2 port - see SiphonEnchantment.kt's class doc for why this is a plain object now.
 *
 * TODO(26.2): config/ isn't ported yet, so `creativeRefill`/`refillNonStackables`/`refillOffhand`
 * WorldConfig toggles are dropped for now (creative players always allowed to refill; non-
 * stackable refilling off; offhand refilling on, matching the original's defaults).
 */
@Suppress("NAME_SHADOWING")
object RefillEnchantment {
    @JvmStatic
    fun onPlayerTick(
        player: ServerPlayer,
        inventoryOpen: Boolean,
        currentSlot: Int,
        currentMainStack: ItemStack,
        currentOffStack: ItemStack,
        previousSlot: Int,
        previousMainStack: ItemStack,
        previousOffStack: ItemStack,
    ) {
        val allowsRefill = !inventoryOpen
        val swappedHands = ItemStack.isSameItemSameComponents(previousMainStack, currentOffStack) &&
            ItemStack.isSameItemSameComponents(currentMainStack, previousOffStack)
        val wasMainEmptied = previousMainStack.count > 0 && currentMainStack.isEmpty && !swappedHands
        val wasOffEmptied = previousOffStack.count > 0 && currentOffStack.isEmpty && !swappedHands
        val shouldRefillMain = (wasMainEmptied || ItemStack.isSameItemSameComponents(currentMainStack, previousMainStack)) &&
            currentMainStack.count < previousMainStack.count && previousMainStack.isStackable
        val shouldRefillOff = (wasOffEmptied || ItemStack.isSameItemSameComponents(currentOffStack, previousOffStack)) &&
            currentOffStack.count < previousOffStack.count && previousOffStack.isStackable
        val doRefill = allowsRefill && currentSlot == previousSlot && !swappedHands && (shouldRefillMain || shouldRefillOff)

        if (!doRefill) return
        if (shouldRefillMain) {
            refill(player, currentSlot, previousMainStack, previousMainStack.count - currentMainStack.count)
        } else {
            refill(player, Inventory.SLOT_OFFHAND, previousOffStack, previousOffStack.count - currentOffStack.count)
        }
    }

    @JvmStatic
    fun refill(player: ServerPlayer, slot: Int, itemType: ItemStack, amount: Int): Boolean {
        if (amount <= 0) return false
        var didRefill = false
        var remaining = amount
        val refillHolder = Utils.enchantmentHolder(player.registryAccess(), Mod.REFILL_KEY)
        val augmentHolder = Utils.enchantmentHolder(player.registryAccess(), Mod.AUGMENT_KEY)
        val containerSlots = Utils.getContainers(player, refillHolder)
        for (container in containerSlots) {
            val containerInventory = Utils.getContainerInventory(container, augmentHolder)
            var updateContainer = false
            for (innerStack in containerInventory) {
                if (innerStack.isEmpty) continue
                val refilled = tryRefillSlot(player.inventory, innerStack, slot, itemType, remaining)
                if (refilled > 0) {
                    remaining -= refilled
                    updateContainer = true
                    if (remaining <= 0) break
                }
            }
            if (updateContainer) {
                didRefill = true
                Utils.setContainerInventory(container, containerInventory)
            }
        }
        return didRefill
    }

    private fun tryRefillSlot(inventory: Inventory, from: ItemStack, slot: Int, itemType: ItemStack, amount: Int): Int {
        val to = inventory.getItem(slot)

        if (!ItemStack.isSameItemSameComponents(itemType, from)) return 0
        if (!to.isEmpty && !ItemStack.isSameItemSameComponents(to, from)) return 0

        val transferCount = minOf(amount, itemType.maxStackSize - to.count, from.count)
        if (transferCount <= 0) return 0

        if (to.isEmpty) {
            val stack = from.copy()
            stack.count = transferCount
            inventory.setItem(slot, stack)
        } else {
            to.grow(transferCount)
        }
        from.shrink(transferCount)
        return transferCount
    }
}
