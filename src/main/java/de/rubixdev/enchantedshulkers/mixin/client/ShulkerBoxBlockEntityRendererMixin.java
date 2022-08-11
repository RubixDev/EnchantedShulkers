package de.rubixdev.enchantedshulkers.mixin.client;

import static de.rubixdev.enchantedshulkers.SpriteAtlasStorage.CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS;
import static de.rubixdev.enchantedshulkers.SpriteAtlasStorage.CLOSED_SHULKER_TEXTURE_ID;

import de.rubixdev.enchantedshulkers.SpriteAtlasStorage;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.ClientConfig;
import java.util.function.Function;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntityRenderer.class)
public class ShulkerBoxBlockEntityRendererMixin {
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
        if (!ClientConfig.get().glintWhenPlaced() || !Utils.shouldGlint(shulkerBoxBlockEntity))
            return instance.getVertexConsumer(vertexConsumers, layerFactory);
        return instance.getSprite()
                .getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(
                        vertexConsumers, instance.getRenderLayer(layerFactory), false, true));
    }

    @Unique
    private static final ModelPart CLOSED_BOX;

    static {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(
                "box",
                ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, 8.0f, -8.0f, 16.0f, 16.0f, 16.0f),
                ModelTransform.NONE);
        CLOSED_BOX = TexturedModelData.of(modelData, 64, 32).createModel();
    }

    @Inject(
            method =
                    "render(Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/client/render/entity/model/ShulkerEntityModel;getLid()Lnet/minecraft/client/model/ModelPart;"),
            cancellable = true)
    private void renderGlint(
            ShulkerBoxBlockEntity shulkerBoxBlockEntity,
            float f,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int i,
            int j,
            CallbackInfo ci) {
        if (!ClientConfig.get().customModels()
                || !Utils.shouldGlint(shulkerBoxBlockEntity)
                || shulkerBoxBlockEntity.getAnimationProgress(f) > 0.01f) return;

        DyeColor dyeColor;
        SpriteIdentifier spriteIdentifier = (dyeColor = shulkerBoxBlockEntity.getColor()) == null
                ? CLOSED_SHULKER_TEXTURE_ID
                : CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS.get(dyeColor.getId());

        VertexConsumer vertexConsumer = SpriteAtlasStorage.closedContainersAtlasTexture
                .getSprite(spriteIdentifier.getTextureId())
                .getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(
                        vertexConsumerProvider,
                        spriteIdentifier.getRenderLayer(RenderLayer::getEntityCutout),
                        false,
                        true));
        CLOSED_BOX.render(matrixStack, vertexConsumer, i, j, 1.0f, 1.0f, 1.0f, 1.0f);
        matrixStack.pop();
        ci.cancel();
    }
}
