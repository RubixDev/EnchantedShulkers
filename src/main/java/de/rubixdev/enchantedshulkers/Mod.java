package de.rubixdev.enchantedshulkers;

import com.chocohead.mm.api.ClassTinkerers;
import de.rubixdev.enchantedshulkers.config.ConfigCommand;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.enchantment.RefillEnchantment;
import de.rubixdev.enchantedshulkers.enchantment.SiphonEnchantment;
import de.rubixdev.enchantedshulkers.interfaces.InventoryState;
import java.util.Arrays;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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

    public static final TagKey<Item> PORTABLE_CONTAINER_TAG =
            TagKey.of(Registry.ITEM_KEY, new Identifier(MOD_ID, "portable_container"));
    public static final EnchantmentTarget PORTABLE_CONTAINER_TARGET =
            ClassTinkerers.getEnum(EnchantmentTarget.class, "PORTABLE_CONTAINER");

    public static final SiphonEnchantment SIPHON_ENCHANTMENT = new SiphonEnchantment();
    public static final RefillEnchantment REFILL_ENCHANTMENT = new RefillEnchantment();

    public static final Identifier INVENTORY_OPEN_PACKET_ID = new Identifier(MOD_ID, "inventory_open");
    public static final Identifier INVENTORY_CLOSE_PACKET_ID = new Identifier(MOD_ID, "inventory_close");

    @Override
    public void onInitialize() {
        Registry.register(Registry.ENCHANTMENT, new Identifier(MOD_ID, "siphon"), SIPHON_ENCHANTMENT);
        Registry.register(Registry.ENCHANTMENT, new Identifier(MOD_ID, "refill"), REFILL_ENCHANTMENT);

        // Add enchantments to DECORATIONS ItemGroup
        EnchantmentTarget[] currentTargets = ItemGroup.DECORATIONS.getEnchantments();
        EnchantmentTarget[] newTargets = Arrays.copyOf(currentTargets, currentTargets.length + 1);
        newTargets[newTargets.length - 1] = PORTABLE_CONTAINER_TARGET;
        ItemGroup.DECORATIONS.setEnchantments(newTargets);

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
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ConfigCommand.register(dispatcher));

        // Register packet listeners
        ServerPlayNetworking.registerGlobalReceiver(
                INVENTORY_OPEN_PACKET_ID,
                (server, player, handler, buf, responseSender) -> ((InventoryState) player).setOpen());
        ServerPlayNetworking.registerGlobalReceiver(
                INVENTORY_CLOSE_PACKET_ID,
                (server, player, handler, buf, responseSender) -> ((InventoryState) player).setClosed());

        LOGGER.info(MOD_NAME + " v" + MOD_VERSION.getFriendlyString() + " loaded");
    }
}
