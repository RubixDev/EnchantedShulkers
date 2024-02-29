package de.rubixdev.enchantedshulkers.mixin.compat;

import de.maxhenkel.peek.events.TooltipImageEvents;
import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Restriction(require = @Condition("peek"))
@Mixin(TooltipImageEvents.class)
public class Peek_TooltipImageEventsMixin {
    @ModifyConstant(method = "getShulkerBoxTooltipImage", constant = @Constant(intValue = 27), require = 1)
    private static int augmentInvSize(int constant, ItemStack stack) {
        int level = EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, stack);
        return level == 0 ? constant : 9 * Utils.getInvRows(level);
    }

    @ModifyConstant(method = "getShulkerBoxTooltipImage", constant = @Constant(intValue = 3), require = 1)
    private static int augmentInvRows(int constant, ItemStack stack) {
        int level = EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, stack);
        return level == 0 ? constant : Utils.getInvRows(level);
    }
}
