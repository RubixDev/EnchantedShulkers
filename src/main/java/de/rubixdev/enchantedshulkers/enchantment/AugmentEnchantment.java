package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.config.WorldConfig;

public class AugmentEnchantment extends ContainerEnchantment {
    public AugmentEnchantment() {
        super(Mod.AUGMENTABLE_CONTAINER_TARGET);
    }

    @Override
    protected boolean generate() {
        return WorldConfig.generateAugment();
    }

    @Override
    public int getMaxLevel() {
        return WorldConfig.maxAugmentLevel();
    }
}
