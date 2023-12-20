package de.rubixdev.enchantedshulkers.asm;

import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("UnusedMixin") // this "mixin" should not be registered (https://github.com/Chocohead/Fabric-ASM#landing-amphibiously)
@Mixin(EnchantmentTarget.class)
public abstract class EnchantmentTargetMixin {
    @SuppressWarnings("unused") // implemented above, used by Minecraft
    @Shadow
    public abstract boolean isAcceptableItem(Item item);
}
