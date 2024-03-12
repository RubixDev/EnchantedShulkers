package de.rubixdev.enchantedshulkers.screen

import de.rubixdev.enchantedshulkers.Utils
import de.rubixdev.enchantedshulkers.config.ClientConfig
import net.minecraft.util.Identifier

const val SLOT_SIZE = 18
const val TEXT_LINE_HEIGHT = 11
const val PADDING_TOP = 17
const val PADDING_BOTTOM = 7
const val PADDING_LEFT = 7
const val PADDING_RIGHT = 7
const val GAP_BETWEEN_CONTAINER_INVENTORY_AND_PLAYER_INVENTORY = 14
const val GAP_BETWEEN_PLAYER_INVENTORY_AND_HOTBAR = 4
const val GAP_BETWEEN_CONTAINER_INVENTORY_AND_SCROLLBAR = 4

const val SINGLE_SCREEN_THRESHOLD_SIZE = 81
const val SINGLE_SCREEN_DEFAULT_COLS = 9
const val SCROLL_SCREEN_COLS = 9

val BG_TEXTURE = Identifier("textures/gui/demo_background.png")
const val BG_CORNER = 4
const val BG_X = 0
const val BG_Y = 0
const val BG_WIDTH = 248
const val BG_HEIGHT = 166

val CONTAINER_TEXTURE = Identifier("textures/gui/container/generic_54.png")
const val CONTAINER_INVENTORY_X = 7
const val CONTAINER_INVENTORY_Y = 17
const val CONTAINER_INVENTORY_COLS = 9
const val CONTAINER_INVENTORY_ROWS = 6
const val PLAYER_INVENTORY_X = 7
const val PLAYER_INVENTORY_Y = 139
const val PLAYER_INVENTORY_WIDTH = 162
const val PLAYER_INVENTORY_HEIGHT = 76

val SCROLLBAR_BG_TEXTURE = Identifier("textures/gui/container/creative_inventory/tab_items.png")
const val SCROLLBAR_BG_X = 174
const val SCROLLBAR_BG_Y = 17
const val SCROLLBAR_BG_WIDTH = 14
const val SCROLLBAR_BG_HEIGHT = 112

//#if MC >= 12002
val SCROLLER_TEXTURE = Identifier("container/creative_inventory/scroller")
val SCROLLER_DISABLED_TEXTURE = Identifier("container/creative_inventory/scroller_disabled")

//#else
//$$ val SCROLLBAR_TEXTURE = Identifier("textures/gui/container/creative_inventory/tabs.png")
//$$ const val SCROLLBAR_X = 232
//$$ const val SCROLLBAR_Y = 0
//#endif
const val SCROLLER_WIDTH = 12
const val SCROLLER_HEIGHT = 15

data class Point(val x: Int, val y: Int)

class BigAugmentedScreenModel(private val augmentLevel: Int, private val augmentLevel2: Int?) {
    companion object {

        fun getContainerInventorySize(augmentLevel: Int, augmentLevel2: Int?) =
            9 * Utils.getInvRows(augmentLevel).let { if (augmentLevel2 != null) it + Utils.getInvRows(augmentLevel2) else it }

        fun getContainerInventoryColumns(size: Int) =
            if (ClientConfig.scrollScreen) {
                SCROLL_SCREEN_COLS
            } else if (size <= SINGLE_SCREEN_THRESHOLD_SIZE) {
                SINGLE_SCREEN_DEFAULT_COLS
            } else {
                size / SINGLE_SCREEN_DEFAULT_COLS
            }

        fun getContainerInventoryRows(size: Int, cols: Int) = (size / cols).let { rows ->
            if (ClientConfig.scrollScreen && rows > ClientConfig.scrollScreenRows) ClientConfig.scrollScreenRows else rows
        }
    }

    val containerInventoryPoint get() = Point(CONTAINER_INVENTORY_X, CONTAINER_INVENTORY_Y)
    val playerInventoryPoint: Point get() {
        val size = getContainerInventorySize(augmentLevel, augmentLevel2)
        val cols = getContainerInventoryColumns(size)
        val rows = getContainerInventoryRows(size, cols)

        val x = CONTAINER_INVENTORY_X + (cols - SINGLE_SCREEN_DEFAULT_COLS) * SLOT_SIZE / 2
        val y = CONTAINER_INVENTORY_Y + rows * SLOT_SIZE + GAP_BETWEEN_CONTAINER_INVENTORY_AND_PLAYER_INVENTORY

        return Point(x, y)
    }
}
