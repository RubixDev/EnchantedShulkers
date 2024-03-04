package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.Utils
import de.rubixdev.enchantedshulkers.config.WorldConfig
import kotlin.math.min
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.collection.DefaultedList

class SiphonEnchantment : ContainerEnchantment(Mod.PORTABLE_CONTAINER_TARGET) {
    override fun generate() = WorldConfig.generateSiphon

    companion object {
        @JvmStatic
        fun onItemPickup(player: ServerPlayerEntity, stack: ItemStack) =
            (!player.isCreative || WorldConfig.creativeSiphon) && onItemPickup(
                player,
                stack,
                Mod.SIPHON_ENCHANTMENT,
                requireStack = true,
                WorldConfig.strongerSiphon,
            )

        fun onItemPickup(
            player: ServerPlayerEntity,
            stack: ItemStack,
            enchantment: Enchantment,
            requireStack: Boolean,
            strongerSiphon: Boolean,
        ): Boolean {
            if (strongerSiphon) return onItemPickupStrongerSiphon(player, stack, enchantment)
            val containerSlots = Utils.getContainers(player, enchantment)

            var usedSiphon = false
            for (container in containerSlots) {
                if (stack.isEmpty) return usedSiphon
                val containerInventory = Utils.getContainerInventory(container, player)
                var updateContainer = false
                for (i in 0 until containerInventory.size) {
                    val innerStack = containerInventory[i]
                    if (innerStack.isEmpty && requireStack) continue
                    if (trySiphonStack(stack, innerStack, containerInventory, i)) {
                        updateContainer = true
                        if (stack.isEmpty) break
                    }
                }
                if (updateContainer) {
                    usedSiphon = true
                    Utils.setContainerInventory(container, containerInventory)
                }
            }
            return usedSiphon
        }

        private fun onItemPickupStrongerSiphon(player: ServerPlayerEntity, stack: ItemStack, enchantment: Enchantment): Boolean {
            val containerSlots = Utils.getContainers(player, enchantment)

            var usedSiphon = false
            for (container in containerSlots) {
                if (stack.isEmpty) return usedSiphon
                val item = stack.item
                val foundItemSlots = mutableListOf<Int>()
                val emptyItemSlots = mutableListOf<Int>()
                val containerInventory = Utils.getContainerInventory(container, player)
                for (i in 0 until containerInventory.size) {
                    val innerStack = containerInventory[i]
                    if (innerStack.isEmpty) {
                        emptyItemSlots.add(i)
                    } else if (innerStack.isOf(item)) {
                        foundItemSlots.add(i)
                    }
                }

                // don't siphon if there are no items of that type already in the container
                if (foundItemSlots.isEmpty()) continue

                var updateContainer = false
                // try to add to the existing stacks first
                for (slotId in foundItemSlots) {
                    if (stack.isEmpty) break
                    if (trySiphonStack(stack, containerInventory[slotId], containerInventory, slotId)) {
                        updateContainer = true
                    }
                }
                // then fill empty slots
                for (slotId in emptyItemSlots) {
                    if (stack.isEmpty) break
                    if (trySiphonStack(stack, containerInventory[slotId], containerInventory, slotId)) {
                        updateContainer = true
                    }
                }

                if (updateContainer) {
                    usedSiphon = true
                    Utils.setContainerInventory(container, containerInventory)
                }
            }
            return usedSiphon
        }

        private fun trySiphonStack(from: ItemStack, to: ItemStack, containerInventory: DefaultedList<ItemStack>, toIndex: Int): Boolean {
            if (to.isEmpty) {
                containerInventory[toIndex] = from.copy()
                from.count = 0
                return true
            }

            if (!ItemStack.canCombine(from, to)) return false
            val transferCount = min(to.maxCount - to.count, from.count)
            if (transferCount <= 0) return false

            to.increment(transferCount)
            from.decrement(transferCount)
            return true
        }
    }
}
