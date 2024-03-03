package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Mod
import de.rubixdev.enchantedshulkers.config.WorldConfig

class AugmentEnchantment : ContainerEnchantment(Mod.AUGMENTABLE_CONTAINER_TARGET) {
    override fun generate() = WorldConfig.generateAugment
    override fun getMaxLevel() = WorldConfig.maxAugmentLevel
}
