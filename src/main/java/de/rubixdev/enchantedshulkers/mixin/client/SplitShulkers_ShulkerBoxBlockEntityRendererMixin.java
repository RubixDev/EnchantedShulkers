package de.rubixdev.enchantedshulkers.mixin.client;

import com.bawnorton.mixinsquared.TargetHandler;
import cursedflames.splitshulkers.SplitShulkerBoxBlockEntity;
import de.rubixdev.enchantedshulkers.ClientMod;
import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.function.Function;

@Mixin(value = ShulkerBoxBlockEntityRenderer.class, priority = 1100)
public class SplitShulkers_ShulkerBoxBlockEntityRendererMixin {
    @SuppressWarnings({"MixinAnnotationTarget", "InvalidMemberReference", "CancellableInjectionUsage"}) // MixinSquared
    @TargetHandler(
            mixin = "cursedflames.splitshulkers.mixin.client.MixinShulkerBoxRenderer",
            name = "onRender")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void splitShulkersCompat(ShulkerBoxBlockEntity shulkerBox, float f, MatrixStack poseStack, VertexConsumerProvider multiBufferSource, int i, int j, CallbackInfo ci, Direction direction, SpriteIdentifier material, CallbackInfo ci2) {
        SplitShulkerBoxBlockEntity splitShulker = (SplitShulkerBoxBlockEntity) shulkerBox;
        if (Objects.equals(splitShulker.getColor(), splitShulker.splitshulkers_getSecondaryColor())) {
            ci2.cancel();
        }
    }

    @SuppressWarnings({"MixinAnnotationTarget", "InvalidMemberReference", "UnresolvedMixinReference"}) // MixinSquared
    @TargetHandler(
            mixin = "cursedflames.splitshulkers.mixin.client.MixinShulkerBoxRenderer",
            name = "onRender")
    @Redirect(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;"))
    private VertexConsumer getVertexConsumerSplitShulkers(
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
}
