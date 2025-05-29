package de.rubixdev.enchantedshulkers

import de.rubixdev.enchantedshulkers.Utils.id
import de.rubixdev.enchantedshulkers.config.ClientConfig
import de.rubixdev.enchantedshulkers.config.ConfigCommand
import de.rubixdev.enchantedshulkers.config.WorldConfig
import de.rubixdev.enchantedshulkers.enchantment.*
import de.rubixdev.enchantedshulkers.interfaces.InventoryState
import de.rubixdev.enchantedshulkers.screen.ScreenHandlerTypes
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//#if MC < 12001
//$$ import eu.pb4.polymer.networking.api.PolymerServerNetworking
//#endif

//#if MC >= 12006
import de.rubixdev.enchantedshulkers.network.ConfigSyncS2CPacket
import de.rubixdev.enchantedshulkers.network.InventoryCloseC2SPacket
import de.rubixdev.enchantedshulkers.network.InventoryOpenC2SPacket
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
//#else
//$$ import com.chocohead.mm.api.ClassTinkerers
//$$ import net.minecraft.enchantment.EnchantmentTarget
//#endif

object Mod : ModInitializer {
    const val MOD_ID = "enchantedshulkers"
    private val MOD_NAME: String
    private val MOD_VERSION: Version

    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    init {
        val metadata = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .orElseThrow(::RuntimeException)
            .metadata
        MOD_NAME = metadata.name
        MOD_VERSION = metadata.version
    }

    @JvmField val PORTABLE_CONTAINER_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, "portable_container".id)
    @JvmField val AUGMENTABLE_CONTAINER_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, "augmentable_container".id)

    //#if MC < 12006
    //$$ @JvmField val PORTABLE_CONTAINER_TARGET: EnchantmentTarget = ClassTinkerers.getEnum(
    //$$     EnchantmentTarget::class.java,
    //$$     "PORTABLE_CONTAINER",
    //$$ )
    //$$ @JvmField val AUGMENTABLE_CONTAINER_TARGET: EnchantmentTarget = ClassTinkerers.getEnum(
    //$$     EnchantmentTarget::class.java,
    //$$     "AUGMENTABLE_CONTAINER",
    //$$ )
    //#endif

    @JvmField val SIPHON_ENCHANTMENT = SiphonEnchantment()
    @JvmField val REFILL_ENCHANTMENT = RefillEnchantment()
    @JvmField val VACUUM_ENCHANTMENT = VacuumEnchantment()
    @JvmField val VOID_ENCHANTMENT = VoidEnchantment()
    @JvmField val AUGMENT_ENCHANTMENT = AugmentEnchantment()

    @JvmField val HANDSHAKE_PACKET_ID = "handshake".id
    //#if MC < 12006
    //$$ @JvmField val CONFIG_SYNC_PACKET_ID = "config_sync".id
    //$$ @JvmField val INVENTORY_OPEN_PACKET_ID = "inventory_open".id
    //$$ @JvmField val INVENTORY_CLOSE_PACKET_ID = "inventory_close".id
    //#endif

    override fun onInitialize() {
        // register enchantments
        Registry.register(Registries.ENCHANTMENT, "siphon".id, SIPHON_ENCHANTMENT)
        Registry.register(Registries.ENCHANTMENT, "refill".id, REFILL_ENCHANTMENT)
        Registry.register(Registries.ENCHANTMENT, "vacuum".id, VACUUM_ENCHANTMENT)
        Registry.register(Registries.ENCHANTMENT, "void".id, VOID_ENCHANTMENT)
        Registry.register(Registries.ENCHANTMENT, "augment".id, AUGMENT_ENCHANTMENT)

        // register packets
        //#if MC >= 12006
        PayloadTypeRegistry.playS2C().register(ConfigSyncS2CPacket.ID, ConfigSyncS2CPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(InventoryOpenC2SPacket.ID, InventoryOpenC2SPacket.CODEC)
        PayloadTypeRegistry.playC2S().register(InventoryCloseC2SPacket.ID, InventoryCloseC2SPacket.CODEC)
        //#endif

        // handshake with modded clients
        //#if MC < 12001
        //$$ PolymerServerNetworking.registerSendPacket(HANDSHAKE_PACKET_ID, 2)
        //#endif

        // register optional data packs
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent {
            WorldConfig.OPTIONAL_PACKS.forEach { (name, _) ->
                ResourceManagerHelper.registerBuiltinResourcePack(name.id, it, ResourcePackActivationType.NORMAL)
            }
        }

        // register event hooks and command
        ServerLifecycleEvents.SERVER_STARTING.register(WorldConfig::attachServer)
        ServerLifecycleEvents.SERVER_STOPPED.register { WorldConfig.detachServer() }
        // we can't use ServerPlayConnectionEvents.JOIN here because Polymer's handshake comes after that in versions before 1.20.2
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register { player, _ -> WorldConfig.sendConfigToClient(player) }
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> ConfigCommand.register(dispatcher) }

        // register packet listeners
        //#if MC >= 12006
        ServerPlayNetworking.registerGlobalReceiver(InventoryOpenC2SPacket.ID) { _, ctx ->
            (ctx.player() as InventoryState).`enchantedShulkers$setOpen`()
        }
        ServerPlayNetworking.registerGlobalReceiver(InventoryCloseC2SPacket.ID) { _, ctx ->
            (ctx.player() as InventoryState).`enchantedShulkers$setClosed`()
        }
        //#else
        //$$ ServerPlayNetworking.registerGlobalReceiver(INVENTORY_OPEN_PACKET_ID) { _, player, _, _, _ ->
        //$$     (player as InventoryState).`enchantedShulkers$setOpen`()
        //$$ }
        //$$ ServerPlayNetworking.registerGlobalReceiver(INVENTORY_CLOSE_PACKET_ID) { _, player, _, _, _ ->
        //$$     (player as InventoryState).`enchantedShulkers$setClosed`()
        //$$ }
        //#endif

        // register screen types
        ScreenHandlerTypes.init()

        // initialize client config
        ClientConfig.init()

        LOGGER.info("$MOD_NAME v${MOD_VERSION.friendlyString} loaded")
    }
}
