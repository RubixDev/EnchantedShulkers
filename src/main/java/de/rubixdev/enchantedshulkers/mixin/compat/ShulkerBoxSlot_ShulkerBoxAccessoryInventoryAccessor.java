package de.rubixdev.enchantedshulkers.mixin.compat;

import com.illusivesoulworks.shulkerboxslot.ShulkerBoxAccessoryInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerBoxAccessoryInventory.class)
public interface ShulkerBoxSlot_ShulkerBoxAccessoryInventoryAccessor {
    @Accessor("items")
    DefaultedList<ItemStack> getItems();

    @Accessor("shulkerBox")
    ItemStack getShulkerBox();
}
