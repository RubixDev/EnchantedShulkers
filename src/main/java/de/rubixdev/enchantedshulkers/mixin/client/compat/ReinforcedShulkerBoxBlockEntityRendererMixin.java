package de.rubixdev.enchantedshulkers.mixin.client.compat;

import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import atonkish.reinfcore.util.ReinforcingMaterial;
import atonkish.reinfshulker.block.entity.ReinforcedShulkerBoxBlockEntity;
import atonkish.reinfshulker.client.render.block.entity.ReinforcedShulkerBoxBlockEntityRenderer;
import atonkish.reinfshulker.util.ReinforcingMaterialSettings;
import com.google.common.collect.ImmutableList;
import de.rubixdev.enchantedshulkers.ClientMod;
import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static de.rubixdev.enchantedshulkers.ClientMod.*;

@Restriction(require = @Condition("reinfshulker"))
@Mixin(ReinforcedShulkerBoxBlockEntityRenderer.class)
public class ReinforcedShulkerBoxBlockEntityRendererMixin {
    @Unique private static final List<String> reinforcingMaterials = Arrays.stream(ReinforcingMaterialSettings.values())
        .map(ReinforcingMaterialSettings::getMaterial)
        .map(ReinforcingMaterial::getName)
        .toList();
    @Unique private static final Map<String, SpriteIdentifier> REINFORCED_CLOSED_SHULKER_TEXTURE_ID_MAP = new HashMap<>() {
        {
            for (String material : reinforcingMaterials) {
                put(
                    material,
                    new SpriteIdentifier(
                        TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE,
                        new Identifier(Mod.MOD_ID, "entity/reinforced_shulker/" + material + "/closed_shulker")
                    )
                );
            }
        }
    };
    @Unique private static final Map<String, List<SpriteIdentifier>> REINFORCED_CLOSED_COLORED_SHULKER_TEXTURE_ID_MAP =
        new HashMap<>() {
            {
                for (String material : reinforcingMaterials) {
                    put(
                        material,
                        COLORS.stream()
                            .map(
                                string -> new SpriteIdentifier(
                                    TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE,
                                    new Identifier(
                                        Mod.MOD_ID,
                                        "entity/reinforced_shulker/" + material + "/closed_shulker_" + string
                                    )
                                )
                            )
                            .collect(ImmutableList.toImmutableList())
                    );
                }
            }
        };

    @ModifyVariable(
        method = "render(Latonkish/reinfshulker/block/entity/ReinforcedShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"),
        ordinal = 0
    )
    private SpriteIdentifier modifySpriteIdentifier(
        SpriteIdentifier value,
        ReinforcedShulkerBoxBlockEntity shulkerBox,
        float f
    ) {
        if (!ClientMod.customModels() || !Utils.shouldGlint(shulkerBox) || shulkerBox.getAnimationProgress(f) > 0.01f)
            return value;
        DyeColor dyeColor;
        return (dyeColor = shulkerBox.getColor()) == null
            ? REINFORCED_CLOSED_SHULKER_TEXTURE_ID_MAP.get(shulkerBox.getMaterial().getName())
            : REINFORCED_CLOSED_COLORED_SHULKER_TEXTURE_ID_MAP.get(shulkerBox.getMaterial().getName())
                .get(dyeColor.getId());
    }

    @Redirect(
        method = "render(Latonkish/reinfshulker/block/entity/ReinforcedShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/SpriteIdentifier;getVertexConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/util/function/Function;)Lnet/minecraft/client/render/VertexConsumer;"
        )
    )
    private VertexConsumer getVertexConsumer(
        SpriteIdentifier instance,
        VertexConsumerProvider vertexConsumers,
        Function<Identifier, RenderLayer> layerFactory,
        ReinforcedShulkerBoxBlockEntity shulkerBoxBlockEntity
    ) {
        if (!ClientMod.glintWhenPlaced() || !Utils.shouldGlint(shulkerBoxBlockEntity))
            return instance.getVertexConsumer(vertexConsumers, layerFactory);
        return instance.getSprite()
            .getTextureSpecificVertexConsumer(
                ItemRenderer
                    .getDirectItemGlintConsumer(vertexConsumers, instance.getRenderLayer(layerFactory), false, true)
            );
    }

    @Redirect(
        method = "render(Latonkish/reinfshulker/block/entity/ReinforcedShulkerBoxBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
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
        ReinforcedShulkerBoxBlockEntity shulkerBox,
        float f
    ) {
        if (!ClientMod.customModels() || !Utils.shouldGlint(shulkerBox) || shulkerBox.getAnimationProgress(f) > 0.01f) {
            instance.render(matrixStack, vertexConsumer, light, overlay, red, green, blue, alpha);
        } else {
            CLOSED_BOX.render(matrixStack, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
    }
}
