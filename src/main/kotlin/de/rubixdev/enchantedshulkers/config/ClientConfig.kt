package de.rubixdev.enchantedshulkers.config

import de.rubixdev.enchantedshulkers.Mod
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object ClientConfig {
    lateinit var inner: Inner
        private set

    fun init() {
        AutoConfig.register(Inner::class.java, ::Toml4jConfigSerializer)
        inner = AutoConfig.getConfigHolder(Inner::class.java).config
    }

    @Config(name = Mod.MOD_ID)
    class Inner : ConfigData {
        @ConfigEntry.Gui.Tooltip(count = 2)
        var glintWhenPlaced = true
            private set

        @ConfigEntry.Gui.Tooltip(count = 5)
        var customModels = true
            private set

        var refillInInventory = false
            private set
    }
}
