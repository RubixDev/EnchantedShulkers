package de.rubixdev.enchanted_shulkers.asm;

import de.rubixdev.enchanted_shulkers.Utils;
import de.rubixdev.enchanted_shulkers.mixin.EnchantmentTargetMixin;
import net.minecraft.item.Item;

@SuppressWarnings("unused") // used in de.rubixdev.enchanted_shulkers.asm.EnumInjector
public class PortableContainerTarget extends EnchantmentTargetMixin {
    @Override
    public boolean isAcceptableItem(Item item) {
        return Utils.canEnchant(item);
    }
}
