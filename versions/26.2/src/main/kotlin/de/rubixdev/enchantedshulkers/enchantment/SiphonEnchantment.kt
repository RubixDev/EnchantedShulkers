package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.Utils
import net.minecraft.core.Holder
import net.minecraft.core.NonNullList
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import kotlin.math.min

/**
 * 26.2 port. `Enchantment` is a non-subclassable `final record` in 26.2, so this is now a plain
 * object (the enchantment identity itself lives in `data/enchantedshulkers/enchantment/siphon.json`
 * + [Mod.SIPHON_KEY]) instead of an `Enchantment` subclass - see the port report / Mod.kt.
 *
 * TODO(26.2): config/ isn't ported yet, so the `creativeSiphon` and `strongerSiphon` WorldConfig
 * toggles are dropped for now (creative players are always allowed to siphon; the "stronger"
 * empty-slot-filling variant is always off, matching the original's default).
 */
object SiphonEnchantment {
    @JvmStatic
    fun onItemPickup(player: ServerPlayer, stack: ItemStack, enchantmentHolder: Holder<Enchantment>): Boolean {
        val containerSlots = Utils.getContainers(player, enchantmentHolder)
        val augmentHolder = Utils.enchantmentHolder(player.registryAccess(), Mod.AUGMENT_KEY)

        var usedSiphon = false
        for (container in containerSlots) {
            if (stack.isEmpty) return usedSiphon
            val containerInventory = Utils.getContainerInventory(container, augmentHolder)
            var updateContainer = false
            for (i in 0 until containerInventory.size) {
                val innerStack = containerInventory[i]
                if (innerStack.isEmpty) continue
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

    @JvmStatic
    fun trySiphonStack(from: ItemStack, to: ItemStack, containerInventory: NonNullList<ItemStack>, toIndex: Int): Boolean {
        if (to.isEmpty) return false // "stronger siphon" (fill empty slots) not ported - see class doc

        if (!ItemStack.isSameItemSameComponents(from, to)) return false
        val transferCount = min(to.maxStackSize - to.count, from.count)
        if (transferCount <= 0) return false

        to.grow(transferCount)
        from.shrink(transferCount)
        return true
    }
}
