package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
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
        return WorldConfig.generateBooks();
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return WorldConfig.generateBooks();
    }

    public static boolean onItemPickup(ServerPlayerEntity player, ItemStack stack) {
        if (player.isCreative() && !WorldConfig.creativeSiphon()) return false;

        List<Pair<Integer, ItemStack>> containerSlots =
                Utils.getContainers(player.getInventory(), Mod.SIPHON_ENCHANTMENT);

        boolean usedSiphon = false;
        for (Pair<Integer, ItemStack> containerSlot : containerSlots) {
            if (stack.isEmpty()) return usedSiphon;
            ItemStack container = containerSlot.getRight();
            DefaultedList<ItemStack> containerInventory = Utils.getContainerInventory(container, player);

            boolean updateContainer = false;
            for (ItemStack innerStack : containerInventory) {
                if (innerStack.isEmpty()) continue;
                if (trySiphonStack(stack, innerStack)) {
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

    static boolean trySiphonStack(ItemStack from, ItemStack to) {
        if (!ItemStack.canCombine(from, to)) return false;
        int transferCount = Math.min(to.getMaxCount() - to.getCount(), from.getCount());
        if (transferCount <= 0) return false;

        from.decrement(transferCount);
        to.increment(transferCount);
        return true;
    }
}
