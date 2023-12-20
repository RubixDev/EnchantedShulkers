package de.rubixdev.enchantedshulkers;

import atonkish.reinfshulker.block.ReinforcedShulkerBoxBlock;
import atonkish.reinfcore.screen.ReinforcedStorageScreenHandler;
//#if MC >= 12001
import megaminds.clickopener.api.BlockEntityInventory;
//#else
//$$ import megaminds.clickopener.api.ShulkerInventory;
//#endif
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import com.illusivesoulworks.shulkerboxslot.ShulkerBoxAccessoryInventory;
import com.illusivesoulworks.shulkerboxslot.platform.Services;
import cursedflames.splitshulkers.SplitShulkerBoxBlockEntity;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.rubixdev.enchantedshulkers.mixin.compat.QuickShulker_ItemStackInventoryAccessor;
import de.rubixdev.enchantedshulkers.mixin.compat.ShulkerBoxAccessoryInventoryAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.shulkerutils.ItemStackInventory;
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
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;

public class Utils {
    public static boolean canEnchant(Item item) {
        return canEnchant(item.getDefaultStack());
    }

    public static boolean canEnchant(ItemStack stack) {
        return stack.isIn(Mod.PORTABLE_CONTAINER_TAG);
    }

    public static boolean canAugment(Item item) {
        return canAugment(item.getDefaultStack());
    }

    public static boolean canAugment(ItemStack stack) {
        return stack.isIn(Mod.AUGMENTABLE_CONTAINER_TAG);
    }

    public static List<ItemStack> getContainers(ServerPlayerEntity player, Enchantment enchantment) {
        List<ItemStack> playerInventory = new ArrayList<>();
        for (int i = 0; i <= player.getInventory().size(); i++) {
            playerInventory.add(player.getInventory().getStack(i));
        }
        if (FabricLoader.getInstance().isModLoaded("shulkerboxslot")) {
            // include the slot from Shulker Box Slot
            Services.INSTANCE.findShulkerBoxAccessory(player).ifPresent(triple -> playerInventory.add(triple.getLeft()));
        }
        return getContainers(playerInventory, player, enchantment, 0, false);
    }

    private static List<ItemStack> getContainers(
            List<ItemStack> inventory,
            ServerPlayerEntity player,
            Enchantment enchantment,
            int recursionDepth,
            boolean visitedEnderChest
    ) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : inventory) {
            // TODO: technically a vacuum shulker box inside a siphon ender chest should also be returned here,
            // but unless someone complains i can't be bothered :P
            if (canEnchant(stack)
                    && EnchantmentHelper.getLevel(enchantment, stack) > 0
                    && !(visitedEnderChest && stack.isOf(Items.ENDER_CHEST))
                    // in case some other mod allows shulkers to stack, ignore them to prevent duping
                    && (stack.isOf(Items.ENDER_CHEST) || stack.getCount() == 1)) {
                out.add(stack);
                if (recursionDepth < (WorldConfig.nestedContainers() ? 255 : 0)) {
                    out.addAll(getContainers(
                            getContainerInventory(stack, player),
                            player,
                            enchantment,
                            recursionDepth + 1,
                            visitedEnderChest || stack.isOf(Items.ENDER_CHEST)
                    ));
                }
            }
        }
        return out;
    }

    public static DefaultedList<ItemStack> getContainerInventory(ItemStack container, ServerPlayerEntity player) {
        if (container.isOf(Items.ENDER_CHEST)) {
            return player.getEnderChestInventory().heldStacks;
        }
        if (FabricLoader.getInstance().isModLoaded("quickshulker")) {
            if (player.currentScreenHandler instanceof ShulkerBoxScreenHandler handler
                    && handler.inventory instanceof ItemStackInventory inventory
                    && ((QuickShulker_ItemStackInventoryAccessor) inventory).getItemStack() == container) {
                return inventory.heldStacks;
            }
            if (FabricLoader.getInstance().isModLoaded("reinfshulker")
                    && player.currentScreenHandler instanceof ReinforcedStorageScreenHandler handler
                    && handler.getInventory() instanceof ItemStackInventory inventory
                    && ((QuickShulker_ItemStackInventoryAccessor) inventory).getItemStack() == container) {
                return inventory.heldStacks;
            }
        }
        if (FabricLoader.getInstance().isModLoaded("shulkerboxslot")
                && player.currentScreenHandler instanceof ShulkerBoxScreenHandler handler
                && handler.inventory instanceof ShulkerBoxAccessoryInventory inventory
                && ((ShulkerBoxAccessoryInventoryAccessor) inventory).getShulkerBox() == container) {
            return ((ShulkerBoxAccessoryInventoryAccessor) inventory).getItems();
        }
        //#if MC >= 12001
        if (FabricLoader.getInstance().isModLoaded("clickopener")
                && player.currentScreenHandler instanceof ShulkerBoxScreenHandler handler
                && handler.inventory instanceof BlockEntityInventory inventory
                && inventory.getLink() == container) {
            return inventory.getInventory();
        }
        //#else
        //$$ if (FabricLoader.getInstance().isModLoaded("clickopener")
        //$$         && player.currentScreenHandler instanceof ShulkerBoxScreenHandler handler
        //$$         && handler.inventory instanceof ShulkerInventory inventory
        //$$         && inventory.link() == container) {
        //$$     return inventory.inventory();
        //$$ }
        //#endif

        int size = 9 * getInvRows(EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, container));
        if (FabricLoader.getInstance().isModLoaded("reinfshulker") &&
                container.getItem() instanceof BlockItem blockItem &&
                Block.getBlockFromItem(blockItem) instanceof ReinforcedShulkerBoxBlock reinforcedShulkerBoxBlock) {
            size = reinforcedShulkerBoxBlock.getMaterial().getSize();
        }
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

    public static int getLevelFromNbt(Enchantment enchantment, NbtList nbt) {
        return EnchantmentHelper.fromNbt(nbt).getOrDefault(enchantment, 0);
    }

    public static int getInvRows(int augmentLevel) {
        return MathHelper.clamp(augmentLevel + 3, 3, 6);
    }
}
