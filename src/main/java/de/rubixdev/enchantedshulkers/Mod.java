package de.rubixdev.enchantedshulkers;

import com.chocohead.mm.api.ClassTinkerers;
import de.rubixdev.enchantedshulkers.config.ConfigCommand;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.enchantment.RefillEnchantment;
import de.rubixdev.enchantedshulkers.enchantment.SiphonEnchantment;
import de.rubixdev.enchantedshulkers.interfaces.HasClientMod;
import de.rubixdev.enchantedshulkers.interfaces.InventoryState;
import net.fabricmc.api.ModInitializer;
//#if MC >= 11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#endif
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.registry.Registry;
//#if MC >= 11900
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
//#else
//$$ import net.minecraft.item.ItemGroup;
//$$ import java.util.Arrays;
//#endif
//#if MC >= 11802
import net.minecraft.registry.tag.TagKey;
//#else
//$$ import net.minecraft.tag.Tag;
//#endif
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    //#if MC >= 11802
    public static final TagKey<Item> PORTABLE_CONTAINER_TAG =
            TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "portable_container"));
    //#else
    //$$ public static final Tag<Item> PORTABLE_CONTAINER_TAG =
    //$$     net.fabricmc.fabric.api.tag.TagFactory.ITEM.create(new Identifier(MOD_ID, "portable_container"));
    //#endif

    public static final EnchantmentTarget PORTABLE_CONTAINER_TARGET =
            ClassTinkerers.getEnum(EnchantmentTarget.class, "PORTABLE_CONTAINER");

    public static final SiphonEnchantment SIPHON_ENCHANTMENT = new SiphonEnchantment();
    public static final RefillEnchantment REFILL_ENCHANTMENT = new RefillEnchantment();

    public static final Identifier CLIENT_INSTALLED_PACKET_ID = new Identifier(MOD_ID, "client_installed");
    public static final Identifier INVENTORY_OPEN_PACKET_ID = new Identifier(MOD_ID, "inventory_open");
    public static final Identifier INVENTORY_CLOSE_PACKET_ID = new Identifier(MOD_ID, "inventory_close");

    //#if MC < 11800
    //$$ // some "random" numbers (must be bytes) that hopefully no other mods use
    //$$ // to identify block entities over packets
    //$$ public static final int BLOCK_ENTITY_TYPE_SHULKER_BOX = 23;
    //$$ public static final int BLOCK_ENTITY_TYPE_ENDER_CHEST = 24;
    //#endif

    @Override
    public void onInitialize() {
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "siphon"), SIPHON_ENCHANTMENT);
        Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "refill"), REFILL_ENCHANTMENT);

        //#if MC < 11900
        //$$ // Add enchantments to DECORATIONS ItemGroup
        //$$ EnchantmentTarget[] currentTargets = ItemGroup.DECORATIONS.getEnchantments();
        //$$ EnchantmentTarget[] newTargets = Arrays.copyOf(currentTargets, currentTargets.length + 1);
        //$$ newTargets[newTargets.length - 1] = PORTABLE_CONTAINER_TARGET;
        //$$ ItemGroup.DECORATIONS.setEnchantments(newTargets);
        //#endif

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
        CommandRegistrationCallback.EVENT.register(
                (
                        dispatcher,
                        registryAccess
                        //#if MC >= 11900
                        ,environment
                        //#endif
                ) -> ConfigCommand.register(dispatcher));

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
