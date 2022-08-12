package de.rubixdev.enchantedshulkers.config;

import de.rubixdev.enchantedshulkers.Mod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@Environment(EnvType.CLIENT)
public class ClientConfig {
    private static Inner inner;

    public static void init() {
        AutoConfig.register(ClientConfig.Inner.class, Toml4jConfigSerializer::new);
        inner = AutoConfig.getConfigHolder(ClientConfig.Inner.class).getConfig();
    }

    public static boolean glintWhenPlaced() {
        return inner.glintWhenPlaced;
    }

    public static boolean customModels() {
        return inner.glintWhenPlaced && inner.customModels;
    }

    @Config(name = Mod.MOD_ID)
    public static class Inner implements ConfigData {
        @ConfigEntry.Gui.Tooltip(count = 2)
        boolean glintWhenPlaced = true;

        @ConfigEntry.Gui.Tooltip(count = 5)
        boolean customModels = true;
    }
}
