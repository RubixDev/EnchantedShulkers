package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
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

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return WorldConfig.generateBooks();
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return WorldConfig.generateBooks();
    }

    public static void onPlayerTick(
            ServerPlayerEntity player,
            boolean inventoryOpen,
            int currentSlot,
            ItemStack currentMainStack,
            ItemStack currentOffStack,
            int previousSlot,
            ItemStack previousMainStack,
            ItemStack previousOffStack) {
        boolean allowsRefill = (!player.isCreative() || WorldConfig.creativeRefill()) && !inventoryOpen;
        boolean swappedHands = ItemStack.areEqual(previousMainStack, currentOffStack)
                && ItemStack.canCombine(previousMainStack, currentOffStack)
                && ItemStack.areEqual(currentMainStack, previousOffStack)
                && ItemStack.canCombine(currentMainStack, previousOffStack);
        boolean wasMainEmptied = previousMainStack.getCount() > 0 && currentMainStack.isEmpty() && !swappedHands;
        boolean wasOffEmptied = previousOffStack.getCount() > 0 && currentOffStack.isEmpty() && !swappedHands;
        boolean shouldRefillMain = (wasMainEmptied || ItemStack.canCombine(currentMainStack, previousMainStack))
                && currentMainStack.getCount() < previousMainStack.getCount()
                && (WorldConfig.refillNonStackables() || previousMainStack.isStackable());
        boolean shouldRefillOff = (wasOffEmptied || ItemStack.canCombine(currentOffStack, previousOffStack))
                && currentOffStack.getCount() < previousOffStack.getCount()
                && (WorldConfig.refillNonStackables() || previousOffStack.isStackable())
                && WorldConfig.refillOffhand();
        boolean doRefill =
                allowsRefill && currentSlot == previousSlot && !swappedHands && (shouldRefillMain || shouldRefillOff);

        if (!doRefill) return;
        if (shouldRefillMain) {
            refill(player, currentSlot, previousMainStack, previousMainStack.getCount() - currentMainStack.getCount());
        } else {
            refill(
                    player,
                    PlayerInventory.OFF_HAND_SLOT,
                    previousOffStack,
                    previousOffStack.getCount() - currentOffStack.getCount());
        }
    }

    static void refill(ServerPlayerEntity player, int slot, ItemStack itemType, int amount) {
        List<ItemStack> containerSlots = Utils.getContainers(player, Mod.REFILL_ENCHANTMENT);
        for (ItemStack container : containerSlots) {
            if (amount <= 0) return;
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container, player);

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
