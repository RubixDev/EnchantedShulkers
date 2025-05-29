package de.rubixdev.enchantedshulkers.enchantment

import de.rubixdev.enchantedshulkers.Utils.clientModVersion
import eu.pb4.polymer.core.api.other.PolymerEnchantment
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EquipmentSlot
import net.minecraft.server.network.ServerPlayerEntity

//#if MC >= 12006
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey
//#else
//$$ import net.minecraft.enchantment.EnchantmentTarget
//#endif

//#if MC >= 12006
abstract class ContainerEnchantment(supportedItems: TagKey<Item>, maxLevel: Int = 1) :
    Enchantment(
        properties(
            supportedItems,
            2,
            maxLevel,
            leveledCost(10, 10),
            leveledCost(16, 10),
            4,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND,
        ),
    ),
//#else
//$$ abstract class ContainerEnchantment(target: EnchantmentTarget) :
//$$     Enchantment(Rarity.RARE, target, arrayOf(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)),
//#endif
    PolymerEnchantment {
    protected abstract fun generate(): Boolean

    override fun isTreasure() = true
    override fun isAvailableForEnchantedBookOffer() = generate()
    override fun isAvailableForRandomSelection() = generate()

    override fun getPolymerReplacement(player: ServerPlayerEntity?): Enchantment? {
        // clients with this mod can understand the enchantments
        if (player.clientModVersion() > 0) return this
        // clients without this mod have it handled by Polymer
        return super.getPolymerReplacement(player)
    }
}
