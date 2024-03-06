package de.rubixdev.enchantedshulkers.interfaces

import net.minecraft.item.ItemStack

@Suppress("FunctionName")
interface ProjectileHolder {
    fun `enchantedShulkers$getProjectileSlot`(expectedStack: ItemStack): Int?

    fun `enchantedShulkers$getProjectileType`(): ItemStack
}
