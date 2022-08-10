package de.rubixdev.enchantedshulkers.asm;

import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.mixin.EnchantmentTargetMixin;
import net.minecraft.item.Item;

@SuppressWarnings("unused") // used in de.rubixdev.enchantedshulkers.asm.EnumInjector
public class PortableContainerTarget extends EnchantmentTargetMixin {
    @Override
    public boolean isAcceptableItem(Item item) {
        return Utils.canEnchant(item);
    }
}
