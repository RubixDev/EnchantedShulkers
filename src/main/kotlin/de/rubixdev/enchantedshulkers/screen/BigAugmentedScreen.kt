/**
 * A lot of this code is heavily based on <https://github.com/Aton-Kish/reinforced-core/blob/b1406a7ba19669c8fda3a17aaee2a34608167142/src/client/java/atonkish/reinfcore/client/gui/screen/ingame/ReinforcedStorageScreen.java>.
 * Credits go to Aton-Kish
 */

package de.rubixdev.enchantedshulkers.screen

import com.mojang.blaze3d.systems.RenderSystem
import de.rubixdev.enchantedshulkers.config.ClientConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.GameRenderer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper

//#if MC >= 12001
import net.minecraft.client.gui.DrawContext
//#else
//$$ import net.minecraft.client.gui.DrawableHelper
//$$ import net.minecraft.client.util.math.MatrixStack
//$$ import net.minecraft.util.Identifier
//#endif

// TODO: custom ktlint import-ordering rule breaks here when combining these preproc blocks
//#if MC >= 12001
typealias Context = DrawContext
//#else
//$$ typealias Context = MatrixStack
//$$ fun Context.drawTexture(
//$$     texture: Identifier,
//$$     x: Int,
//$$     y: Int,
//$$     u: Int,
//$$     v: Int,
//$$     width: Int,
//$$     height: Int,
//$$ ) {
//$$     RenderSystem.setShaderTexture(0, texture)
//$$     DrawableHelper.drawTexture(this, x, y, u, v, width, height)
//$$ }
//#endif

