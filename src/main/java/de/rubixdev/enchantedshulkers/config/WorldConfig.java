package de.rubixdev.enchantedshulkers.config;

import static java.nio.file.StandardOpenOption.*;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import de.rubixdev.enchantedshulkers.Mod;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public class WorldConfig {
    private static MinecraftServer server;
    private static Inner inner = new Inner();

    public static void attachServer(MinecraftServer server) {
        WorldConfig.server = server;
        WorldConfig.read();
    }

    public static void detachServer() {
        WorldConfig.server = null;
        WorldConfig.inner = new Inner();
    }

    public static Stream<String> getOptions() {
        return Arrays.stream(Inner.class.getDeclaredFields()).map(Field::getName);
    }

    public static boolean getOption(String option) throws NoSuchFieldException {
        Field optionField = Inner.class.getDeclaredField(option);
        optionField.setAccessible(true);
        try {
            return optionField.getBoolean(inner);
        } catch (IllegalAccessException e) {
            // unable to fail after `setAccessible(true)` call
            throw new RuntimeException(e);
        }
    }

    public static void setOption(String option, boolean value) throws NoSuchFieldException {
        Field optionField = Inner.class.getDeclaredField(option);
        optionField.setAccessible(true);
        try {
            if (optionField.getBoolean(inner) == value) return;
            optionField.set(inner, value);
        } catch (IllegalAccessException e) {
            // unable to fail after `setAccessible(true)`
            throw new RuntimeException(e);
        }
        write();
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

    public static boolean enchantableEnderChest() {
        return inner.enchantableEnderChest;
    }

    public static boolean colorizeContainerNames() {
        return inner.colorizeContainerNames;
    }

    private static class Inner {
        boolean refillOffhand = true;
        boolean refillNonStackables = false;
        boolean enchantableEnderChest = false;
        boolean colorizeContainerNames = false;
    }
}
