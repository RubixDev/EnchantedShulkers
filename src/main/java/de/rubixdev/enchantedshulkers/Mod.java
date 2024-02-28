package de.rubixdev.enchantedshulkers;

import com.chocohead.mm.api.ClassTinkerers;
import de.rubixdev.enchantedshulkers.config.ConfigCommand;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.enchantment.*;
import de.rubixdev.enchantedshulkers.interfaces.InventoryState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//#if MC < 12001
//$$ import eu.pb4.polymer.networking.api.PolymerServerNetworking;
//#endif

public class Mod implements ModInitializer {
    public static final String MOD_ID = "enchantedshulkers";
    public static final String MOD_NAME;
    public static final Version MOD_VERSION;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    static {
        ModMetadata metadata = FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow(RuntimeException::new)
                .getMetadata();
        MOD_NAME = metadata.getName();
        MOD_VERSION = metadata.getVersion();
    }

    public static final TagKey<Item> PORTABLE_CONTAINER_TAG =
            TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "portable_container"));
    public static final TagKey<Item> AUGMENTABLE_CONTAINER_TAG =
            TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "augmentable_container"));

    public static final EnchantmentTarget PORTABLE_CONTAINER_TARGET =
            ClassTinkerers.getEnum(EnchantmentTarget.class, "PORTABLE_CONTAINER");
    public static final EnchantmentTarget AUGMENTABLE_CONTAINER_TARGET =
            ClassTinkerers.getEnum(EnchantmentTarget.class, "AUGMENTABLE_CONTAINER");

    public static final SiphonEnchantment SIPHON_ENCHANTMENT = new SiphonEnchantment();
    public static final RefillEnchantment REFILL_ENCHANTMENT = new RefillEnchantment();
    public static final VacuumEnchantment VACUUM_ENCHANTMENT = new VacuumEnchantment();
    public static final VoidEnchantment VOID_ENCHANTMENT = new VoidEnchantment();
    public static final AugmentEnchantment AUGMENT_ENCHANTMENT = new AugmentEnchantment();

    public static final Identifier HANDSHAKE_PACKET_ID = new Identifier(MOD_ID, "handshake");
    public static final Identifier CONFIG_SYNC_PACKET_ID = new Identifier(MOD_ID, "config_sync");
    public static final Identifier INVENTORY_OPEN_PACKET_ID = new Identifier(MOD_ID, "inventory_open");
    public static final Identifier INVENTORY_CLOSE_PACKET_ID = new Identifier(MOD_ID, "inventory_close");

    @Override
    public void onInitialize() {
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "siphon"), SIPHON_ENCHANTMENT);
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "refill"), REFILL_ENCHANTMENT);
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "vacuum"), VACUUM_ENCHANTMENT);
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "void"), VOID_ENCHANTMENT);
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "augment"), AUGMENT_ENCHANTMENT);

        // handshake with modded clients
        //#if MC < 12001
        //$$ PolymerServerNetworking.registerSendPacket(HANDSHAKE_PACKET_ID, 1);
        //#endif

        // Add enchanted_ender_chest data pack when enabled in config
        FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .ifPresent(modContainer -> ResourceManagerHelper.registerBuiltinResourcePack(
                        new Identifier(MOD_ID, "enchanted_ender_chest"),
                        modContainer,
                        ResourcePackActivationType.NORMAL));

        // Register event hooks and command
        ServerLifecycleEvents.SERVER_STARTING.register(WorldConfig::attachServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> WorldConfig.detachServer());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> WorldConfig.sendConfigToClient(handler.player));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ConfigCommand.register(dispatcher));

        // Register packet listeners
        ServerPlayNetworking.registerGlobalReceiver(
                INVENTORY_OPEN_PACKET_ID,
                (server, player, handler, buf, responseSender) -> ((InventoryState) player).enchantedShulkers$setOpen());
        ServerPlayNetworking.registerGlobalReceiver(
                INVENTORY_CLOSE_PACKET_ID,
                (server, player, handler, buf, responseSender) -> ((InventoryState) player).enchantedShulkers$setClosed());

        LOGGER.info(MOD_NAME + " v" + MOD_VERSION.getFriendlyString() + " loaded");
    }
}
