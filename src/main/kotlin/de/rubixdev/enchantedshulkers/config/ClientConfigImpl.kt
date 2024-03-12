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
object ClientConfigImpl {
    val inner: Inner

    init {
        AutoConfig.register(Inner::class.java, ::Toml4jConfigSerializer)
        inner = AutoConfig.getConfigHolder(Inner::class.java).config
    }

    fun init() {}

    @Config(name = Mod.MOD_ID)
    class Inner : ConfigData {
        @ConfigEntry.Gui.Tooltip(count = 2)
        var glintWhenPlaced = ClientConfig.DEFAULT_GLINT_WHEN_PLACED
            private set

        @ConfigEntry.Gui.Tooltip(count = 5)
        var customModels = ClientConfig.DEFAULT_CUSTOM_MODELS
            private set

        var refillInInventory = ClientConfig.DEFAULT_REFILL_IN_INVENTORY
            private set

        var scrollScreen = ClientConfig.DEFAULT_SCROLL_SCREEN
            private set

        // TODO: disable when `scrollScreen` is false
        @ConfigEntry.BoundedDiscrete(min = 6, max = 9)
        var scrollScreenRows = ClientConfig.DEFAULT_SCROLL_SCREEN_ROWS
            private set
    }
}
