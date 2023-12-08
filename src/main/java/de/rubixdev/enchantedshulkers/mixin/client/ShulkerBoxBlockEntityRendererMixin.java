package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.ClientMod;
import de.rubixdev.enchantedshulkers.Utils;
import java.util.function.Function;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import static de.rubixdev.enchantedshulkers.ClientMod.*;

@Mixin(ShulkerBoxBlockEntityRenderer.class)
public class ShulkerBoxBlockEntityRendererMixin {
    @ModifyVariable(
            method = "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"),
            name = "spriteIdentifier")
    private SpriteIdentifier modifySpriteIdentifier(SpriteIdentifier value, ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f) {
        if (!ClientMod.customModels()
                || !Utils.shouldGlint(shulkerBoxBlockEntity)
                || shulkerBoxBlockEntity.getAnimationProgress(f) > 0.01f
                || Utils.hasTwoColors(shulkerBoxBlockEntity)) return value;
        DyeColor dyeColor;
        return (dyeColor = shulkerBoxBlockEntity.getColor()) == null
                ? CLOSED_SHULKER_TEXTURE_ID
                : CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS.get(dyeColor.getId());
    }

    @Redirect(
            method =
                    "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;"))
    private VertexConsumer getVertexConsumer(
            SpriteIdentifier instance,
            VertexConsumerProvider vertexConsumers,
            Function<Identifier, RenderLayer> layerFactory,
            ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
        if (!ClientMod.glintWhenPlaced() || !Utils.shouldGlint(shulkerBoxBlockEntity))
            return instance.getVertexConsumer(vertexConsumers, layerFactory);
        return instance.getSprite()
                .getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(
                        vertexConsumers, instance.getRenderLayer(layerFactory), false, true));
    }

    @Redirect(
            method = "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/ShulkerEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void renderClosedBox(ShulkerEntityModel<?> instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha, ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f) {
        if (!ClientMod.customModels()
                || !Utils.shouldGlint(shulkerBoxBlockEntity)
                || shulkerBoxBlockEntity.getAnimationProgress(f) > 0.01f) {
            instance.render(matrixStack, vertexConsumer, light, overlay, red, green, blue, alpha);
        } else {
            CLOSED_BOX.render(matrixStack, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
    }
}
