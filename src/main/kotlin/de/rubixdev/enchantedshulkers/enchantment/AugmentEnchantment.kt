package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.config.WorldConfig

class AugmentEnchantment : ContainerEnchantment(
    //#if MC >= 12006
    Mod.AUGMENTABLE_CONTAINER_TAG,
    //#else
    //$$ Mod.AUGMENTABLE_CONTAINER_TARGET,
    //#endif
    //#if MC >= 12006
    WorldConfig.maxAugmentLevel,
    //#endif
) {
    override fun generate() = WorldConfig.generateAugment
    override fun getMaxLevel() = WorldConfig.maxAugmentLevel
}
