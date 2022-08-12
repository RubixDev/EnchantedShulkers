package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.EnchantableBlockEntity;
import de.rubixdev.enchantedshulkers.config.ClientConfig;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShulkerBoxBlockEntity.class)
public class ShulkerBoxBlockEntityMixin {
    @Redirect(
            method = "getContainerName",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"))
    public MutableText getContainerName(String key) {
        MutableText text = Text.translatable(key);
        if (this instanceof EnchantableBlockEntity enchantableBlockEntity
                && ClientConfig.coloredNames()
                && !enchantableBlockEntity.getEnchantments().isEmpty()) {
            text.setStyle(Style.EMPTY.withFormatting(Formatting.AQUA));
        }
        return text;
    }
}
