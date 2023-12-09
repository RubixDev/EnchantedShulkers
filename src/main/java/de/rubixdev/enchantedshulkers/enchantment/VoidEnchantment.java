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

public class VoidEnchantment extends Enchantment {
    public VoidEnchantment() {
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
        return WorldConfig.generateVoid();
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return WorldConfig.generateVoid();
    }

    public static boolean onItemPickup(ServerPlayerEntity player, ItemStack stack) {
        if (player.isCreative() && !WorldConfig.creativeVoid() || stack.isEmpty()) return false;
        List<ItemStack> containerSlots = Utils.getContainers(player, Mod.VOID_ENCHANTMENT);
        for (ItemStack container : containerSlots) {
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container, player);
            for (ItemStack innerStack : containerInventory) {
                if (innerStack.isOf(stack.getItem())) {
                    stack.setCount(0);
                    return true;
                }
            }
        }
        return false;
    }
}
