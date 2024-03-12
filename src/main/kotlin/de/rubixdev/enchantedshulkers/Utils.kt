package de.rubixdev.enchantedshulkers

import atonkish.reinfcore.screen.ReinforcedStorageScreenHandler
import atonkish.reinfshulker.block.ReinforcedShulkerBoxBlock
import com.glisco.things.Things
import com.glisco.things.items.ThingsItems
import com.illusivesoulworks.shulkerboxslot.ShulkerBoxAccessoryInventory
import com.illusivesoulworks.shulkerboxslot.platform.Services
import cursedflames.splitshulkers.SplitShulkerBoxBlockEntity
import de.rubixdev.enchantedshulkers.config.WorldConfig
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity
import de.rubixdev.enchantedshulkers.mixin.compat.QuickShulker_ItemStackInventoryAccessor
import de.rubixdev.enchantedshulkers.mixin.compat.ShulkerBoxSlot_ShulkerBoxAccessoryInventoryAccessor
import de.rubixdev.enchantedshulkers.screen.AugmentedShulkerBoxScreenHandler
import de.rubixdev.enchantedshulkers.screen.BigAugmentedScreenHandler
import de.rubixdev.enchantedshulkers.screen.VanillaBigAugmentedGui
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler
import net.fabricmc.loader.api.FabricLoader
import net.kyrptonaught.shulkerutils.ItemStackInventory
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.ShulkerBoxBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.ShulkerBoxBlockEntity
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registries
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ShulkerBoxScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper

//#if MC >= 12001
import net.minecraft.nbt.NbtInt
import megaminds.clickopener.api.BlockEntityInventory
//#else
//$$ import megaminds.clickopener.api.ShulkerInventory
//#endif

//#if MC >= 12002
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking
//#else
//$$ import eu.pb4.polymer.networking.api.PolymerServerNetworking
//#endif

object Utils {
    @JvmStatic
    fun canEnchant(item: Item?) = Registries.ITEM.getEntry(item).isIn(Mod.PORTABLE_CONTAINER_TAG)

    @JvmStatic
    fun canEnchant(stack: ItemStack) = stack.isIn(Mod.PORTABLE_CONTAINER_TAG)

    @JvmStatic
    fun canAugment(item: Item?) = Registries.ITEM.getEntry(item).isIn(Mod.AUGMENTABLE_CONTAINER_TAG)

    @JvmStatic
    fun canAugment(stack: ItemStack) = stack.isIn(Mod.AUGMENTABLE_CONTAINER_TAG)

    @JvmStatic
    fun ItemStack.isShulkerBox() = (this.item as? BlockItem)?.block is ShulkerBoxBlock

    private fun ItemStack.isEnderChest() = this.isOf(Items.ENDER_CHEST) || (FabricLoader.getInstance().isModLoaded("things") && this.isOf(ThingsItems.ENDER_POUCH))

    @JvmStatic
    fun ServerPlayerEntity?.clientModVersion(): Int {
        //#if MC >= 12001
        return PolymerServerNetworking.getMetadata(this?.networkHandler ?: return 0, Mod.HANDSHAKE_PACKET_ID, NbtInt.TYPE)?.intValue() ?: 0
        //#else
        //$$ return PolymerServerNetworking.getSupportedVersion(this?.networkHandler ?: return 0, Mod.HANDSHAKE_PACKET_ID)
        //#endif
    }

    @JvmStatic
    fun getContainers(player: ServerPlayerEntity, enchantment: Enchantment): List<ItemStack> {
        val playerInventory = mutableListOf<ItemStack>()
        for (i in 0 until player.inventory.size()) {
            playerInventory.add(player.inventory.getStack(i))
        }
        if (FabricLoader.getInstance().isModLoaded("shulkerboxslot")) {
            // include the slot from Shulker Box Slot
            Services.INSTANCE.findShulkerBoxAccessory(player).ifPresent { playerInventory.add(it.left) }
        }
        if (FabricLoader.getInstance().isModLoaded("things")) {
            // include Ender Pouch in belt slot from Things
            Things.getTrinkets(player).getEquipped { it.isEnderChest() }.forEach { playerInventory.add(it.right) }
        }
        return getContainers(playerInventory, player, enchantment)
    }

    @JvmStatic
    fun getContainers(
        inventory: List<ItemStack>,
        player: ServerPlayerEntity,
        enchantment: Enchantment,
        recursionDepth: Int = 0,
        visitedEnderChest: Boolean = false,
    ): List<ItemStack> {
        val out = mutableListOf<ItemStack>()
        for (stack in inventory) {
            // TODO: technically a vacuum shulker box inside a siphon ender chest should also be returned here,
            // but unless someone complains i can't be bothered :P
            if (canEnchant(stack) && EnchantmentHelper.getLevel(
                    enchantment,
                    stack,
                ) > 0 && !(visitedEnderChest && stack.isEnderChest())
                // in case some other mod allows shulkers to stack, ignore them to prevent duping
                && (stack.isEnderChest() || stack.count == 1)
            ) {
                out.add(stack)
                if (recursionDepth < WorldConfig.nestedContainers) {
                    out.addAll(
                        getContainers(
                            getContainerInventory(stack, player),
                            player,
                            enchantment,
                            recursionDepth + 1,
                            visitedEnderChest || stack.isEnderChest(),
                        ),
                    )
                }
            }
        }
        return out
    }

