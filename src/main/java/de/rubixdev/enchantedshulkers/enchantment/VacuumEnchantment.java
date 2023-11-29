package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class VacuumEnchantment extends Enchantment {
    public VacuumEnchantment() {
        super(Rarity.RARE, Mod.PORTABLE_CONTAINER_TARGET, new EquipmentSlot[] {
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
        });
    }

    @Override
    public int getMinPower(int level) {
        return level * 25;
    }

    @Override
    public int getMaxPower(int level) {
        return getMinPower(level) + 50;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return WorldConfig.generateVacuum();
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return WorldConfig.generateVacuum();
    }

    public static boolean onItemPickup(ServerPlayerEntity player, ItemStack stack) {
        if (player.isCreative() && !WorldConfig.creativeVacuum()) return false;
        return SiphonEnchantment.onItemPickup(player, stack, Mod.VACUUM_ENCHANTMENT, false);
    }
}
