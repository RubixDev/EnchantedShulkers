package de.rubixdev.enchantedshulkers

import net.minecraft.core.Holder
import net.minecraft.core.NonNullList
import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.block.ShulkerBoxBlock

/**
 * 26.2 port - core pass. Trimmed to what the enchantment-registry + container-storage rewrite
 * needs. NOT ported (needs subsystems not yet present for 26.2 - config/, screen/, and the
 * compat-mod integrations): `getInventory` (ScreenHandler-dependent), `clientModVersion` (Polymer
 * networking), the Ender Chest / nested-container / cross-mod-compat branches of the original
 * `getContainers`/`getContainerInventory`, `hasTwoColors` (Split Shulkers compat),
 * `getDisplayName`/`getShulkerColor`/`getGlintVertexConsumer` (rendering, deferred with the
 * renderer mixins).
 */
object Utils {
    /** Resolves a custom enchantment's [Holder] from its [ResourceKey] via the given registry access. */
    @JvmStatic
    fun enchantmentHolder(registryAccess: RegistryAccess, key: ResourceKey<Enchantment>): Holder<Enchantment> =
        registryAccess.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key)

    @JvmStatic
    fun canEnchant(item: Item?): Boolean = item != null && item.builtInRegistryHolder().`is`(Mod.PORTABLE_CONTAINER_TAG)

    @JvmStatic
    fun canEnchant(stack: ItemStack): Boolean = stack.typeHolder().`is`(Mod.PORTABLE_CONTAINER_TAG)

    @JvmStatic
    fun canAugment(item: Item?): Boolean = item != null && item.builtInRegistryHolder().`is`(Mod.AUGMENTABLE_CONTAINER_TAG)

    @JvmStatic
    fun canAugment(stack: ItemStack): Boolean = stack.typeHolder().`is`(Mod.AUGMENTABLE_CONTAINER_TAG)

    @JvmStatic
    fun isShulkerBox(stack: ItemStack): Boolean = (stack.item as? BlockItem)?.block is ShulkerBoxBlock

    // TODO(26.2): the original clamps against `WorldConfig.maxAugmentLevel` (a runtime config
    // value); config/ isn't ported yet, so this takes it as a parameter with the original default
    // (5) until WorldConfig is ported.
    @JvmStatic
    @JvmOverloads
    fun getInvRows(augmentLevel: Int, maxAugmentLevel: Int = 5): Int =
        (augmentLevel + 3).coerceIn(3, 3 + maxAugmentLevel)

    /**
     * Reads a portable/augmentable container ItemStack's contents into a fresh, correctly-sized
     * [NonNullList]. Ported from NBT (`getSubNbt(BLOCK_ENTITY_TAG_KEY)` + `Inventories.readNbt`)
     * onto [net.minecraft.world.item.component.ItemContainerContents]
     * (`DataComponents.CONTAINER`), which ItemStack no longer has any NBT API to reach at all in
     * 26.2. `ItemContainerContents` only remembers up to its last non-empty slot (it has no
     * fixed-size concept), so - same as the original code, which always recomputed the size from
     * the current Augment enchantment level rather than persisting it - the size here is always
     * recomputed from `augmentHolder`'s level and the stored contents are copied into a list of
     * that size, leaving any remaining slots empty.
     */
    @JvmStatic
    fun getContainerInventory(container: ItemStack, augmentHolder: Holder<Enchantment>): NonNullList<ItemStack> {
        val size = 9 * getInvRows(EnchantmentHelper.getItemEnchantmentLevel(augmentHolder, container))
        val inventory = NonNullList.withSize(size, ItemStack.EMPTY)
        container.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(inventory)
        return inventory
    }

    @JvmStatic
    fun setContainerInventory(container: ItemStack, inventory: NonNullList<ItemStack>) {
        container.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inventory))
    }

    /**
     * Finds portable containers with the given enchantment directly in `player`'s inventory.
     * TODO(26.2): the original also recurses into nested containers (up to
     * `WorldConfig.nestedContainers` deep), includes the player's Ender Chest, and integrates
     * Shulker Box Slot / Things belt-slot containers - all deferred pending config/ and (for the
     * two compat mods) their being dropped anyway (no 26.2 build - see the port report).
     */
    @JvmStatic
    fun getContainers(player: ServerPlayer, enchantmentHolder: Holder<Enchantment>): List<ItemStack> {
        val playerInventory = mutableListOf<ItemStack>()
        for (i in 0 until player.inventory.containerSize) {
            playerInventory.add(player.inventory.getItem(i))
        }
        return getContainers(playerInventory, enchantmentHolder)
    }

    @JvmStatic
    fun getContainers(inventory: List<ItemStack>, enchantmentHolder: Holder<Enchantment>): List<ItemStack> {
        val out = mutableListOf<ItemStack>()
        for (stack in inventory) {
            if (canEnchant(stack) &&
                EnchantmentHelper.getItemEnchantmentLevel(enchantmentHolder, stack) > 0 &&
                stack.count == 1
            ) {
                out.add(stack)
            }
        }
        return out
    }
}
