package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.WorldConfig;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class SiphonEnchantment extends Enchantment {
    public SiphonEnchantment() {
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
        return WorldConfig.generateSiphon();
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return WorldConfig.generateSiphon();
    }

    public static boolean onItemPickup(ServerPlayerEntity player, ItemStack stack) {
        return (!player.isCreative() || WorldConfig.creativeSiphon())
                && onItemPickup(player, stack, Mod.SIPHON_ENCHANTMENT, true);
    }

    public static boolean onItemPickup(ServerPlayerEntity player, ItemStack stack, Enchantment enchantment, boolean requireStack) {
        if (WorldConfig.strongerSiphon())
            return onItemPickupStrongSiphon(player, stack, enchantment, requireStack);
        List<ItemStack> containerSlots = Utils.getContainers(player, enchantment);

        boolean usedSiphon = false;
        for (ItemStack container : containerSlots) {
            if (stack.isEmpty()) return usedSiphon;
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container, player);
            boolean updateContainer = false;
            for (int i = 0; i < containerInventory.toArray().length; i++) {
                ItemStack innerStack = containerInventory.get(i);
                if (innerStack.isEmpty() && requireStack) continue;
                if (trySiphonStack(stack, innerStack, containerInventory, i)) {
                    updateContainer = true;
                    if (stack.isEmpty()) break;
                }
            }
            if (updateContainer) {
                usedSiphon = true;
                Utils.setContainerInventory(container, containerInventory);
            }
        }
        return usedSiphon;
    }

    public static boolean onItemPickupStrongSiphon(ServerPlayerEntity player, ItemStack stack, Enchantment enchantment, boolean requireStack) {
        List<ItemStack> containerSlots = Utils.getContainers(player, enchantment);

        boolean usedSiphon = false;
        for (ItemStack container : containerSlots) {
            Item item = stack.getItem();
            ArrayList<Integer> foundItemSlots = new ArrayList<>();
            ArrayList<Integer> emptyItemSlots = new ArrayList<>();
            if (stack.isEmpty())
                return usedSiphon;
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container, player);
            for (int i = 0; i < containerInventory.toArray().length; i++) {
                ItemStack innerStack = containerInventory.get(i);
                if (innerStack.isEmpty())
                    emptyItemSlots.add(i);
                else if (innerStack.isOf(item))
                    foundItemSlots.add(i);
            }

            if (foundItemSlots.isEmpty())
                return false;

            boolean updateContainer = false;
            for (int slotId : foundItemSlots) {
                if (stack.isEmpty())
                    break;
                if (trySiphonStack(stack, containerInventory.get(slotId), containerInventory, slotId))
                    updateContainer = true;
            }
            for (int slotId : emptyItemSlots) {
                if (stack.isEmpty())
                    break;
                if (trySiphonStack(stack, containerInventory.get(slotId), containerInventory, slotId))
                    updateContainer = true;
            }

            if (updateContainer) {
                usedSiphon = true;
                Utils.setContainerInventory(container, containerInventory);
            }

        }
        return usedSiphon;
    }

    static boolean trySiphonStack(ItemStack from, ItemStack to, DefaultedList<ItemStack> containerInventory, int toIndex) {
        if (to.isEmpty()) {
            containerInventory.set(toIndex, from.copy());
            from.setCount(0);
            return true;
        }

        if (!ItemStack.canCombine(from, to)) return false;
        int transferCount = Math.min(to.getMaxCount() - to.getCount(), from.getCount());
        if (transferCount <= 0) return false;

        from.decrement(transferCount);
        to.increment(transferCount);
        return true;
    }
}
