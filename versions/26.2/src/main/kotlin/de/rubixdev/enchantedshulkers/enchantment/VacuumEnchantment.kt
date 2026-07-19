package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.Utils
import net.minecraft.core.NonNullList
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * 26.2 port - see SiphonEnchantment.kt's class doc.
 *
 * TODO(26.2): `creativeVacuum`/`weakerVacuum` WorldConfig toggles dropped for now (creative always
 * allowed; "weaker" require-existing-stack variant always off, matching the original defaults).
 */
object VacuumEnchantment {
    @JvmStatic
    fun onItemPickup(player: ServerPlayer, stack: ItemStack): Boolean {
        val vacuumHolder = Utils.enchantmentHolder(player.registryAccess(), Mod.VACUUM_KEY)
        val augmentHolder = Utils.enchantmentHolder(player.registryAccess(), Mod.AUGMENT_KEY)
        val containerSlots = Utils.getContainers(player, vacuumHolder)

        var usedVacuum = false
        for (container in containerSlots) {
            if (stack.isEmpty) return usedVacuum
            val containerInventory = Utils.getContainerInventory(container, augmentHolder)
            var updateContainer = false
            for (i in 0 until containerInventory.size) {
                if (stack.isEmpty) break
                if (tryFillSlot(stack, containerInventory, i)) updateContainer = true
            }
            if (updateContainer) {
                usedVacuum = true
                Utils.setContainerInventory(container, containerInventory)
            }
        }
        return usedVacuum
    }

    private fun tryFillSlot(from: ItemStack, containerInventory: NonNullList<ItemStack>, toIndex: Int): Boolean {
        val to = containerInventory[toIndex]
        if (to.isEmpty) {
            containerInventory[toIndex] = from.copy()
            from.count = 0
            return true
        }
        if (!ItemStack.isSameItemSameComponents(from, to)) return false
        val transferCount = minOf(to.maxStackSize - to.count, from.count)
        if (transferCount <= 0) return false
        to.grow(transferCount)
        from.shrink(transferCount)
        return true
    }
}
