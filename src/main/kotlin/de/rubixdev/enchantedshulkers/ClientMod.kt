package de.rubixdev.enchantedshulkers

import de.rubixdev.enchantedshulkers.config.ClientConfig
import de.rubixdev.enchantedshulkers.config.InvalidOptionValueException
import de.rubixdev.enchantedshulkers.config.WorldConfig
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.model.*
import net.minecraft.client.render.TexturedRenderLayers
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.nbt.NbtByte
import net.minecraft.util.DyeColor

//#if MC >= 12001
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking
import net.minecraft.nbt.NbtInt
//#endif

@Environment(EnvType.CLIENT)
object ClientMod : ClientModInitializer {
    @JvmField val CLOSED_ENDER_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, Mod.id("entity/chest/closed_ender"))
    @JvmField val COLORS = DyeColor.values().sortedBy { it.id }.map { it.getName() }
    @JvmField val CLOSED_SHULKER_TEXTURE_ID = SpriteIdentifier(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, Mod.id("entity/shulker/closed_shulker"))
    @JvmField val CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS = COLORS.map { SpriteIdentifier(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, Mod.id("entity/shulker/closed_shulker_$it")) }
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

    private var hasCloth = false

    override fun onInitializeClient() {
        // let the server know that we have the client mod installed
        //#if MC >= 12001
        PolymerClientNetworking.setClientMetadata(Mod.HANDSHAKE_PACKET_ID, NbtInt.of(1))
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

        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) return
        hasCloth = true
        ClientConfig.init()
    }

    @JvmStatic
    @get:JvmName("glintWhenPlaced")
    val glintWhenPlaced: Boolean get() = if (!hasCloth) true else ClientConfig.inner.glintWhenPlaced
    @JvmStatic
    @get:JvmName("customModels")
    val customModels: Boolean get() = if (!hasCloth) true else glintWhenPlaced && ClientConfig.inner.customModels
    @JvmStatic
    @get:JvmName("refillInInventory")
    val refillInInventory: Boolean get() = if (!hasCloth) false else ClientConfig.inner.refillInInventory
}
