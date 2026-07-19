package de.rubixdev.enchantedshulkers.interfaces

import net.minecraft.world.item.enchantment.ItemEnchantments

/**
 * 26.2 port. Was NbtList-based (`enchantedShulkers$getEnchantments(): NbtList`); block entities
 * still use NBT for their own persisted state in 26.2 (only ItemStack lost its NBT API - see the
 * port report), but [ItemEnchantments] is the natural Mojmap type to carry this data now (it's
 * what `EnchantmentHelper`/the enchantment component system consumes directly, avoiding a
 * NbtList<->ItemEnchantments conversion at every use site), and it has its own [ItemEnchantments.CODEC]
 * that works directly with the new `ValueInput`/`ValueOutput` block-entity persistence API (see
 * ChestBlockEntityMixin).
 */
@Suppress("FunctionName")
interface EnchantableBlockEntity {
    fun `enchantedShulkers$getEnchantments`(): ItemEnchantments

    fun `enchantedShulkers$setEnchantments`(enchantments: ItemEnchantments)
}
