package de.rubixdev.enchanted_shulkers;

import com.chocohead.mm.api.ClassTinkerers;
import de.rubixdev.enchanted_shulkers.enchantment.RefillEnchantment;
import de.rubixdev.enchanted_shulkers.enchantment.SiphonEnchantment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod implements ModInitializer {
    public static final String MOD_ID = "enchanted_shulkers";
    public static final String MOD_NAME;
    public static final Version MOD_VERSION;

    static {
        ModMetadata metadata = FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow(RuntimeException::new)
                .getMetadata();
        MOD_NAME = metadata.getName();
        MOD_VERSION = metadata.getVersion();
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final TagKey<Item> PORTABLE_CONTAINER_TAG =
            TagKey.of(Registry.ITEM_KEY, new Identifier(MOD_ID, "portable_container"));
    public static final EnchantmentTarget PORTABLE_CONTAINER_TARGET =
            ClassTinkerers.getEnum(EnchantmentTarget.class, "PORTABLE_CONTAINER");

    public static final SiphonEnchantment SIPHON_ENCHANTMENT = new SiphonEnchantment();
    public static final RefillEnchantment REFILL_ENCHANTMENT = new RefillEnchantment();

    @Override
    public void onInitialize() {
        Registry.register(Registry.ENCHANTMENT, new Identifier(MOD_ID, "siphon"), SIPHON_ENCHANTMENT);
        Registry.register(Registry.ENCHANTMENT, new Identifier(MOD_ID, "refill"), REFILL_ENCHANTMENT);

        LOGGER.info(MOD_NAME + " v" + MOD_VERSION.getFriendlyString() + " loaded");
    }
}
