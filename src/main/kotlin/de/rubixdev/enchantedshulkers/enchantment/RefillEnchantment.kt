package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.Utils
import de.rubixdev.enchantedshulkers.config.WorldConfig
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

@Suppress("NAME_SHADOWING")
class RefillEnchantment : ContainerEnchantment(Mod.PORTABLE_CONTAINER_TARGET) {
    override fun generate() = WorldConfig.generateRefill

    companion object {
        @JvmStatic
        fun onPlayerTick(
            player: ServerPlayerEntity,
            inventoryOpen: Boolean,
            currentSlot: Int,
            currentMainStack: ItemStack,
            currentOffStack: ItemStack,
            previousSlot: Int,
            previousMainStack: ItemStack,
            previousOffStack: ItemStack,
        ) {
            val allowsRefill = (!player.isCreative || WorldConfig.creativeRefill) && !inventoryOpen
            val swappedHands = ItemStack.areEqual(previousMainStack, currentOffStack) &&
                ItemStack.areEqual(currentMainStack, previousOffStack)
            val wasMainEmptied = previousMainStack.count > 0 && currentMainStack.isEmpty && !swappedHands
            val wasOffEmptied = previousOffStack.count > 0 && currentOffStack.isEmpty && !swappedHands
            val shouldRefillMain = (wasMainEmptied || ItemStack.canCombine(currentMainStack, previousMainStack)) &&
                currentMainStack.count < previousMainStack.count &&
                (WorldConfig.refillNonStackables || previousMainStack.isStackable)
            val shouldRefillOff = (wasOffEmptied || ItemStack.canCombine(currentOffStack, previousOffStack)) &&
                currentOffStack.count < previousOffStack.count &&
                (WorldConfig.refillNonStackables || previousOffStack.isStackable) &&
                WorldConfig.refillOffhand
            val doRefill = allowsRefill && currentSlot == previousSlot && !swappedHands && (shouldRefillMain || shouldRefillOff)

            if (!doRefill) return
            if (shouldRefillMain) {
                refill(player, currentSlot, previousMainStack, previousMainStack.count - currentMainStack.count)
            } else {
                refill(
                    player,
                    PlayerInventory.OFF_HAND_SLOT,
                    previousOffStack,
                    previousOffStack.count - currentOffStack.count,
                )
            }
        }

        private fun refill(player: ServerPlayerEntity, slot: Int, itemType: ItemStack, amount: Int) {
            if (amount <= 0) return
            var amount = amount
            val containerSlots = Utils.getContainers(player, Mod.REFILL_ENCHANTMENT)
            for (container in containerSlots) {
                val containerInventory = Utils.getContainerInventory(container, player)
                var updateContainer = false
                for (innerStack in containerInventory) {
                    if (innerStack.isEmpty) continue
                    val refilled = tryRefillSlot(player.inventory, innerStack, slot, itemType, amount)
                    if (refilled > 0) {
                        amount -= refilled
                        updateContainer = true
                        if (amount <= 0) break
                    }
                }
                if (updateContainer) Utils.setContainerInventory(container, containerInventory)
            }
        }

        private fun tryRefillSlot(inventory: PlayerInventory, from: ItemStack, slot: Int, itemType: ItemStack, amount: Int): Int {
            val to = inventory.getStack(slot)

            if (!ItemStack.canCombine(itemType, from)) return 0
            if (!to.isEmpty && !ItemStack.canCombine(to, from)) return 0

            val transferCount = minOf(amount, itemType.maxCount - to.count, from.count)
            if (transferCount <= 0) return 0

            if (to.isEmpty) {
                val stack = from.copy()
                stack.count = transferCount
                inventory.setStack(slot, stack)
            } else {
                to.increment(transferCount)
            }
            from.decrement(transferCount)
            return transferCount
        }
    }
}
