package de.rubixdev.enchantedshulkers.mixin.compat;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.kyrptonaught.shulkerutils.ItemStackInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Restriction(require = @Condition("shulkerutils"))
@Mixin(ItemStackInventory.class)
public interface QuickShulker_ItemStackInventoryAccessor {
    @Accessor("itemStack")
    ItemStack getItemStack();
}
