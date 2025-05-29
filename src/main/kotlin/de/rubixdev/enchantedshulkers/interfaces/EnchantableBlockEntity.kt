package de.rubixdev.enchantedshulkers.interfaces

import net.minecraft.nbt.NbtCompound

//#if MC >= 12006
import net.minecraft.block.entity.BlockEntity
import net.minecraft.component.type.ItemEnchantmentsComponent
import net.minecraft.nbt.NbtOps
//#else
//$$ import net.minecraft.nbt.NbtList
//#endif

@Suppress("FunctionName")
interface EnchantableBlockEntity {
    //#if MC >= 12006
    fun `enchantedShulkers$getEnchantments`(): ItemEnchantmentsComponent

    fun `enchantedShulkers$setEnchantments`(enchantments: ItemEnchantmentsComponent)
    //#else
    //$$ fun `enchantedShulkers$getEnchantments`(): NbtList

    //$$ fun `enchantedShulkers$setEnchantments`(enchantments: NbtList)
    //#endif

    fun `enchantedShulkers$toClientNbt`() =
        //#if MC >= 12006
        when (this) {
            is BlockEntity -> BlockEntity.Components.CODEC.encodeStart(NbtOps.INSTANCE, components).getOrThrow() as NbtCompound
            else -> NbtCompound()
        }
        //#else
        //$$ NbtCompound().apply {
        //$$     put("Enchantments", `enchantedShulkers$getEnchantments`())
        //$$ }
        //#endif
}
