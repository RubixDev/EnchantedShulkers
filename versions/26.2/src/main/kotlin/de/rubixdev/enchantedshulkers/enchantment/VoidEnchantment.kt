package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.Utils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/** 26.2 port - see SiphonEnchantment.kt's class doc. TODO(26.2): `creativeVoid` toggle dropped. */
object VoidEnchantment {
    @JvmStatic
    fun onItemPickup(player: ServerPlayer, stack: ItemStack): Boolean {
        if (stack.isEmpty) return false
        val voidHolder = Utils.enchantmentHolder(player.registryAccess(), Mod.VOID_KEY)
        val augmentHolder = Utils.enchantmentHolder(player.registryAccess(), Mod.AUGMENT_KEY)
        val item = stack.item
        val containerSlots = Utils.getContainers(player, voidHolder)
        for (container in containerSlots) {
            val containerInventory = Utils.getContainerInventory(container, augmentHolder)
            for (innerStack in containerInventory) {
                if (!innerStack.isEmpty && innerStack.item === item) {
                    stack.count = 0
                    return true
                }
            }
        }
        return false
    }
}
