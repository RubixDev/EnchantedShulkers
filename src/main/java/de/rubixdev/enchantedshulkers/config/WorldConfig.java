package de.rubixdev.enchantedshulkers.config;

import static java.nio.file.StandardOpenOption.*;

import com.google.common.collect.Lists;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import de.rubixdev.enchantedshulkers.Mod;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class WorldConfig {
    private static MinecraftServer server;
    private static Inner inner = new Inner();

    public static void attachServer(MinecraftServer server) {
        WorldConfig.server = server;
        WorldConfig.read();
        updateResources();
    }

    public static void detachServer() {
        WorldConfig.server = null;
        WorldConfig.inner = new Inner();
    }

    public static Stream<String> getOptions() {
        return Arrays.stream(Inner.class.getDeclaredFields()).map(Field::getName);
    }

    public static boolean getOption(String option) {
        Field optionField = getField(option);
        try {
            return optionField.getBoolean(inner);
        } catch (IllegalAccessException e) {
            // shouldn't fail after `setAccessible(true)` call
            throw new RuntimeException(e);
        }
    }

    public static boolean getOptionDefault(String option) {
        Field optionField = getField(option);
        try {
            return optionField.getBoolean(new Inner());
        } catch (IllegalAccessException e) {
            // shouldn't fail after `setAccessible(true)` call
            throw new RuntimeException(e);
        }
    }

    public static void setOption(String option, boolean value) {
        Field optionField = getField(option);
        try {
            if (optionField.getBoolean(inner) == value) return;
            optionField.set(inner, value);
            updateResources();
        } catch (IllegalAccessException e) {
            // shouldn't fail after `setAccessible(true)`
            throw new RuntimeException(e);
        }
        write();
    }

    private static Field getField(String name) {
        try {
            Field field =  Inner.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String ENCHANTED_ENDER_CHEST_ID;

    static {
        // Fabric API 0.95.4 and later use a different resource pack naming scheme
        try {
            ENCHANTED_ENDER_CHEST_ID = FabricLoader.getInstance().getModContainer("fabric-api").orElseThrow(RuntimeException::new).getMetadata()
                    .getVersion().compareTo(Version.parse("0.95.4")) >= 0
                ? "enchantedshulkers:enchanted_ender_chest_resourcepacks" + File.separator + "enchanted_ender_chest"
                : "enchantedshulkers:enchanted_ender_chest";
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateResources() {
        ResourcePackManager manager = server.getDataPackManager();
        boolean isEnderPackLoaded = manager.getEnabledNames().contains(ENCHANTED_ENDER_CHEST_ID);
        if (isEnderPackLoaded == inner.enchantableEnderChest) return;


        ArrayList<ResourcePackProfile> loadedPacks =
                Lists.newArrayList(server.getDataPackManager().getEnabledProfiles());
        ResourcePackProfile enderPackProfile = manager.getProfile(ENCHANTED_ENDER_CHEST_ID);
        assert enderPackProfile != null;
        if (inner.enchantableEnderChest) {
            enderPackProfile.getInitialPosition().insert(loadedPacks, enderPackProfile, profile -> profile, false);
        } else {
            loadedPacks.remove(enderPackProfile);
        }
        server.reloadResources(
                        loadedPacks.stream().map(ResourcePackProfile::getName).toList())
                .exceptionally(throwable -> {
                    Mod.LOGGER.warn("Failed to execute reload", throwable);
                    return null;
                });
    }

    private static Path getConfigPath() {
        return server.getSavePath(WorldSavePath.ROOT).resolve(Mod.MOD_ID + ".toml");
    }

    private static void read() {
        try (BufferedReader reader = Files.newBufferedReader(getConfigPath())) {
            inner = new Toml().read(reader).to(Inner.class);
            Mod.LOGGER.info("Loaded settings from " + Mod.MOD_ID + ".toml");
        } catch (Throwable e) {
            Mod.LOGGER.warn("Could not read config, using default settings: " + e);
        }
    }

    private static void write() {
        try (BufferedWriter writer = Files.newBufferedWriter(getConfigPath(), WRITE, TRUNCATE_EXISTING, CREATE)) {
            new TomlWriter().write(inner, writer);
        } catch (Throwable e) {
            Mod.LOGGER.error("Could not write config: " + e);
        }
    }

    public static boolean refillOffhand() {
        return inner.refillOffhand;
    }

    public static boolean refillNonStackables() {
        return inner.refillNonStackables;
    }

    public static boolean coloredNames() {
        return inner.coloredNames;
    }

    public static boolean creativeSiphon() {
        return inner.creativeSiphon;
    }

    public static boolean creativeRefill() {
        return inner.creativeRefill;
    }

    public static boolean creativeVacuum() {
        return inner.creativeVacuum;
    }

    public static boolean creativeVoid() {
        return inner.creativeVoid;
    }

    public static boolean generateRefill() {
        return inner.generateRefill;
    }

    public static boolean generateSiphon() {
        return inner.generateSiphon;
    }

    public static boolean generateVacuum() {
        return inner.generateVacuum;
    }

    public static boolean generateVoid() {
        return inner.generateVoid;
    }

    public static boolean nestedContainers() {
        return inner.nestedContainers;
    }

    public static boolean strongerSiphon() {
        return inner.strongerSiphon;
    }

    public static boolean weakerVacuum() {
        return inner.weakerVacuum;
    }

    private static class Inner {
        boolean refillOffhand = true;
        boolean refillNonStackables = false;
        boolean enchantableEnderChest = false;
        boolean coloredNames = false;
        boolean creativeSiphon = false;
        boolean creativeRefill = false;
        boolean creativeVacuum = false;
        boolean creativeVoid = false;
        boolean generateRefill = true;
        boolean generateSiphon = true;
        boolean generateVacuum = false;
        boolean generateVoid = false;
        boolean nestedContainers = true;
        boolean strongerSiphon = false;
        boolean weakerVacuum = false;
    }
}
