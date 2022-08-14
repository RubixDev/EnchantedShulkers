package de.rubixdev.enchantedshulkers.mixin;

import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EnchantmentTarget.class)
public abstract class EnchantmentTargetMixin {
    @SuppressWarnings("unused") // used in de.rubixdev.enchantedshulkers.asm.PortableContainerTarget
    @Shadow
    public abstract boolean isAcceptableItem(Item item);
}
