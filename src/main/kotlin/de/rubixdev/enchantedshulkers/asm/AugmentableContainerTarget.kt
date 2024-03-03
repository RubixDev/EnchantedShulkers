package de.rubixdev.enchantedshulkers.asm

import de.rubixdev.enchantedshulkers.Utils
import net.minecraft.item.Item

class AugmentableContainerTarget : EnchantmentTargetMixin() {
    override fun isAcceptableItem(item: Item?) = Utils.canAugment(item)
}
