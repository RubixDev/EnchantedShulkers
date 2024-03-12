package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.ClientConfig;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static de.rubixdev.enchantedshulkers.ClientMod.*;

@Mixin(value = ShulkerBoxBlockEntityRenderer.class, priority = 999)
public class ShulkerBoxBlockEntityRendererMixin_999 {
    @ModifyVariable(
        method = "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
            shift = At.Shift.BEFORE
        ),
        ordinal = 0
    )
    private SpriteIdentifier modifySpriteIdentifier(
        SpriteIdentifier value,
        ShulkerBoxBlockEntity shulkerBoxBlockEntity,
        float f
    ) {
        if (
            !ClientConfig.customModels()
                || !Utils.shouldGlint(shulkerBoxBlockEntity)
                || shulkerBoxBlockEntity.getAnimationProgress(f) > 0.01f
                || Utils.hasTwoColors(shulkerBoxBlockEntity)
        ) return value;
        DyeColor dyeColor;
        return (dyeColor = shulkerBoxBlockEntity.getColor()) == null
            ? CLOSED_SHULKER_TEXTURE_ID
            : CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS.get(dyeColor.getId());
    }
}
