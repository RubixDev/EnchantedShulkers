package de.rubixdev.enchantedshulkers.config

import de.rubixdev.enchantedshulkers.Mod
import java.lang.reflect.Field
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object ClientConfigImpl {
    val inner: Inner

    init {
        AutoConfig.register(Inner::class.java, ::Toml4jConfigSerializer)
        inner = AutoConfig.getConfigHolder(Inner::class.java).config
    }

    fun init() {
        val registry = AutoConfig.getGuiRegistry(Inner::class.java)
        var scrollScreen: BooleanListEntry? = null
        registry.registerPredicateTransformer(
            { guis, _, _, _, _, _ ->
                scrollScreen = guis.firstNotNullOfOrNull { it as? BooleanListEntry }
                guis
            },
            isField("scrollScreen"),
        )
        registry.registerPredicateTransformer(
            { guis, _, _, _, _, _ ->
                @Suppress("UnstableApiUsage")
                guis.forEach { it.setRequirement { scrollScreen?.value ?: true } }
                guis
            },
            isField("scrollScreenRows"),
        )
    }

    private fun isField(name: String) = { field: Field -> field.declaringClass == Inner::class.java && field.name == name }

    @Config(name = Mod.MOD_ID)
    class Inner : ConfigData {
        @ConfigEntry.Gui.Tooltip(count = 2)
        var glintWhenPlaced = ClientConfig.DEFAULT_GLINT_WHEN_PLACED
            private set

        @ConfigEntry.Gui.Tooltip(count = 5)
        var customModels = ClientConfig.DEFAULT_CUSTOM_MODELS
            private set

        var unobtrusiveGlint = ClientConfig.DEFAULT_UNOBTRUSIVE_GLINT
            private set

        var refillInInventory = ClientConfig.DEFAULT_REFILL_IN_INVENTORY
            private set

        var scrollScreen = ClientConfig.DEFAULT_SCROLL_SCREEN
            private set

        @ConfigEntry.BoundedDiscrete(min = 6, max = 9)
        var scrollScreenRows = ClientConfig.DEFAULT_SCROLL_SCREEN_ROWS
            private set
    }
}
