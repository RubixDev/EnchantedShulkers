package de.rubixdev.enchantedshulkers.mixin.compat;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Restriction(require = @Condition("shulkerboxtooltip"))
//#if MC >= 12004
@SuppressWarnings("UnstableApiUsage")
@Mixin(com.misterpemodder.shulkerboxtooltip.impl.provider.BlockEntityAwarePreviewRenderer.class)
//#else
//$$ @Mixin(com.misterpemodder.shulkerboxtooltip.api.provider.BlockEntityPreviewProvider.class)
//#endif
public class ShulkerBoxTooltip_BlockEntityAwarePreviewProviderMixin {
    @Inject(method = "getInventoryMaxSize", at = @At("RETURN"), cancellable = true, remap = false)
    private void augmentInvSize(PreviewContext context, CallbackInfoReturnable<Integer> cir) {
        if (!Utils.canAugment(context.stack())) return;
        int level = EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, context.stack());
        if (level != 0) {
            cir.setReturnValue(9 * Utils.getInvRows(level));
        }
    }
}
