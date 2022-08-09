package de.rubixdev.enchanted_shulkers.enchantment;

import de.rubixdev.enchanted_shulkers.Mod;
import de.rubixdev.enchanted_shulkers.Utils;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

public class RefillEnchantment extends Enchantment {
    public RefillEnchantment() {
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

    private static final Previous previous = new Previous();

    private static class Previous {
        int slot = -1;
        ItemStack mainStack = ItemStack.EMPTY;
        ItemStack offStack = ItemStack.EMPTY;
    }

    public static void onPlayerTick(ServerPlayerEntity player) {
        int currentSlot = player.getInventory().selectedSlot;
        ItemStack currentMainStack = player.getInventory().getMainHandStack();
        ItemStack currentOffStack = player.getInventory().getStack(PlayerInventory.OFF_HAND_SLOT);

        boolean swappedHands = ItemStack.areNbtEqual(previous.mainStack, currentOffStack)
                && ItemStack.areNbtEqual(currentMainStack, previous.offStack);
        boolean wasMainEmptied = previous.mainStack.getCount() > 0 && currentMainStack.isEmpty() && !swappedHands;
        boolean wasOffEmptied = previous.offStack.getCount() > 0 && currentOffStack.isEmpty() && !swappedHands;

        if (currentSlot != previous.slot
                || swappedHands
                || !(wasMainEmptied || ItemStack.canCombine(currentMainStack, previous.mainStack))
                || !(wasOffEmptied || ItemStack.canCombine(currentOffStack, previous.offStack))) {
            previous.slot = currentSlot;
            previous.mainStack = currentMainStack.copy();
            previous.offStack = currentOffStack.copy();
            return;
        }

        if (currentMainStack.getCount() < previous.mainStack.getCount()) {
            refill(
                    player,
                    currentSlot,
                    previous.mainStack,
                    previous.mainStack.getCount() - currentMainStack.getCount());
        } else if (currentOffStack.getCount() < previous.offStack.getCount()) {
            refill(
                    player,
                    PlayerInventory.OFF_HAND_SLOT,
                    previous.offStack,
                    previous.offStack.getCount() - currentOffStack.getCount());
        }
        previous.mainStack = currentMainStack.copy();
        previous.offStack = currentOffStack.copy();
    }

    static void refill(ServerPlayerEntity player, int slot, ItemStack itemType, int amount) {
        List<Pair<Integer, ItemStack>> containerSlots =
                Utils.getContainers(player.getInventory(), Mod.REFILL_ENCHANTMENT);
        for (Pair<Integer, ItemStack> containerSlot : containerSlots) {
            if (amount <= 0) return;
            ItemStack container = containerSlot.getRight();
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container);

            boolean updateContainer = false;
            for (ItemStack innerStack : containerInventory) {
                if (innerStack.isEmpty()) continue;
                int refilled = tryRefillSlot(player.getInventory(), innerStack, slot, itemType, amount);
                if (refilled > 0) {
                    amount -= refilled;
                    updateContainer = true;
                    if (amount <= 0) break;
                }
            }
            if (updateContainer) Utils.setContainerInventory(container, containerInventory);
        }
    }

    static int tryRefillSlot(PlayerInventory inventory, ItemStack from, int slot, ItemStack itemType, int amount) {
        ItemStack to = inventory.getStack(slot);

        if (!ItemStack.canCombine(itemType, from)) return 0;
        if (!to.isEmpty() && !ItemStack.canCombine(to, from)) return 0;

        int transferCount = Math.min(Math.min(amount, itemType.getMaxCount() - to.getCount()), from.getCount());
        if (transferCount <= 0) return 0;

        if (to.isEmpty()) {
            ItemStack stack = from.copy();
            stack.setCount(transferCount);
            inventory.setStack(slot, stack);
        } else to.increment(transferCount);
        from.decrement(transferCount);
        return transferCount;
    }
}
