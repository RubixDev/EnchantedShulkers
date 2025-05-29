package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.config.WorldConfig
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity

//#if MC >= 12006
class VacuumEnchantment : ContainerEnchantment(Mod.PORTABLE_CONTAINER_TAG) {
//#else
//$$ class VacuumEnchantment : ContainerEnchantment(Mod.PORTABLE_CONTAINER_TARGET) {
//#endif
    override fun generate() = WorldConfig.generateVacuum

    companion object {
        @JvmStatic
        fun onItemPickup(player: ServerPlayerEntity, stack: ItemStack): Boolean {
            if (player.isCreative && !WorldConfig.creativeVacuum) return false
            return SiphonEnchantment.onItemPickup(player, stack, Mod.VACUUM_ENCHANTMENT, false, WorldConfig.weakerVacuum)
        }
    }
}
