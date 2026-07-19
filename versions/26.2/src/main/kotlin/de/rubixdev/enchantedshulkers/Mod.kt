package de.rubixdev.enchantedshulkers

import net.fabricmc.api.ModInitializer
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 26.2 port - core pass: enchantment registry keys + item tags only.
 *
 * The five custom enchantments (Siphon/Refill/Vacuum/Void/Augment) are no longer Java objects
 * registered here: `Enchantment` is a non-subclassable `final record` in 26.2, so they are now
 * datapack-defined at `data/enchantedshulkers/enchantment/<name>.json` and resolved at the point of use
 * via [ResourceKey] + the world's registry access (see `Utils.enchantmentHolder`). This also
 * removes the old Fabric-ASM `ClassTinkerers` enum-injection machinery (former EnumInjector.kt,
 * asm/PortableContainerTarget.kt, asm/AugmentableContainerTarget.kt, EnchantmentTargetMixin.java)
 * that added PORTABLE_CONTAINER/AUGMENTABLE_CONTAINER constants to the old `EnchantmentTarget`
 * enum - 26.2's `EnchantmentTarget` is now an unrelated, small combat-effect-targeting enum, and
 * item applicability is expressed via `supported_items` tag predicates in the enchantment JSON
 * instead.
 *
 * TODO(26.2): the rest of the original Mod.onInitialize() (config load/sync, packet listeners,
 * screen handler type registration, config command, optional resource-pack registration) depends
 * on subsystems (config/, screen/, networking) not yet ported for 26.2.
 */
object Mod : ModInitializer {
    const val MOD_ID = "enchantedshulkers"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    @JvmField val PORTABLE_CONTAINER_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, "portable_container".id)
    @JvmField val AUGMENTABLE_CONTAINER_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, "augmentable_container".id)

    @JvmField val SIPHON_KEY: ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, "siphon".id)
    @JvmField val REFILL_KEY: ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, "refill".id)
    @JvmField val VACUUM_KEY: ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, "vacuum".id)
    @JvmField val VOID_KEY: ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, "void".id)
    @JvmField val AUGMENT_KEY: ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, "augment".id)

    val String.id: Identifier get() = Identifier.fromNamespaceAndPath(MOD_ID, this)

    override fun onInitialize() {
        LOGGER.info("EnchantedShulkers 26.2 (core port: enchantment registry + container storage) loaded")
    }
}