    @JvmStatic
    fun ScreenHandler.getInventory(): Inventory? = when {
        this is ShulkerBoxScreenHandler -> this.inventory
        this is AugmentedShulkerBoxScreenHandler -> this.inventory
        this is BigAugmentedScreenHandler -> this.inventory
        this is VirtualScreenHandler -> (this.gui as? VanillaBigAugmentedGui)?.shulkerInventory
        FabricLoader.getInstance().isModLoaded("reinfshulker") && this is ReinforcedStorageScreenHandler -> this.inventory
        else -> null
    }

    @JvmStatic
    fun getContainerInventory(container: ItemStack, player: ServerPlayerEntity): DefaultedList<ItemStack> {
        if (container.isEnderChest()) {
            return player.enderChestInventory.heldStacks
        }
        val screenHandlerInv = player.currentScreenHandler.getInventory()
        if (FabricLoader.getInstance().isModLoaded("quickshulker")) {
            if (screenHandlerInv is ItemStackInventory
                && (screenHandlerInv as QuickShulker_ItemStackInventoryAccessor).itemStack === container
            ) {
                return screenHandlerInv.heldStacks
            }
            if (FabricLoader.getInstance().isModLoaded("reinfshulker")
                && screenHandlerInv is ItemStackInventory
                && (screenHandlerInv as QuickShulker_ItemStackInventoryAccessor).itemStack === container
            ) {
                return screenHandlerInv.heldStacks
            }
        }
        if (FabricLoader.getInstance().isModLoaded("shulkerboxslot")
            && screenHandlerInv is ShulkerBoxAccessoryInventory
            && (screenHandlerInv as ShulkerBoxSlot_ShulkerBoxAccessoryInventoryAccessor).shulkerBox === container
        ) {
            return (screenHandlerInv as ShulkerBoxSlot_ShulkerBoxAccessoryInventoryAccessor).items
        }
        //#if MC >= 12001
        if (FabricLoader.getInstance().isModLoaded("clickopener")
            && screenHandlerInv is BlockEntityInventory
            && screenHandlerInv.link === container
        ) {
            return screenHandlerInv.inventory
        }
        //#else
        //$$ if (FabricLoader.getInstance().isModLoaded("clickopener")
        //$$     && screenHandlerInv is ShulkerInventory
        //$$     && screenHandlerInv.link === container
        //$$ ) {
        //$$     return screenHandlerInv.inventory
        //$$ }
        //#endif

        return getContainerInventory(container)
    }

    @JvmStatic
    fun getContainerInventory(container: ItemStack): DefaultedList<ItemStack> {
        var size = 9 * getInvRows(EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, container))
        if (FabricLoader.getInstance().isModLoaded("reinfshulker")) {
            size = ((container.item as? BlockItem)?.block as? ReinforcedShulkerBoxBlock)?.material?.size ?: size
        }
        val inventory = DefaultedList.ofSize(size, ItemStack.EMPTY)

        val nbt = container.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY)
        if (nbt != null && nbt.contains("Items", NbtElement.LIST_TYPE.toInt())) {
            Inventories.readNbt(nbt, inventory)
        }
        return inventory
    }

    @JvmStatic
    fun setContainerInventory(container: ItemStack, inventory: DefaultedList<ItemStack>) {
        // no need to write any NBT on ender chests
        if (container.isEnderChest()) return

        val nbt = container.getOrCreateSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY)
        Inventories.writeNbt(nbt, inventory)
    }

    @JvmStatic
    fun <T : BlockEntity> T.shouldGlint() =
        this is EnchantableBlockEntity && !this.`enchantedShulkers$getEnchantments`().isEmpty()

    // for compat with Split Shulker Boxes
    @JvmStatic
    fun ShulkerBoxBlockEntity.hasTwoColors(): Boolean {
        if (!FabricLoader.getInstance().isModLoaded("splitshulkers")) return false
        val splitShulker = this as SplitShulkerBoxBlockEntity
        return splitShulker.color != splitShulker.splitshulkers_getSecondaryColor()
    }

    @JvmStatic
    fun getLevelFromNbt(enchantment: Enchantment, nbt: NbtList): Int =
        EnchantmentHelper.fromNbt(nbt).getOrDefault(enchantment, 0)

    @JvmStatic
    fun getInvRows(augmentLevel: Int): Int =
        MathHelper.clamp(augmentLevel + 3, 3, 3 + WorldConfig.maxAugmentLevel)

    @JvmStatic
    fun ItemStack.getDisplayName(): Text =
        (
            ((this.item as? BlockItem)?.block as? BlockWithEntity)?.createBlockEntity(
                BlockPos.ORIGIN,
                null,
            ) as? NamedScreenHandlerFactory
            )?.displayName ?: this.name

    @JvmStatic
    fun ItemStack.getShulkerColor(): DyeColor? =
        ((this.item as? BlockItem)?.block as? ShulkerBoxBlock)?.color

    @JvmStatic
    val String.id get() = Identifier(Mod.MOD_ID, this)
}
