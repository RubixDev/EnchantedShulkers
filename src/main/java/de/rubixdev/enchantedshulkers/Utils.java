package de.rubixdev.enchantedshulkers;

import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;

public class Utils {
    @SuppressWarnings("deprecation")
    public static boolean canEnchant(Item item) {
        return item.getRegistryEntry().isIn(Mod.PORTABLE_CONTAINER_TAG);
    }

    public static boolean canEnchant(ItemStack stack) {
        return stack.isIn(Mod.PORTABLE_CONTAINER_TAG);
    }

    public static List<Pair<Integer, ItemStack>> getContainers(Inventory inventory, Enchantment enchantment) {
        List<Pair<Integer, ItemStack>> out = new ArrayList<>();
        for (int i = 0; i <= inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (canEnchant(stack) && EnchantmentHelper.getLevel(enchantment, stack) > 0) out.add(new Pair<>(i, stack));
        }
        return out;
    }

    public static DefaultedList<ItemStack> getContainerInventory(ItemStack container, ServerPlayerEntity player) {
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);

        if (container.isOf(Items.ENDER_CHEST)) {
            return player.getEnderChestInventory().stacks;
        }

        NbtCompound nbt = container.getSubNbt("BlockEntityTag");
        if (nbt != null && nbt.contains("Items", NbtElement.LIST_TYPE)) Inventories.readNbt(nbt, inventory);
        return inventory;
    }

    public static void setContainerInventory(ItemStack container, DefaultedList<ItemStack> inventory) {
        // No need to write any NBT on ender chests
        if (container.isOf(Items.ENDER_CHEST)) return;

        NbtCompound nbt = container.getOrCreateSubNbt("BlockEntityTag");
        Inventories.writeNbt(nbt, inventory);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static <T extends BlockEntity> boolean shouldGlint(T blockEntity) {
        return blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity
                && !enchantableBlockEntity.getEnchantments().isEmpty();
    }
}
