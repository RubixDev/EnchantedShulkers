package de.rubixdev.enchantedshulkers.interfaces

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList

@Suppress("FunctionName")
interface EnchantableBlockEntity {
    fun `enchantedShulkers$getEnchantments`(): NbtList

    fun `enchantedShulkers$setEnchantments`(enchantments: NbtList)

    fun `enchantedShulkers$toClientNbt`() = NbtCompound().apply {
        // don't include the shulker inventory to reduce packet size
        put("Enchantments", `enchantedShulkers$getEnchantments`())
    }
}
