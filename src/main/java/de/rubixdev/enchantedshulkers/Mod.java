package de.rubixdev.enchantedshulkers;

import com.chocohead.mm.api.ClassTinkerers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.rubixdev.enchantedshulkers.config.ConfigCommand;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.enchantment.RefillEnchantment;
import de.rubixdev.enchantedshulkers.enchantment.SiphonEnchantment;
import de.rubixdev.enchantedshulkers.interfaces.HasClientMod;
import de.rubixdev.enchantedshulkers.interfaces.InventoryState;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Mod implements ModInitializer {
    public static final String MOD_ID = "enchantedshulkers";
    public static final String MOD_NAME;
    public static final Version MOD_VERSION;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<String, String> EN_US_TRANSLATIONS;

    static {
        ModMetadata metadata = FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow(RuntimeException::new)
                .getMetadata();
        MOD_NAME = metadata.getName();
        MOD_VERSION = metadata.getVersion();

        // read english translations
        InputStream langFile = Mod.class.getClassLoader().getResourceAsStream("assets/%s/lang/en_us.json".formatted(MOD_ID));
        assert langFile != null;
        Gson gson = new GsonBuilder().setLenient().create();
        EN_US_TRANSLATIONS = gson.fromJson(new InputStreamReader(langFile, StandardCharsets.UTF_8), new TypeToken<>() {}.getType());
    }

    public static final TagKey<Item> PORTABLE_CONTAINER_TAG =
            TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "portable_container"));

    public static final EnchantmentTarget PORTABLE_CONTAINER_TARGET =
            ClassTinkerers.getEnum(EnchantmentTarget.class, "PORTABLE_CONTAINER");

    public static final SiphonEnchantment SIPHON_ENCHANTMENT = new SiphonEnchantment();
    public static final RefillEnchantment REFILL_ENCHANTMENT = new RefillEnchantment();

    public static final Identifier CLIENT_INSTALLED_PACKET_ID = new Identifier(MOD_ID, "client_installed");
    public static final Identifier INVENTORY_OPEN_PACKET_ID = new Identifier(MOD_ID, "inventory_open");
    public static final Identifier INVENTORY_CLOSE_PACKET_ID = new Identifier(MOD_ID, "inventory_close");

    @Override
    public void onInitialize() {
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "siphon"), SIPHON_ENCHANTMENT);
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "refill"), REFILL_ENCHANTMENT);

        // Add enchanted_ender_chest data pack when enabled in config
        FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .ifPresent(modContainer -> ResourceManagerHelper.registerBuiltinResourcePack(
                        new Identifier(MOD_ID, "enchanted_ender_chest"),
                        modContainer,
                        ResourcePackActivationType.NORMAL));

        // Register event hooks and command
        ServerLifecycleEvents.SERVER_STARTED.register(WorldConfig::attachServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> WorldConfig.detachServer());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ConfigCommand.register(dispatcher));

        // Register packet listeners
        ServerPlayNetworking.registerGlobalReceiver(
                CLIENT_INSTALLED_PACKET_ID,
                (server, player, handler, buf, responseSender) -> ((HasClientMod) player).enchantedShulkers$setTrue());
        ServerPlayNetworking.registerGlobalReceiver(
                CustomPayloadC2SPacket.BRAND,
                // at this point a modded client would have sent a `CLIENT_INSTALLED_PACKET_ID` packet
                (server, player, handler, buf, responseSender) -> ((HasClientMod) player).enchantedShulkers$submit());
        ServerPlayNetworking.registerGlobalReceiver(
                INVENTORY_OPEN_PACKET_ID,
                (server, player, handler, buf, responseSender) -> ((InventoryState) player).enchantedShulkers$setOpen());
        ServerPlayNetworking.registerGlobalReceiver(
                INVENTORY_CLOSE_PACKET_ID,
                (server, player, handler, buf, responseSender) -> ((InventoryState) player).enchantedShulkers$setClosed());

        LOGGER.info(MOD_NAME + " v" + MOD_VERSION.getFriendlyString() + " loaded");
    }
}
