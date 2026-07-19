package de.rubixdev.enchantedshulkers

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * TOOLCHAIN SCAFFOLD ONLY - this is NOT the ported mod.
 *
 * This proves the 26.2 build (JDK 25 + fabric-loom 1.17 + official Mojang mappings +
 * fabric-language-kotlin) resolves and compiles/assembles correctly end to end. The real
 * `Mod.kt`/`ClientMod.kt` and all gameplay logic (enchantments, mixins, screens) have NOT been
 * ported - see the port-26.2 branch's commit message / the implementer's final report for the
 * two verified architectural blockers that need a design decision before that work can start:
 *
 *   1. `net.minecraft.world.item.enchantment.Enchantment` is now a `final` `Record` in 26.2 - it
 *      can no longer be subclassed the way this mod's six custom enchantments
 *      (Siphon/Refill/Vacuum/Void/Augment/ContainerEnchantment) currently are. Custom
 *      enchantments must now be defined as datapack JSON (`data/<ns>/enchantment/<name>.json`) with
 *      `supported_items`/`primary_items` item-tag predicates, looked up at runtime via
 *      `Holder<Enchantment>`/`ResourceKey<Enchantment>` instead of a Java singleton object. This
 *      also replaces the old `EnchantmentTarget`-enum + Fabric-ASM `ClassTinkerers` enum
 *      injection this mod currently uses to define which items count as "portable container" /
 *      "augmentable container" (that mechanism is gone: 26.2's `EnchantmentTarget` is now a
 *      small 3-value enum for combat-effect targeting only, unrelated to item applicability).
 *
 *   2. `ItemStack` has no NBT API at all in 26.2 (no `getSubNbt`/`getOrCreateSubNbt`,
 *      `BlockItem.BLOCK_ENTITY_TAG_KEY` does not exist) - it's fully `DataComponent`-based now.
 *      This mod stores an entire nested inventory inside a single ItemStack's NBT
 *      (`Utils.getContainerInventory`/`setContainerInventory`), which is the core mechanism
 *      behind portable/augmentable containers. It needs to be rewritten against
 *      `net.minecraft.world.item.component.ItemContainerContents` (confirmed present in the
 *      26.2 jar), which has a different value shape (immutable list of stack+slot pairs, not an
 *      NBT compound), and every mixin that reads/writes container contents on an ItemStack needs
 *      to change accordingly.
 */
object Mod : ModInitializer {
    const val MOD_ID = "enchantedshulkers"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("EnchantedShulkers 26.2 toolchain scaffold loaded (core gameplay not yet ported)")
    }
}
