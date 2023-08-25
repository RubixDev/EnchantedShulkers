package de.rubixdev.enchantedshulkers.asm;

import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("unused") // used in de.rubixdev.enchantedshulkers.asm.EnumInjector
public class PortableContainerTarget extends EnchantmentTargetMixin {
    @Override
    public boolean isAcceptableItem(Item item) {
        return Utils.canEnchant(item);
    }
}

@SuppressWarnings("UnusedMixin") // this "mixin" should not be registered (https://github.com/Chocohead/Fabric-ASM#landing-amphibiously)
@Mixin(EnchantmentTarget.class)
abstract class EnchantmentTargetMixin {
    @SuppressWarnings("unused") // implemented above, used by Minecraft
    @Shadow
    public abstract boolean isAcceptableItem(Item item);
}
