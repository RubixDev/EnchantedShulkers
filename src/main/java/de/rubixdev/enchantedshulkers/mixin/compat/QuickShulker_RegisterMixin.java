package de.rubixdev.enchantedshulkers.mixin.compat;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.screen.AugmentedShulkerBoxScreenHandler;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Restriction(require = @Condition("quickshulker"))
@Mixin(QuickShulkerMod.class)
public class QuickShulker_RegisterMixin {
    @Inject(
            method = "lambda$registerProviders$1",
            at = @At("RETURN"),
            cancellable = true)
    private static void augmentInvSize(PlayerEntity player, ItemStack stack, int i, PlayerInventory playerInventory, PlayerEntity playerEntity, CallbackInfoReturnable<ScreenHandler> cir) {
        cir.setReturnValue(AugmentedShulkerBoxScreenHandler.create(
                i, playerInventory,
                ShulkerUtils.getInventoryFromShulker(stack),
                EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, stack))
        );
    }
}
