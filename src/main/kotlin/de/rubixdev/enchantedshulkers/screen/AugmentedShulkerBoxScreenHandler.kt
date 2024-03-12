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

class AugmentedShulkerBoxScreenHandler private constructor(
    syncId: Int,
    playerInventory: PlayerInventory,
    inventory: Inventory,
    enchantmentLevel: Int,
) : GenericContainerScreenHandler(
    when {
        enchantmentLevel == 1 -> ScreenHandlerType.GENERIC_9X4
        enchantmentLevel == 2 -> ScreenHandlerType.GENERIC_9X5
        enchantmentLevel >= 3 -> ScreenHandlerType.GENERIC_9X6
        else -> ScreenHandlerType.GENERIC_9X3
    },
    syncId,
    playerInventory,
    inventory,
    Utils.getInvRows(enchantmentLevel),
) {
    init {
        convertSlots()
    }

    companion object {
        @JvmStatic
        fun create(
            syncId: Int,
            playerInventory: PlayerInventory,
            inventory: Inventory,
            enchantmentLevel: Int,
            title: Text,
            color: DyeColor?,
        ): ScreenHandler? {
            val rows = Utils.getInvRows(enchantmentLevel)
            ScreenHandler.checkSize(inventory, 9 * rows)
            val player = playerInventory.player
            if (enchantmentLevel > 3 && player is ServerPlayerEntity) {
                return if (player.clientModVersion() >= 2) {
                    BigAugmentedScreenHandler(syncId, playerInventory, inventory, enchantmentLevel, true, null)
                } else {
                    VanillaBigAugmentedGui(player, inventory, rows, title, color).openAsScreenHandler(
                        syncId,
                        playerInventory,
                        player,
                    )
                }
            }
            return AugmentedShulkerBoxScreenHandler(syncId, playerInventory, inventory, enchantmentLevel)
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
