package de.rubixdev.enchanted_shulkers;

import static java.nio.file.StandardOpenOption.*;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class Config {
    public static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve(Mod.MOD_ID + ".toml");
    private static InnerConfig inner = new InnerConfig();

    static {
        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            inner = new Toml().read(reader).to(InnerConfig.class);
        } catch (Throwable e) {
            Mod.LOGGER.warn("Could not read config, using default settings: " + e);
        }

        if (!Files.exists(CONFIG_PATH)) {
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH, WRITE, TRUNCATE_EXISTING, CREATE)) {
                new TomlWriter().write(inner, writer);
            } catch (Throwable e) {
                Mod.LOGGER.error("Could not write config: " + e);
            }
        }
    }

    public static boolean refillOffhand() {
        return inner.refill_offhand;
    }

    private static class InnerConfig {
        boolean refill_offhand = true;
    }
}
