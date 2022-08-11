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
@Config(name = Mod.MOD_ID)
@Environment(EnvType.CLIENT)
public class ClientConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip(count = 2)
    boolean glintWhenPlaced = true;

    @ConfigEntry.Gui.Tooltip(count = 5)
    boolean customModels = true;

    // TODO: move colorizeContainerNames to client side (as coloredNames)

    @ConfigEntry.Gui.Excluded
    private static ClientConfig instance;

    public static void init() {
        AutoConfig.register(ClientConfig.class, Toml4jConfigSerializer::new);
        instance = AutoConfig.getConfigHolder(ClientConfig.class).getConfig();
    }

    public static ClientConfig get() {
        return instance;
    }

    public boolean glintWhenPlaced() {
        return this.glintWhenPlaced;
    }

    public boolean customModels() {
        return this.glintWhenPlaced && this.customModels;
    }
}
