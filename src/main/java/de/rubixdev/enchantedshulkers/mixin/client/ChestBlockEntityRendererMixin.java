package de.rubixdev.enchantedshulkers.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.ClientConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static de.rubixdev.enchantedshulkers.ClientMod.*;

@Mixin(ChestBlockEntityRenderer.class)
public abstract class ChestBlockEntityRendererMixin<T extends BlockEntity> {
    @Shadow
    private boolean christmas;

    @ModifyExpressionValue(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;"
        )
    )
    private VertexConsumer modifyVertexConsumer(
        VertexConsumer original,
        T entity,
        float tickDelta,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        @Local ChestType chestType,
        @Local SpriteIdentifier originalSpriteIdentifier,
        @Local(ordinal = 2) float g
    ) {
        if (!ClientConfig.glintWhenPlaced() || !Utils.shouldGlint(entity)) return original;
        SpriteIdentifier spriteIdentifier = !ClientConfig.customModels() || g > 0.01f ? originalSpriteIdentifier
            : entity instanceof EnderChestBlockEntity ? CLOSED_ENDER_TEXTURE_ID
            : christmas ? switch (chestType) {
                case SINGLE -> CLOSED_CHRISTMAS_TEXTURE_ID;
                case LEFT -> CLOSED_CHRISTMAS_LEFT_TEXTURE_ID;
                case RIGHT -> CLOSED_CHRISTMAS_RIGHT_TEXTURE_ID;
            }
            : entity instanceof TrappedChestBlockEntity ? switch (chestType) {
                case SINGLE -> CLOSED_TRAPPED_TEXTURE_ID;
                case LEFT -> CLOSED_TRAPPED_LEFT_TEXTURE_ID;
                case RIGHT -> CLOSED_TRAPPED_RIGHT_TEXTURE_ID;
            }
            : switch (chestType) {
                case SINGLE -> CLOSED_NORMAL_TEXTURE_ID;
                case LEFT -> CLOSED_NORMAL_LEFT_TEXTURE_ID;
                case RIGHT -> CLOSED_NORMAL_RIGHT_TEXTURE_ID;
            };
        return new SpriteTexturedVertexConsumer(
            ItemRenderer.getDirectItemGlintConsumer(
                vertexConsumers,
                spriteIdentifier.getRenderLayer(RenderLayer::getEntityCutout),
                false,
                true
            ),
            spriteIdentifier.getSprite()
        );
    }

    @Unique private static final ModelPart CLOSED_SINGLE_CHEST;
    @Unique private static final ModelPart CLOSED_DOUBLE_CHEST_LEFT;
    @Unique private static final ModelPart CLOSED_DOUBLE_CHEST_RIGHT;

    static {
        {
            ModelData modelData = new ModelData();
            ModelPartData modelPartData = modelData.getRoot();
            modelPartData.addChild(
                "box",
                ModelPartBuilder.create().uv(0, 0).cuboid(1f, 0f, 1f, 14f, 14f, 14f),
                ModelTransform.NONE
            );
            modelPartData.addChild(
                "latch",
                ModelPartBuilder.create().uv(0, 0).cuboid(7f, -2f, 14f, 2f, 4f, 1f),
                ModelTransform.pivot(0f, 9f, 1f)
            );
            CLOSED_SINGLE_CHEST = TexturedModelData.of(modelData, 64, 32).createModel();
        }

        {
            ModelData modelData = new ModelData();
            ModelPartData modelPartData = modelData.getRoot();
            modelPartData.addChild(
                "box",
                ModelPartBuilder.create().uv(0, 0).cuboid(0f, 0f, 1f, 15f, 14f, 14f),
                ModelTransform.NONE
            );
            modelPartData.addChild(
                "latch",
                ModelPartBuilder.create().uv(0, 0).cuboid(0f, -2f, 14f, 1f, 4f, 1f),
                ModelTransform.pivot(0f, 9f, 1f)
            );
            CLOSED_DOUBLE_CHEST_LEFT = TexturedModelData.of(modelData, 64, 32).createModel();
        }

        {
            ModelData modelData = new ModelData();
            ModelPartData modelPartData = modelData.getRoot();
            modelPartData.addChild(
                "box",
                ModelPartBuilder.create().uv(0, 0).cuboid(1f, 0f, 1f, 15f, 14f, 14f),
                ModelTransform.NONE
            );
            modelPartData.addChild(
                "latch",
                ModelPartBuilder.create().uv(0, 0).cuboid(15f, -2f, 14f, 1f, 4f, 1f),
                ModelTransform.pivot(0f, 9f, 1f)
            );
            CLOSED_DOUBLE_CHEST_RIGHT = TexturedModelData.of(modelData, 64, 32).createModel();
        }
    }

    @WrapOperation(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/entity/ChestBlockEntityRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;FII)V",
            ordinal = 0
        )
    )
    private void renderGlintWhenClosedOnLeftDoubleChest(
        ChestBlockEntityRenderer<? extends BlockEntity> instance,
        MatrixStack matrices,
        VertexConsumer vertices,
        ModelPart lid,
        ModelPart latch,
        ModelPart base,
        float openFactor,
        int light,
        int overlay,
        Operation<Void> original,
        T entity
    ) {
        if (!ClientConfig.customModels() || !Utils.shouldGlint(entity) || openFactor > 0.01f) {
            original.call(instance, matrices, vertices, lid, latch, base, openFactor, light, overlay);
        } else {
            CLOSED_DOUBLE_CHEST_LEFT.render(matrices, vertices, light, overlay);
        }
    }

    @WrapOperation(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/entity/ChestBlockEntityRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;FII)V",
            ordinal = 1
        )
    )
    private void renderGlintWhenClosedOnRightDoubleChest(
        ChestBlockEntityRenderer<? extends BlockEntity> instance,
        MatrixStack matrices,
        VertexConsumer vertices,
        ModelPart lid,
        ModelPart latch,
        ModelPart base,
        float openFactor,
        int light,
        int overlay,
        Operation<Void> original,
        T entity
    ) {
        if (!ClientConfig.customModels() || !Utils.shouldGlint(entity) || openFactor > 0.01f) {
            original.call(instance, matrices, vertices, lid, latch, base, openFactor, light, overlay);
        } else {
            CLOSED_DOUBLE_CHEST_RIGHT.render(matrices, vertices, light, overlay);
        }
    }

    @WrapOperation(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/block/entity/ChestBlockEntityRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/model/ModelPart;FII)V",
            ordinal = 2
        )
    )
    private void renderGlintWhenClosedOnSingleChest(
        ChestBlockEntityRenderer<? extends BlockEntity> instance,
        MatrixStack matrices,
        VertexConsumer vertices,
        ModelPart lid,
        ModelPart latch,
        ModelPart base,
        float openFactor,
        int light,
        int overlay,
        Operation<Void> original,
        T entity
    ) {
        if (!ClientConfig.customModels() || !Utils.shouldGlint(entity) || openFactor > 0.01f) {
            original.call(instance, matrices, vertices, lid, latch, base, openFactor, light, overlay);
        } else {
            CLOSED_SINGLE_CHEST.render(matrices, vertices, light, overlay);
        }
    }
}
