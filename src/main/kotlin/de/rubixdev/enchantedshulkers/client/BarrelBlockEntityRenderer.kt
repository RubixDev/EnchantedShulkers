package de.rubixdev.enchantedshulkers.client

import de.rubixdev.enchantedshulkers.Utils.shouldGlint
import de.rubixdev.enchantedshulkers.config.ClientConfig
import de.rubixdev.enchantedshulkers.config.WorldConfig
import net.minecraft.block.entity.BarrelBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack

class BarrelBlockEntityRenderer(
    @Suppress("UNUSED_PARAMETER") ctx: BlockEntityRendererFactory.Context,
) : BlockEntityRenderer<BarrelBlockEntity> {
    // TODO: find a way to render barrel glint without using a custom block entity renderer
    //  or at least without disabling the normal block rendering
    //  or at least keep rendering unenchanted barrels normally
    override fun render(
        entity: BarrelBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int,
    ) {
        if (!ClientConfig.glintWhenPlaced || !WorldConfig.augmentableChestsAndBarrels) return

        matrices.push()

        val client = MinecraftClient.getInstance() ?: return
        val world = client.world ?: return
        val state = entity.cachedState
        val model = client.blockRenderManager.getModel(state)
        val vertices = if (entity.shouldGlint()) {
            VertexConsumers.union(
                vertexConsumers.getBuffer(RenderLayer.getDirectEntityGlint()),
                vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(model.particleSprite.atlasId)),
            )
        } else {
            vertexConsumers.getBuffer(RenderLayers.getEntityBlockLayer(state, false))
        }

        val color = client.blockRenderManager.blockColors.getColor(state, null, null, 0)
        val r = color.shr(16).and(0xff).toFloat() / 255f
        val g = color.shr(8).and(0xff).toFloat() / 255f
        val b = color.and(0xff).toFloat() / 255f
        client.blockRenderManager.modelRenderer.render(matrices.peek(), vertices, state, model, r, g, b, light, overlay)

        matrices.pop()
    }
}
