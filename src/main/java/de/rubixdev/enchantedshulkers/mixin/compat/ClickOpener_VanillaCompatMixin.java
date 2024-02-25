package de.rubixdev.enchantedshulkers.mixin.compat;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.screen.AugmentedShulkerBoxScreenHandler;
import megaminds.clickopener.api.BlockEntityInventory;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "megaminds/clickopener/compat/VanillaCompat$3")
public class ClickOpener_VanillaCompatMixin {
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = "lambda$createFactory$0",
            at = @At("RETURN"),
            cancellable = true)
    private static void augmentedScreenHandler(ItemStack stack, int syncId, PlayerInventory playerInventory, PlayerEntity player, CallbackInfoReturnable<ScreenHandler> cir) {
        int level = EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, stack);
        if (level != 0) {
            cir.setReturnValue(new AugmentedShulkerBoxScreenHandler(
                    syncId, playerInventory,
                    new BlockEntityInventory(stack, 9 * Utils.getInvRows(level), BlockEntityType.SHULKER_BOX),
                    level
            ));
        }
    }
}
