package de.rubixdev.enchantedshulkers

import com.chocohead.mm.api.ClassTinkerers
import de.rubixdev.enchantedshulkers.config.ConfigCommand
import de.rubixdev.enchantedshulkers.config.WorldConfig
import de.rubixdev.enchantedshulkers.enchantment.*
import de.rubixdev.enchantedshulkers.interfaces.InventoryState
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//#if MC < 12001
//$$ import eu.pb4.polymer.networking.api.PolymerServerNetworking
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

    @JvmField val PORTABLE_CONTAINER_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, id("portable_container"))
    @JvmField val AUGMENTABLE_CONTAINER_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, id("augmentable_container"))

    @JvmField val PORTABLE_CONTAINER_TARGET: EnchantmentTarget = ClassTinkerers.getEnum(
        EnchantmentTarget::class.java,
        "PORTABLE_CONTAINER",
    )
    @JvmField val AUGMENTABLE_CONTAINER_TARGET: EnchantmentTarget = ClassTinkerers.getEnum(
        EnchantmentTarget::class.java,
        "AUGMENTABLE_CONTAINER",
    )

    @JvmField val SIPHON_ENCHANTMENT = SiphonEnchantment()
    @JvmField val REFILL_ENCHANTMENT = RefillEnchantment()
    @JvmField val VACUUM_ENCHANTMENT = VacuumEnchantment()
    @JvmField val VOID_ENCHANTMENT = VoidEnchantment()
    @JvmField val AUGMENT_ENCHANTMENT = AugmentEnchantment()

    @JvmField val HANDSHAKE_PACKET_ID = id("handshake")
    @JvmField val CONFIG_SYNC_PACKET_ID = id("config_sync")
    @JvmField val INVENTORY_OPEN_PACKET_ID = id("inventory_open")
    @JvmField val INVENTORY_CLOSE_PACKET_ID = id("inventory_close")

    fun id(path: String): Identifier = Identifier(MOD_ID, path)

    override fun onInitialize() {
        Registry.register(Registries.ENCHANTMENT, id("siphon"), SIPHON_ENCHANTMENT)
        Registry.register(Registries.ENCHANTMENT, id("refill"), REFILL_ENCHANTMENT)
        Registry.register(Registries.ENCHANTMENT, id("vacuum"), VACUUM_ENCHANTMENT)
        Registry.register(Registries.ENCHANTMENT, id("void"), VOID_ENCHANTMENT)
        Registry.register(Registries.ENCHANTMENT, id("augment"), AUGMENT_ENCHANTMENT)

        // handshake with modded clients
        //#if MC < 12001
        //$$ PolymerServerNetworking.registerSendPacket(HANDSHAKE_PACKET_ID, 1)
        //#endif

        // add enchanted_ender_chest data pack when enabled in config
        FabricLoader.getInstance().getModContainer(MOD_ID).ifPresent {
            ResourceManagerHelper.registerBuiltinResourcePack(
                id("enchanted_ender_chest"),
                it,
                ResourcePackActivationType.NORMAL,
            )
        }

        // register event hooks and command
        ServerLifecycleEvents.SERVER_STARTING.register(WorldConfig::attachServer)
        ServerLifecycleEvents.SERVER_STOPPED.register { WorldConfig.detachServer() }
        // we can't use ServerPlayConnectionEvents.JOIN here because Polymer's handshake comes after that in versions before 1.20.2
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register { player, _ -> WorldConfig.sendConfigToClient(player) }
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> ConfigCommand.register(dispatcher) }

        // register packet listeners
        ServerPlayNetworking.registerGlobalReceiver(INVENTORY_OPEN_PACKET_ID) { _, player, _, _, _ ->
            (player as InventoryState).`enchantedShulkers$setOpen`()
        }
        ServerPlayNetworking.registerGlobalReceiver(INVENTORY_CLOSE_PACKET_ID) { _, player, _, _, _ ->
            (player as InventoryState).`enchantedShulkers$setClosed`()
        }

        LOGGER.info("$MOD_NAME v${MOD_VERSION.friendlyString} loaded")
    }
}
