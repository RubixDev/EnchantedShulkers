package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.Utils
import de.rubixdev.enchantedshulkers.config.WorldConfig
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

class VoidEnchantment : ContainerEnchantment(Mod.PORTABLE_CONTAINER_TARGET) {
    override fun generate() = WorldConfig.generateVoid

    companion object {
        @JvmStatic
        fun onItemPickup(player: ServerPlayerEntity, stack: ItemStack): Boolean {
            if (player.isCreative && !WorldConfig.creativeVoid || stack.isEmpty) return false
            val item = stack.item
            val containerSlots = Utils.getContainers(player, Mod.VOID_ENCHANTMENT)
            for (container in containerSlots) {
                val containerInventory = Utils.getContainerInventory(container, player)
                for (innerStack in containerInventory) {
                    if (innerStack.isOf(item)) {
                        stack.count = 0
                        return true
                    }
                }
            }
            return false
        }
    }
}
