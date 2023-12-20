package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.WorldConfig;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class SiphonEnchantment extends ContainerEnchantment {
    public SiphonEnchantment() {
        super(Mod.PORTABLE_CONTAINER_TARGET);
    }

    @Override
    protected boolean generate() {
        return WorldConfig.generateSiphon();
    }

    public static boolean onItemPickup(ServerPlayerEntity player, ItemStack stack) {
        return (!player.isCreative() || WorldConfig.creativeSiphon())
                && onItemPickup(player, stack, Mod.SIPHON_ENCHANTMENT, true, WorldConfig.strongerSiphon());
    }

    public static boolean onItemPickup(
            ServerPlayerEntity player,
            ItemStack stack,
            Enchantment enchantment,
            boolean requireStack,
            boolean strongerSiphon
    ) {
        if (strongerSiphon) return onItemPickupStrongerSiphon(player, stack, enchantment);
        List<ItemStack> containerSlots = Utils.getContainers(player, enchantment);

        boolean usedSiphon = false;
        for (ItemStack container : containerSlots) {
            if (stack.isEmpty()) return usedSiphon;
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container, player);
            boolean updateContainer = false;
            for (int i = 0; i < containerInventory.size(); i++) {
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

    private static boolean onItemPickupStrongerSiphon(ServerPlayerEntity player, ItemStack stack, Enchantment enchantment) {
        List<ItemStack> containerSlots = Utils.getContainers(player, enchantment);

        boolean usedSiphon = false;
        for (ItemStack container : containerSlots) {
            if (stack.isEmpty())
                return usedSiphon;
            Item item = stack.getItem();
            List<Integer> foundItemSlots = new ArrayList<>();
            List<Integer> emptyItemSlots = new ArrayList<>();
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container, player);
            for (int i = 0; i < containerInventory.size(); i++) {
                ItemStack innerStack = containerInventory.get(i);
                if (innerStack.isEmpty())
                    emptyItemSlots.add(i);
                else if (innerStack.isOf(item))
                    foundItemSlots.add(i);
            }

            // don't siphon if there are no items of that type already in the container
            if (foundItemSlots.isEmpty())
                continue;

            boolean updateContainer = false;
            // try to add to the existing stacks first
            for (int slotId : foundItemSlots) {
                if (stack.isEmpty())
                    break;
                if (trySiphonStack(stack, containerInventory.get(slotId), containerInventory, slotId))
                    updateContainer = true;
            }
            // then fill empty slots
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
