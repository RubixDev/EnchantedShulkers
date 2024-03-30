package de.rubixdev.enchantedshulkers

import de.rubixdev.enchantedshulkers.Utils.id
import de.rubixdev.enchantedshulkers.client.BarrelBlockEntityRenderer
import de.rubixdev.enchantedshulkers.config.InvalidOptionValueException
import de.rubixdev.enchantedshulkers.config.WorldConfig
import de.rubixdev.enchantedshulkers.screen.BigAugmentedScreen
import de.rubixdev.enchantedshulkers.screen.ScreenHandlerTypes
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.model.*
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.nbt.NbtByte
import net.minecraft.util.DyeColor

//#if MC >= 12001
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking
import net.minecraft.nbt.NbtInt
//#endif

@Environment(EnvType.CLIENT)
object ClientMod : ClientModInitializer {
    @JvmField val CLOSED_ENDER_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_ender".id)
    @JvmField val CLOSED_NORMAL_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_normal".id)
    @JvmField val CLOSED_NORMAL_LEFT_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_normal_left".id)
    @JvmField val CLOSED_NORMAL_RIGHT_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_normal_right".id)
    @JvmField val CLOSED_TRAPPED_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_trapped".id)
    @JvmField val CLOSED_TRAPPED_LEFT_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_trapped_left".id)
    @JvmField val CLOSED_TRAPPED_RIGHT_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_trapped_right".id)
    @JvmField val CLOSED_CHRISTMAS_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_christmas".id)
    @JvmField val CLOSED_CHRISTMAS_LEFT_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_christmas_left".id)
    @JvmField val CLOSED_CHRISTMAS_RIGHT_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, "entity/chest/closed_christmas_right".id)

    @JvmField val COLORS = DyeColor.values().sortedBy { it.id }.map { it.getName() }
    @JvmField val CLOSED_SHULKER_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, "entity/shulker/closed_shulker".id)
    @JvmField val CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS = COLORS.map { SpriteIdentifier(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, "entity/shulker/closed_shulker_$it".id) }
    @JvmField val CLOSED_BOX: ModelPart = let {
        val modelData = ModelData()
        val modelPartData = modelData.root
        modelPartData.addChild(
            "box",
            ModelPartBuilder.create().uv(0, 0).cuboid(-8f, 8f, -8f, 16f, 16f, 16f),
            ModelTransform.NONE,
        )
        TexturedModelData.of(modelData, 64, 32).createModel()
    }

    override fun onInitializeClient() {
        // let the server know that we have the client mod installed
        //#if MC >= 12001
        PolymerClientNetworking.setClientMetadata(Mod.HANDSHAKE_PACKET_ID, NbtInt.of(2))
        //#endif

        // receive config updates from server
        ClientPlayNetworking.registerGlobalReceiver(Mod.CONFIG_SYNC_PACKET_ID) { _, _, buf, _ ->
            Mod.LOGGER.info("Received world config from server")
            val config = buf.readNbt()
            if (config == null) {
                Mod.LOGGER.warn("Received server config is null")
                return@registerGlobalReceiver
            }
            for (option in config.keys) {
                val nbtValue = config.get(option)!!
                var value = nbtValue.asString()
                if (nbtValue is NbtByte) {
                    value = (nbtValue.byteValue() != 0.toByte()).toString()
                }
                try {
                    WorldConfig.setOption(option, value)
                } catch (e: InvalidOptionValueException) {
                    Mod.LOGGER.error("Received server config value for '$option' is invalid: ${e.message}")
                }
            }
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> WorldConfig.detachServer() }

        // screens
        ScreenHandlerTypes.SHULKER_LIST.forEach { HandledScreens.register(it, ::BigAugmentedScreen) }
        ScreenHandlerTypes.GENERIC_LIST.forEach { HandledScreens.register(it, ::BigAugmentedScreen) }
        ScreenHandlerTypes.GENERIC_DOUBLE_MAP.values.forEach { HandledScreens.register(it, ::BigAugmentedScreen) }

        BlockEntityRendererFactories.register(BlockEntityType.BARREL, ::BarrelBlockEntityRenderer)
    }
}
