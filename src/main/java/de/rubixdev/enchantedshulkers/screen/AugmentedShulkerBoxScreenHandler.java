package de.rubixdev.enchantedshulkers.screen;

import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AugmentedShulkerBoxScreenHandler extends GenericContainerScreenHandler {
    private AugmentedShulkerBoxScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, int enchantmentLevel) {
        super(
                enchantmentLevel == 1 ? ScreenHandlerType.GENERIC_9X4
                    : enchantmentLevel == 2 ? ScreenHandlerType.GENERIC_9X5
                    : enchantmentLevel >= 3 ? ScreenHandlerType.GENERIC_9X6
                    : ScreenHandlerType.GENERIC_9X3,
                syncId,
                playerInventory,
                inventory,
                Utils.getInvRows(enchantmentLevel)
        );
        this.convertSlots();
    }

    public static ScreenHandler create(int syncId, PlayerInventory playerInventory, Inventory inventory, int enchantmentLevel, Text title) {
        int rows = Utils.getInvRows(enchantmentLevel);
        ScreenHandler.checkSize(inventory, 9 * rows);
        if (enchantmentLevel > 3 && playerInventory.player instanceof ServerPlayerEntity player) {
            // TODO: modded clients should have a better UI
            return new VanillaBigAugmentedGui(player, inventory, rows, title).openAsScreenHandler(syncId, playerInventory, player);
        }
        return new AugmentedShulkerBoxScreenHandler(syncId, playerInventory, inventory, enchantmentLevel);
    }

    private void convertSlots() {
        // we have to replace the generic Slots with ShulkerBoxSlots to prevent nesting Shulker Boxes
        for (int i = 0; i < this.slots.size(); i++) {
            Slot oldSlot = this.slots.get(i);
            ShulkerBoxSlot newSlot = new ShulkerBoxSlot(oldSlot.inventory, oldSlot.getIndex(), oldSlot.x, oldSlot.y);
            newSlot.id = oldSlot.id;
            this.slots.set(i, newSlot);
        }
    }
}
