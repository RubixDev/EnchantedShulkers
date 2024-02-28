package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Utils;
import eu.pb4.polymer.core.api.other.PolymerEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public @Nullable Enchantment getPolymerReplacement(ServerPlayerEntity player) {
        // clients with this mod can understand the enchantments
        if (Utils.hasClientMod(player)) return this;
        // clients without this mod have it handled by Polymer
        return PolymerEnchantment.super.getPolymerReplacement(player);
    }
}
