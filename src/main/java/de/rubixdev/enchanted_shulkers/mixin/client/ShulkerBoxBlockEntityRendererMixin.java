package de.rubixdev.enchanted_shulkers.mixin.client;

import de.rubixdev.enchanted_shulkers.EnchantedBlockEntity;
import java.util.function.Function;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.ShulkerBoxBlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
        return new SpriteTexturedVertexConsumer(
                ItemRenderer.getDirectItemGlintConsumer(
                        vertexConsumers,
                        instance.getRenderLayer(layerFactory),
                        false,
                        !((EnchantedBlockEntity) shulkerBoxBlockEntity)
                                .getEnchantments()
                                .isEmpty()),
                instance.getSprite());
    }
}
