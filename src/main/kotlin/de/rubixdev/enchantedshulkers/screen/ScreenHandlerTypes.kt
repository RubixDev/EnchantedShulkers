package de.rubixdev.enchantedshulkers.screen

import de.rubixdev.enchantedshulkers.Utils.id
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.screen.ScreenHandlerType

object ScreenHandlerTypes {
    val SHULKER_LIST = (4..10).map { level -> register(level, null, isShulkerBox = true) }
    val GENERIC_LIST = (4..10).map { level -> register(level, null, isShulkerBox = false) }
    val GENERIC_DOUBLE_MAP = (4..10).flatMap { level1 ->
        (4..10).map { level2 -> Pair(level1, level2) to register(level1, level2, isShulkerBox = false) }
    }.toMap()

    fun init() {}

    private fun register(augmentLevel: Int, augmentLevel2: Int?, isShulkerBox: Boolean): ScreenHandlerType<BigAugmentedScreenHandler> {
        return Registry.register(
            Registries.SCREEN_HANDLER,
            "${if (isShulkerBox) "shulker" else "generic"}_${augmentLevel}_$augmentLevel2".id,
            ScreenHandlerType({ syncId: Int, playerInventory: PlayerInventory ->
                BigAugmentedScreenHandler(
                    syncId,
                    playerInventory,
                    SimpleInventory((augmentLevel + 3) * 9),
                    augmentLevel,
                    isShulkerBox,
                    augmentLevel2,
                )
            }, FeatureFlags.VANILLA_FEATURES),
        )
    }
}
