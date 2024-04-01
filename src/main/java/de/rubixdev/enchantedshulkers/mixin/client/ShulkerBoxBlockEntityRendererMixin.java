package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.Utils;
import java.util.function.Function;
import de.rubixdev.enchantedshulkers.config.ClientConfig;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static de.rubixdev.enchantedshulkers.ClientMod.*;

@Mixin(value = ShulkerBoxBlockEntityRenderer.class, priority = 1001)
public class ShulkerBoxBlockEntityRendererMixin {
    @Redirect(
        method = "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;"
        )
    )
    private VertexConsumer getVertexConsumer(
        SpriteIdentifier instance,
        VertexConsumerProvider vertexConsumers,
        Function<Identifier, RenderLayer> layerFactory,
        ShulkerBoxBlockEntity shulkerBoxBlockEntity,
        float f,
        MatrixStack matrices
    ) {
        if (!ClientConfig.glintWhenPlaced() || !Utils.shouldGlint(shulkerBoxBlockEntity))
            return instance.getVertexConsumer(vertexConsumers, layerFactory);
        return instance.getSprite()
            .getTextureSpecificVertexConsumer(
                Utils.getGlintVertexConsumer(vertexConsumers, instance.getRenderLayer(layerFactory), matrices)
            );
    }

    @Redirect(
        method = "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/ShulkerEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"
        )
    )
    private void renderClosedBox(
        ShulkerEntityModel<?> instance,
        MatrixStack matrixStack,
        VertexConsumer vertexConsumer,
        int light,
        int overlay,
        float red,
        float green,
        float blue,
        float alpha,
        ShulkerBoxBlockEntity shulkerBoxBlockEntity,
        float f
    ) {
        if (
            !ClientConfig.customModels()
                || !Utils.shouldGlint(shulkerBoxBlockEntity)
                || shulkerBoxBlockEntity.getAnimationProgress(f) > 0.01f
        ) {
            instance.render(matrixStack, vertexConsumer, light, overlay, red, green, blue, alpha);
        } else {
            CLOSED_BOX.render(matrixStack, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
    }
}
