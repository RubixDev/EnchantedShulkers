package de.rubixdev.enchantedshulkers.screen

import de.rubixdev.enchantedshulkers.Utils
import de.rubixdev.enchantedshulkers.Utils.clientModVersion
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.ShulkerBoxSlot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.DyeColor

class AugmentedScreenHandler private constructor(
    syncId: Int,
    playerInventory: PlayerInventory,
    inventory: Inventory,
    augmentLevel: Int,
    isShulkerBox: Boolean,
) : GenericContainerScreenHandler(
    when {
        augmentLevel == 1 -> ScreenHandlerType.GENERIC_9X4
        augmentLevel == 2 -> ScreenHandlerType.GENERIC_9X5
        augmentLevel >= 3 -> ScreenHandlerType.GENERIC_9X6
        else -> ScreenHandlerType.GENERIC_9X3
    },
    syncId,
    playerInventory,
    inventory,
    Utils.getInvRows(augmentLevel),
) {
    init {
        if (isShulkerBox) convertSlots()
    }

    companion object {
        @JvmStatic
        fun create(
            syncId: Int,
            playerInventory: PlayerInventory,
            inventory: Inventory,
            augmentLevel: Int,
            title: Text,
            color: DyeColor?,
            isShulkerBox: Boolean,
            augmentLevel2: Int?,
        ): ScreenHandler? {
            val rows = Utils.getInvRows(augmentLevel)
            ScreenHandler.checkSize(inventory, 9 * rows)
            val player = playerInventory.player
            if (player is ServerPlayerEntity && (augmentLevel > 3 || (augmentLevel2 != null && augmentLevel + augmentLevel2 > 0))) {
                return if (player.clientModVersion() >= 2) {
                    BigAugmentedScreenHandler(syncId, playerInventory, inventory, augmentLevel, isShulkerBox, augmentLevel2)
                } else {
                    VanillaBigAugmentedGui(player, inventory, rows, title, color, isShulkerBox).openAsScreenHandler(
                        syncId,
                        playerInventory,
                        player,
                    )
                }
            }
            return AugmentedScreenHandler(syncId, playerInventory, inventory, augmentLevel, isShulkerBox)
        }
    }

    private fun convertSlots() {
        // we have to replace the generic Slots with ShulkerBoxSlots to prevent nesting Shulker Boxes
        for (i in 0 until this.slots.size) {
            val oldSlot = this.slots[i]
            val newSlot = ShulkerBoxSlot(oldSlot.inventory, oldSlot.index, oldSlot.x, oldSlot.y)
            newSlot.id = oldSlot.id
            this.slots[i] = newSlot
        }
    }
}
