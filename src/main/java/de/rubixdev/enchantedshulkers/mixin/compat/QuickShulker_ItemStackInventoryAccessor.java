package de.rubixdev.enchantedshulkers.mixin.compat;

import net.kyrptonaught.shulkerutils.ItemStackInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackInventory.class)
public interface QuickShulker_ItemStackInventoryAccessor {
    @Accessor("itemStack")
    ItemStack getItemStack();
}
