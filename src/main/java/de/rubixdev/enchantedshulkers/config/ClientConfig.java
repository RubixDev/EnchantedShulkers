package de.rubixdev.enchantedshulkers.config;

import de.rubixdev.enchantedshulkers.Mod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientConfig {
    private static Inner inner;

    public static void init() {
        AutoConfig.register(ClientConfig.Inner.class, Toml4jConfigSerializer::new);
        inner = AutoConfig.getConfigHolder(ClientConfig.Inner.class).getConfig();
    }

    public static Inner getInner() {
        return inner;
    }

    @Config(name = Mod.MOD_ID)
    public static class Inner implements ConfigData {
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean glintWhenPlaced = true;

        @ConfigEntry.Gui.Tooltip(count = 5)
        public boolean customModels = true;

        public boolean refillInInventory = false;
    }
}
