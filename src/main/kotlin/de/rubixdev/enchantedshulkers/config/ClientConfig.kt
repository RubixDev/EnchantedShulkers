package de.rubixdev.enchantedshulkers.config

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader

object ClientConfig {
    private val canViewConfig = FabricLoader.getInstance().let { it.environmentType == EnvType.CLIENT && it.isModLoaded("cloth-config") }

    const val DEFAULT_GLINT_WHEN_PLACED = true
    const val DEFAULT_CUSTOM_MODELS = true
    const val DEFAULT_UNOBTRUSIVE_GLINT = false
    const val DEFAULT_REFILL_IN_INVENTORY = false
    const val DEFAULT_SCROLL_SCREEN = false
    const val DEFAULT_SCROLL_SCREEN_ROWS = 6

    fun init() {
        if (!canViewConfig) return
        ClientConfigImpl.init()
    }

    @JvmStatic
    @get:JvmName("glintWhenPlaced")
    val glintWhenPlaced: Boolean get() = if (!canViewConfig) DEFAULT_GLINT_WHEN_PLACED else ClientConfigImpl.inner.glintWhenPlaced
    @JvmStatic
    @get:JvmName("customModels")
    val customModels: Boolean get() = if (!canViewConfig) DEFAULT_CUSTOM_MODELS else glintWhenPlaced && ClientConfigImpl.inner.customModels
    @JvmStatic
    @get:JvmName("unobtrusiveGlint")
    val unobtrusiveGlint: Boolean get() = if (!canViewConfig) DEFAULT_UNOBTRUSIVE_GLINT else ClientConfigImpl.inner.unobtrusiveGlint
    @JvmStatic
    @get:JvmName("refillInInventory")
    val refillInInventory: Boolean get() = if (!canViewConfig) DEFAULT_REFILL_IN_INVENTORY else ClientConfigImpl.inner.refillInInventory
    @JvmStatic
    @get:JvmName("scrollScreen")
    val scrollScreen: Boolean get() = if (!canViewConfig) DEFAULT_SCROLL_SCREEN else ClientConfigImpl.inner.scrollScreen
    @JvmStatic
    @get:JvmName("scrollScreenRows")
    val scrollScreenRows: Int get() = if (!canViewConfig) DEFAULT_SCROLL_SCREEN_ROWS else ClientConfigImpl.inner.scrollScreenRows
}