@Environment(EnvType.CLIENT)
class BigAugmentedScreen(
    handler: BigAugmentedScreenHandler,
    inventory: PlayerInventory?,
    title: Text?,
) :
    HandledScreen<BigAugmentedScreenHandler>(handler, inventory, title) {
    private val screenModel = BigAugmentedScreenModel(handler.augmentLevel, handler.augmentLevel2)
    private val cols = handler.cols
    private val rows = handler.rows

    private var scrollPosition = 0f
    private var scrolling = false

    private val hasScrollbar by handler::hasScrollbar

    init {
        backgroundWidth = PADDING_LEFT + cols * SLOT_SIZE + PADDING_RIGHT
        if (hasScrollbar) {
            backgroundWidth += GAP_BETWEEN_CONTAINER_INVENTORY_AND_SCROLLBAR + SCROLLBAR_BG_WIDTH
        }

        backgroundHeight = PADDING_TOP + rows * SLOT_SIZE + GAP_BETWEEN_CONTAINER_INVENTORY_AND_PLAYER_INVENTORY + 3 * SLOT_SIZE + GAP_BETWEEN_PLAYER_INVENTORY_AND_HOTBAR + 1 * SLOT_SIZE + PADDING_BOTTOM
        titleX = PADDING_LEFT + 1
        titleY = PADDING_TOP - TEXT_LINE_HEIGHT
        playerInventoryTitleX = PADDING_LEFT + (cols - SINGLE_SCREEN_DEFAULT_COLS) * SLOT_SIZE / 2 + 1
        playerInventoryTitleY = backgroundHeight - (TEXT_LINE_HEIGHT + 3 * SLOT_SIZE + GAP_BETWEEN_PLAYER_INVENTORY_AND_HOTBAR + 1 * SLOT_SIZE + PADDING_BOTTOM)
    }

    override fun render(context: Context?, mouseX: Int, mouseY: Int, delta: Float) {
        //#if MC >= 12002
        renderBackground(context, mouseX, mouseY, delta)
        //#else
        //$$ renderBackground(context)
        //#endif
        super.render(context, mouseX, mouseY, delta)
        drawMouseoverTooltip(context, mouseX, mouseY)
    }

    override fun drawBackground(context: Context, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)

        drawBackgroundTexture(context)
        drawSlotTexture(context)
        if (hasScrollbar) drawScrollbarTexture(context)
    }

    private fun drawBackgroundTexture(context: Context) {
        val hnum = (backgroundWidth - BG_CORNER * 2) / (BG_WIDTH - BG_CORNER * 2)
        val hrem = (backgroundWidth - BG_CORNER * 2) % (BG_WIDTH - BG_CORNER * 2)

        val vnum = (backgroundHeight - BG_CORNER * 2) / (BG_HEIGHT - BG_CORNER * 2)
        val vrem = (backgroundHeight - BG_CORNER * 2) % (BG_HEIGHT - BG_CORNER * 2)

        //// corners ////
        // top-left
        context.drawTexture(
            BG_TEXTURE,
            x,
            y,
            BG_X,
            BG_Y,
            BG_CORNER,
            BG_CORNER,
        )
        // top-right
        context.drawTexture(
            BG_TEXTURE,
            x + backgroundWidth - BG_CORNER,
            y,
            BG_WIDTH - BG_CORNER,
            BG_Y,
            BG_CORNER,
            BG_CORNER,
        )
        // bottom-left
        context.drawTexture(
            BG_TEXTURE,
            x,
            y + backgroundHeight - BG_CORNER,
            BG_X,
            BG_HEIGHT - BG_CORNER,
            BG_CORNER,
            BG_CORNER,
        )
        // bottom-right
        context.drawTexture(
            BG_TEXTURE,
            x + backgroundWidth - BG_CORNER,
            y + backgroundHeight - BG_CORNER,
            BG_WIDTH - BG_CORNER,
            BG_HEIGHT - BG_CORNER,
            BG_CORNER,
            BG_CORNER,
        )

        //// edges ////
        for (hcnt in 0 until hnum) {
            // top
            context.drawTexture(
                BG_TEXTURE,
                x + BG_CORNER + hcnt * (BG_WIDTH - BG_CORNER * 2),
                y,
                BG_CORNER,
                BG_Y,
                BG_WIDTH - BG_CORNER * 2,
                BG_CORNER,
            )
            // bottom
            context.drawTexture(
                BG_TEXTURE,
                x + BG_CORNER + hcnt * (BG_WIDTH - BG_CORNER * 2),
                y + backgroundHeight - BG_CORNER,
                BG_CORNER,
                BG_HEIGHT - BG_CORNER,
                BG_WIDTH - BG_CORNER * 2,
                BG_CORNER,
            )
        }
        for (vcnt in 0 until vnum) {
            // left
            context.drawTexture(
                BG_TEXTURE,
                x,
                y + BG_CORNER + vcnt * (BG_HEIGHT - BG_CORNER * 2),
                BG_X,
                BG_CORNER,
                BG_CORNER,
                BG_HEIGHT - BG_CORNER * 2,
            )
            // bottom
            context.drawTexture(
                BG_TEXTURE,
                x + backgroundWidth - BG_CORNER,
                y + BG_CORNER + vcnt * (BG_HEIGHT - BG_CORNER * 2),
                BG_WIDTH - BG_CORNER,
                BG_CORNER,
                BG_CORNER,
                BG_HEIGHT - BG_CORNER * 2,
            )
        }
        // top
        context.drawTexture(
            BG_TEXTURE,
            x + BG_CORNER + hnum * (BG_WIDTH - BG_CORNER * 2),
            y,
            BG_CORNER,
            BG_Y,
            hrem,
            BG_CORNER,
        )
        // bottom
        context.drawTexture(
            BG_TEXTURE,
            x + BG_CORNER + hnum * (BG_WIDTH - BG_CORNER * 2),
            y + backgroundHeight - BG_CORNER,
            BG_CORNER,
            BG_HEIGHT - BG_CORNER,
            hrem,
            BG_CORNER,
        )
        // left
        context.drawTexture(
            BG_TEXTURE,
            x,
            y + BG_CORNER + vnum * (BG_HEIGHT - BG_CORNER * 2),
            BG_X,
            BG_CORNER,
            BG_CORNER,
            vrem,
        )
        // bottom
        context.drawTexture(
            BG_TEXTURE,
            x + backgroundWidth - BG_CORNER,
            y + BG_CORNER + vnum * (BG_HEIGHT - BG_CORNER * 2),
            BG_WIDTH - BG_CORNER,
            BG_CORNER,
            BG_CORNER,
            vrem,
        )

        //// area ////
        for (vcnt in 0 until vnum) {
            for (hcnt in 0 until hnum) {
                context.drawTexture(
                    BG_TEXTURE,
                    x + BG_CORNER + hcnt * (BG_WIDTH - BG_CORNER * 2),
                    y + BG_CORNER + vcnt * (BG_HEIGHT - BG_CORNER * 2),
                    BG_CORNER,
                    BG_CORNER,
                    BG_WIDTH - BG_CORNER * 2,
                    BG_HEIGHT - BG_CORNER * 2,
                )
            }
            context.drawTexture(
                BG_TEXTURE,
                x + BG_CORNER + hnum * (BG_WIDTH - BG_CORNER * 2),
                y + BG_CORNER + vcnt * (BG_HEIGHT - BG_CORNER * 2),
                BG_CORNER,
                BG_CORNER,
                hrem,
                BG_HEIGHT - BG_CORNER * 2,
            )
        }
        for (hcnt in 0 until hnum) {
            context.drawTexture(
                BG_TEXTURE,
                x + BG_CORNER + hcnt * (BG_WIDTH - BG_CORNER * 2),
                y + BG_CORNER + vnum * (BG_HEIGHT - BG_CORNER * 2),
                BG_CORNER,
                BG_CORNER,
                BG_WIDTH - BG_CORNER * 2,
                vrem,
            )
        }
        context.drawTexture(
            BG_TEXTURE,
            x + BG_CORNER + hnum * (BG_WIDTH - BG_CORNER * 2),
            y + BG_CORNER + vnum * (BG_HEIGHT - BG_CORNER * 2),
            BG_CORNER,
            BG_CORNER,
            hrem,
            vrem,
        )
    }

    private fun drawSlotTexture(context: Context) {
        //// container inventory ////
        val containerInventoryPoint = screenModel.containerInventoryPoint

        val hnum = cols / CONTAINER_INVENTORY_COLS
        val hrem = (cols % CONTAINER_INVENTORY_COLS) * SLOT_SIZE

        val vnum = rows / CONTAINER_INVENTORY_ROWS
        val vrem = (rows % CONTAINER_INVENTORY_ROWS) * SLOT_SIZE

        for (vcnt in 0 until vnum) {
            for (hcnt in 0 until hnum) {
                context.drawTexture(
                    CONTAINER_TEXTURE,
                    x + containerInventoryPoint.x + hcnt * CONTAINER_INVENTORY_COLS * SLOT_SIZE,
                    y + containerInventoryPoint.y + vcnt * CONTAINER_INVENTORY_ROWS * SLOT_SIZE,
                    CONTAINER_INVENTORY_X,
                    CONTAINER_INVENTORY_Y,
                    CONTAINER_INVENTORY_COLS * SLOT_SIZE,
                    CONTAINER_INVENTORY_ROWS * SLOT_SIZE,
                )
            }
            context.drawTexture(
                CONTAINER_TEXTURE,
                x + containerInventoryPoint.x + hnum * CONTAINER_INVENTORY_COLS * SLOT_SIZE,
                y + containerInventoryPoint.y + vcnt * CONTAINER_INVENTORY_ROWS * SLOT_SIZE,
                CONTAINER_INVENTORY_X,
                CONTAINER_INVENTORY_Y,
                hrem,
                CONTAINER_INVENTORY_ROWS * SLOT_SIZE,
            )
        }
        for (hcnt in 0 until hnum) {
            context.drawTexture(
                CONTAINER_TEXTURE,
                x + containerInventoryPoint.x + hcnt * CONTAINER_INVENTORY_COLS * SLOT_SIZE,
                y + containerInventoryPoint.y + vnum * CONTAINER_INVENTORY_ROWS * SLOT_SIZE,
                CONTAINER_INVENTORY_X,
                CONTAINER_INVENTORY_Y,
                CONTAINER_INVENTORY_COLS * SLOT_SIZE,
                vrem,
            )
        }
        context.drawTexture(
            CONTAINER_TEXTURE,
            x + containerInventoryPoint.x + hnum * CONTAINER_INVENTORY_COLS * SLOT_SIZE,
            y + containerInventoryPoint.y + vnum * CONTAINER_INVENTORY_ROWS * SLOT_SIZE,
            CONTAINER_INVENTORY_X,
            CONTAINER_INVENTORY_Y,
            hrem,
            vrem,
        )

        //// player inventory ////
        val playerInventoryPoint = screenModel.playerInventoryPoint

        context.drawTexture(
            CONTAINER_TEXTURE,
            x + playerInventoryPoint.x,
            y + playerInventoryPoint.y,
            PLAYER_INVENTORY_X,
            PLAYER_INVENTORY_Y,
            PLAYER_INVENTORY_WIDTH,
            PLAYER_INVENTORY_HEIGHT,
        )
    }

    private fun drawScrollbarTexture(context: Context) {
        //// background ////
        val vnum = (rows * SLOT_SIZE - 2) / (SCROLLBAR_BG_HEIGHT - 2)
        val vrem = (rows * SLOT_SIZE - 2) % (SCROLLBAR_BG_HEIGHT - 2)

        context.drawTexture(
            SCROLLBAR_BG_TEXTURE,
            x + backgroundWidth - (SCROLLBAR_BG_WIDTH + PADDING_RIGHT),
            y + PADDING_TOP,
            SCROLLBAR_BG_X,
            SCROLLBAR_BG_Y,
            SCROLLBAR_BG_WIDTH,
            1,
        )
        for (vcnt in 0 until vnum) {
            context.drawTexture(
                SCROLLBAR_BG_TEXTURE,
                x + backgroundWidth - (SCROLLBAR_BG_WIDTH + PADDING_RIGHT),
                y + PADDING_TOP + 1 + vcnt * (SCROLLBAR_BG_HEIGHT - 2),
                SCROLLBAR_BG_X,
                SCROLLBAR_BG_Y + 1,
                SCROLLBAR_BG_WIDTH,
                SCROLLBAR_BG_HEIGHT - 2,
            )
        }
        context.drawTexture(
            SCROLLBAR_BG_TEXTURE,
            x + backgroundWidth - (SCROLLBAR_BG_WIDTH + PADDING_RIGHT),
            y + PADDING_TOP + 1 + vnum * (SCROLLBAR_BG_HEIGHT - 2),
            SCROLLBAR_BG_X,
            SCROLLBAR_BG_Y + 1,
            SCROLLBAR_BG_WIDTH,
            vrem,
        )
        context.drawTexture(
            SCROLLBAR_BG_TEXTURE,
            x + backgroundWidth - (SCROLLBAR_BG_WIDTH + PADDING_RIGHT),
            y + PADDING_TOP + rows * SLOT_SIZE - 1,
            SCROLLBAR_BG_X,
            SCROLLBAR_BG_Y + SCROLLBAR_BG_HEIGHT - 1,
            SCROLLBAR_BG_WIDTH,
            1,
        )

        //// scroller ////
        val ymin = y + PADDING_TOP + 1
        val ymax = ymin + rows * SLOT_SIZE
        //#if MC >= 12002
        val identifier = if (hasScrollbar) SCROLLER_TEXTURE else SCROLLER_DISABLED_TEXTURE
        context.drawGuiTexture(
            identifier,
            x + PADDING_LEFT + cols * SLOT_SIZE + GAP_BETWEEN_CONTAINER_INVENTORY_AND_SCROLLBAR + 1,
            ymin + ((ymax - ymin - (SCROLLER_HEIGHT + 2)).toFloat() * scrollPosition).toInt(),
            SCROLLER_WIDTH,
            SCROLLER_HEIGHT,
        )
        //#else
        //$$ context.drawTexture(
        //$$     SCROLLBAR_TEXTURE,
        //$$     x + PADDING_LEFT + cols * SLOT_SIZE + GAP_BETWEEN_CONTAINER_INVENTORY_AND_SCROLLBAR + 1,
        //$$     ymin + ((ymax - ymin - (SCROLLER_HEIGHT + 2)).toFloat() * scrollPosition).toInt(),
        //$$     SCROLLBAR_X + if (hasScrollbar) 0 else SCROLLER_WIDTH,
        //$$     SCROLLBAR_Y,
        //$$     SCROLLER_WIDTH,
        //$$     SCROLLER_HEIGHT,
        //$$ )
        //#endif
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (hasScrollbar && button == 0) {
            if (isClickInScollbar(mouseX, mouseY)) {
                scrolling = hasScrollbar
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (hasScrollbar && button == 0) {
            scrolling = false
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        //#if MC >= 12002
        horizontalAmount: Double,
        //#endif
        verticalAmount: Double,
    ): Boolean {
        if (!hasScrollbar) return false
        val i = MathHelper.ceilDiv(handler.inventory.size(), SCROLL_SCREEN_COLS) - ClientConfig.scrollScreenRows
        val f = (verticalAmount / i.toDouble()).toFloat()
        scrollPosition = (scrollPosition - f).coerceIn(0f..1f)
        handler.scrollItems(scrollPosition)
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (hasScrollbar && scrolling) {
            val i = y + PADDING_TOP + 1
            val j = i + rows * SLOT_SIZE
            scrollPosition = (
                (mouseY.toFloat() - i.toFloat() - SCROLLER_HEIGHT.toFloat() / 2f)
                    / ((j - i).toFloat() - SCROLLER_HEIGHT.toFloat())
                ).coerceIn(0f..1f)
            handler.scrollItems(scrollPosition)
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    private fun isClickInScollbar(mouseX: Double, mouseY: Double): Boolean {
        val i = x + PADDING_LEFT + cols * SLOT_SIZE + GAP_BETWEEN_CONTAINER_INVENTORY_AND_SCROLLBAR + 1
        val j = y + PADDING_TOP + 1
        val k = i + SCROLLER_WIDTH + 1
        val l = j + rows * SLOT_SIZE
        return mouseX >= i.toDouble() && mouseY >= j.toDouble() && mouseX < k.toDouble() && mouseY < l.toDouble()
    }
}
