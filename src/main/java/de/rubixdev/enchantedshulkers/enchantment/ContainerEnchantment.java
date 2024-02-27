package de.rubixdev.enchantedshulkers.enchantment;

import eu.pb4.polymer.core.api.other.PolymerEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public abstract class ContainerEnchantment extends Enchantment implements PolymerEnchantment {
    protected ContainerEnchantment(EnchantmentTarget target) {
        super(Rarity.RARE, target, new EquipmentSlot[] {
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
        });
    }

    protected abstract boolean generate();

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return this.generate();
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return this.generate();
    }
}
