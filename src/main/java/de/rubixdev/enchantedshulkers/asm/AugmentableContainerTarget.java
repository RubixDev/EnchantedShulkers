package de.rubixdev.enchantedshulkers.asm;

import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.item.Item;

@SuppressWarnings("unused") // used in de.rubixdev.enchantedshulkers.asm.EnumInjector
public class AugmentableContainerTarget extends EnchantmentTargetMixin {
    @Override
    public boolean isAcceptableItem(Item item) {
        return Utils.canAugment(item);
    }
}
