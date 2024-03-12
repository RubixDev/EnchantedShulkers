package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.ClientConfig;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.ChestBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static de.rubixdev.enchantedshulkers.ClientMod.CLOSED_ENDER_TEXTURE_ID;

@Mixin(ChestBlockEntityRenderer.class)
public abstract class ChestBlockEntityRendererMixin<T extends BlockEntity> {
    @Shadow
    protected abstract void render(
        MatrixStack matrices,
        VertexConsumer vertices,
        ModelPart lid,
        ModelPart latch,
        ModelPart base,
        float openFactor,
        int light,
        int overlay
    );

    @Shadow
    @Final
    private ModelPart singleChestLid;

    @Shadow
    @Final
    private ModelPart singleChestLatch;

    @Shadow
    @Final
    private ModelPart singleChestBase;

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;"
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION,
        cancellable = true
    )
    private void renderGlint(
        T entity,
        float tickDelta,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        CallbackInfo ci,
        World world,
        boolean bl,
        BlockState blockState,
        ChestType chestType,
        Block block,
        AbstractChestBlock<?> abstractChestBlock,
        boolean bl2,
        float f,
        DoubleBlockProperties.PropertySource<?> propertySource,
        float g,
        int i,
        SpriteIdentifier spriteIdentifier
    ) {
        if (!ClientConfig.glintWhenPlaced() || !(entity instanceof EnderChestBlockEntity) || !Utils.shouldGlint(entity))
            return;
        VertexConsumer vertexConsumer = new SpriteTexturedVertexConsumer(
            ItemRenderer.getDirectItemGlintConsumer(
                vertexConsumers,
                spriteIdentifier.getRenderLayer(RenderLayer::getEntityCutout),
                false,
                true
            ),
            spriteIdentifier.getSprite()
        );
        this.render(
            matrices,
            vertexConsumer,
            this.singleChestLid,
            this.singleChestLatch,
            this.singleChestBase,
            g,
            i,
            overlay
        );
        matrices.pop();
        ci.cancel();
    }

    @Unique private static final ModelPart CLOSED_CHEST;

    static {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(
            "box",
            ModelPartBuilder.create().uv(0, 0).cuboid(1.0f, 0.0f, 1.0f, 14.0f, 14.0f, 14.0f),
            ModelTransform.NONE
        );
        modelPartData.addChild(
            "latch",
            ModelPartBuilder.create().uv(0, 0).cuboid(7.0f, -1.0f, 15.0f, 2.0f, 4.0f, 1.0f),
            ModelTransform.pivot(0.0f, 8.0f, 0.0f)
        );
        CLOSED_CHEST = TexturedModelData.of(modelData, 64, 32).createModel();
    }

    @Inject(
        method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/TexturedRenderLayers;getChestTextureId(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/block/enums/ChestType;Z)Lnet/minecraft/client/util/SpriteIdentifier;"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void renderGlintWhenClosed(
        T entity,
        float tickDelta,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        CallbackInfo ci,
        World world,
        boolean bl,
        BlockState blockState,
        ChestType chestType,
        Block block,
        AbstractChestBlock<?> abstractChestBlock,
        boolean bl2,
        float f,
        DoubleBlockProperties.PropertySource<?> propertySource,
        float g,
        int i
    ) {
        if (
            !ClientConfig.customModels()
                || !(entity instanceof EnderChestBlockEntity)
                || !Utils.shouldGlint(entity)
                || g > 0.01f
        ) return;

        SpriteIdentifier spriteIdentifier = CLOSED_ENDER_TEXTURE_ID;
        VertexConsumer vertexConsumer = spriteIdentifier.getSprite()
            .getTextureSpecificVertexConsumer(
                ItemRenderer.getDirectItemGlintConsumer(
                    vertexConsumers,
                    spriteIdentifier.getRenderLayer(RenderLayer::getEntityCutout),
                    false,
                    true
                )
            );
        CLOSED_CHEST.render(matrices, vertexConsumer, i, overlay, 1.0f, 1.0f, 1.0f, 1.0f);
        matrices.pop();
        ci.cancel();
    }
}
