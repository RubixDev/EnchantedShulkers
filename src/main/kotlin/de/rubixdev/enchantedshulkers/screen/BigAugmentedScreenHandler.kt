package de.rubixdev.enchantedshulkers.screen

import de.rubixdev.enchantedshulkers.config.ClientConfig
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.ShulkerBoxSlot
import net.minecraft.screen.slot.Slot

class BigAugmentedScreenHandler internal constructor(
    syncId: Int,
    playerInventory: PlayerInventory,
    val inventory: Inventory,
    val augmentLevel: Int,
    val isShulkerBox: Boolean,
    val augmentLevel2: Int?,
) : ScreenHandler(
    when {
        augmentLevel2 == null && isShulkerBox -> ScreenHandlerTypes.SHULKER_LIST[augmentLevel - 4]
        augmentLevel2 == null && !isShulkerBox -> ScreenHandlerTypes.GENERIC_LIST[augmentLevel - 4]
        isShulkerBox -> throw IllegalArgumentException("double shulker boxes are not supported")
        else -> ScreenHandlerTypes.GENERIC_DOUBLE_MAP[augmentLevel to augmentLevel2!!]
    },
    syncId,
) {
    val cols = BigAugmentedScreenModel.getContainerInventoryColumns(inventory.size())
    val rows = BigAugmentedScreenModel.getContainerInventoryRows(inventory.size(), cols)
    private val screenModel = BigAugmentedScreenModel(augmentLevel, augmentLevel2)

    val hasScrollbar get() = ClientConfig.scrollScreen && inventory.size() > SCROLL_SCREEN_COLS * ClientConfig.scrollScreenRows

    init {
        inventory.onOpen(playerInventory.player)
        addSlots(playerInventory)
    }

    private fun addSlots(playerInventory: PlayerInventory) {
        val containerInventoryPoint = screenModel.containerInventoryPoint
        val playerInventoryPoint = screenModel.playerInventoryPoint

        // main container inventory
        for (index in 0 until inventory.size()) {
            val col = index % cols
            val row = (index - col) / cols
            var x = containerInventoryPoint.x + col * SLOT_SIZE + 1
            var y = containerInventoryPoint.y + row * SLOT_SIZE + 1

            if (ClientConfig.scrollScreen && row >= ClientConfig.scrollScreenRows) {
                // HACK: put invisible slots far outside the visible area for scrolling screen
                x = Int.MIN_VALUE
                y = Int.MIN_VALUE
            }

            val slot = if (isShulkerBox) ShulkerBoxSlot(inventory, index, x, y) else Slot(inventory, index, x, y)
            addSlot(slot)
        }

        // main player inventory
        for (index in 9 until 36) {
            val col = (index - 9) % 9
            val row = (index - col - 9) / 9
            val x = playerInventoryPoint.x + col * SLOT_SIZE + 1
            val y = playerInventoryPoint.y + row * SLOT_SIZE + 1

            val slot = Slot(playerInventory, index, x, y)
            addSlot(slot)
        }

        // player hotbar
        for (index in 0 until 9) {
            val x = playerInventoryPoint.x + index * SLOT_SIZE + 1
            val y = playerInventoryPoint.y + 3 * SLOT_SIZE + GAP_BETWEEN_PLAYER_INVENTORY_AND_HOTBAR + 1

            val slot = Slot(playerInventory, index, x, y)
            addSlot(slot)
        }
    }

    fun scrollItems(position: Float) {
        if (!ClientConfig.scrollScreen) return
        val i = (inventory.size() + SCROLL_SCREEN_COLS - 1) / SCROLL_SCREEN_COLS - ClientConfig.scrollScreenRows
        val srow = ((position * i.toFloat()) + 0.5).toInt().coerceAtLeast(0)

        val containerInventoryPoint = screenModel.containerInventoryPoint
        for (index in 0 until inventory.size()) {
            val col = index % cols
            val row = (index - col) / cols
            var x = containerInventoryPoint.x + col * SLOT_SIZE + 1
            var y = containerInventoryPoint.y + (row - srow) * SLOT_SIZE + 1

            if (row < srow || row >= srow + ClientConfig.scrollScreenRows) {
                // HACK: put invisible slots far outside the visible area for scrolling screen
                x = Int.MIN_VALUE
                y = Int.MIN_VALUE
            }

            val slot = getSlot(index)
            slot.x = x
            slot.y = y
        }
    }

    override fun quickMove(player: PlayerEntity?, slotIndex: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots.getOrNull(slotIndex)
        if (slot != null && slot.hasStack()) {
            val itemStack2 = slot.stack
            itemStack = itemStack2.copy()
            val didInsert = if (slotIndex < inventory.size()) insertItem(itemStack2, inventory.size(), slots.size, true) else insertItem(itemStack2, 0, inventory.size(), false)
            if (!didInsert) {
                return ItemStack.EMPTY
            }
            if (itemStack2.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }
        return itemStack
    }

    override fun canUse(player: PlayerEntity?): Boolean = inventory.canPlayerUse(player)

    override fun onClosed(player: PlayerEntity?) {
        super.onClosed(player)
        inventory.onClose(player)
    }
}
