package de.rubixdev.enchantedshulkers;

//#if MC < 12002
//$$ import atonkish.reinfshulker.block.ReinforcedShulkerBoxBlock;
//$$ import net.minecraft.block.Block;
//$$ import net.minecraft.item.BlockItem;
//#endif
import cursedflames.splitshulkers.SplitShulkerBoxBlockEntity;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class Utils {
    public static boolean canEnchant(Item item) {
        return canEnchant(item.getDefaultStack());
    }

    public static boolean canEnchant(ItemStack stack) {
        return stack.isIn(Mod.PORTABLE_CONTAINER_TAG);
    }

    public static List<ItemStack> getContainers(ServerPlayerEntity player, Enchantment enchantment) {
        visitedEnderChest = false;
        List<ItemStack> playerInventory = new ArrayList<>();
        for (int i = 0; i <= player.getInventory().size(); i++) {
            playerInventory.add(player.getInventory().getStack(i));
        }
        return getContainers(playerInventory, player, enchantment, 0);
    }

    // TODO: does this break with multiple players?
    private static boolean visitedEnderChest;

    private static List<ItemStack> getContainers(
            List<ItemStack> inventory, ServerPlayerEntity player, Enchantment enchantment, int recursionDepth) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : inventory) {
            // TODO: technically a vacuum shulker box inside a siphon ender chest should also be returned here,
            // but unless someone complains i can't be bothered :P
            if (canEnchant(stack)
                    && EnchantmentHelper.getLevel(enchantment, stack) > 0
                    && !(visitedEnderChest && stack.isOf(Items.ENDER_CHEST))) {
                out.add(stack);
                if (stack.isOf(Items.ENDER_CHEST)) visitedEnderChest = true;
                if (recursionDepth < (WorldConfig.nestedContainers() ? 255 : 0)) {
                    out.addAll(getContainers(
                            getContainerInventory(stack, player), player, enchantment, recursionDepth + 1));
                }
            }
        }
        return out;
    }

    public static DefaultedList<ItemStack> getContainerInventory(ItemStack container, ServerPlayerEntity player) {
        if (container.isOf(Items.ENDER_CHEST)) {
            return player.getEnderChestInventory().stacks;
        }

        int size = 27;
        //#if MC < 12002
        //$$ if (FabricLoader.getInstance().isModLoaded("reinfshulker") &&
        //$$         container.getItem() instanceof BlockItem blockItem &&
        //$$         Block.getBlockFromItem(blockItem) instanceof ReinforcedShulkerBoxBlock reinforcedShulkerBoxBlock) {
        //$$         size = reinforcedShulkerBoxBlock.getMaterial().getSize();
        //$$ }
        //#endif
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(size, ItemStack.EMPTY);

        NbtCompound nbt = container.getSubNbt("BlockEntityTag");
        if (nbt != null && nbt.contains("Items", NbtElement.LIST_TYPE))
            Inventories.readNbt(nbt, inventory);
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
                && !enchantableBlockEntity.enchantedShulkers$getEnchantments().isEmpty();
    }

    public static MutableText translatableText(String trKey, Object... args) {
        return Text.translatableWithFallback(trKey, Mod.EN_US_TRANSLATIONS.get(trKey), args);
    }

    // for compat with Split Shulker Boxes
    public static boolean hasTwoColors(ShulkerBoxBlockEntity shulkerBox) {
        if (!FabricLoader.getInstance().isModLoaded("splitshulkers")) return false;
        SplitShulkerBoxBlockEntity splitShulker = (SplitShulkerBoxBlockEntity) shulkerBox;
        return !Objects.equals(splitShulker.getColor(), splitShulker.splitshulkers_getSecondaryColor());
    }
}
