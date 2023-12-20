package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class VoidEnchantment extends ContainerEnchantment {
    public VoidEnchantment() {
        super(Mod.PORTABLE_CONTAINER_TARGET);
    }

    @Override
    protected boolean generate() {
        return WorldConfig.generateVoid();
    }

    public static boolean onItemPickup(ServerPlayerEntity player, ItemStack stack) {
        if (player.isCreative() && !WorldConfig.creativeVoid() || stack.isEmpty()) return false;
        Item item = stack.getItem();
        List<ItemStack> containerSlots = Utils.getContainers(player, Mod.VOID_ENCHANTMENT);
        for (ItemStack container : containerSlots) {
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container, player);
            for (ItemStack innerStack : containerInventory) {
                if (innerStack.isOf(item)) {
                    stack.setCount(0);
                    return true;
                }
            }
        }
        return false;
    }
}
