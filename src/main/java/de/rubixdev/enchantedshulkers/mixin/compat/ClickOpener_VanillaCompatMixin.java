package de.rubixdev.enchantedshulkers.mixin.compat;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.screen.AugmentedShulkerBoxScreenHandler;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
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

//#if MC >= 12001
import megaminds.clickopener.api.BlockEntityInventory;
//#else
//$$ import megaminds.clickopener.api.ShulkerInventory;
//#endif

@Restriction(require = @Condition("clickopener"))
//#if MC >= 12001
@Mixin(targets = "megaminds/clickopener/compat/VanillaCompat$3")
//#else
//$$ @Mixin(megaminds.clickopener.compat.VanillaCompat.class)
//#endif
public class ClickOpener_VanillaCompatMixin {
    //#if MC >= 12001
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = "lambda$createFactory$0",
            at = @At("RETURN"),
            cancellable = true)
    private static void augmentedScreenHandler(ItemStack stack, int syncId, PlayerInventory playerInventory, PlayerEntity player, CallbackInfoReturnable<ScreenHandler> cir) {
        int level = EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, stack);
        if (level != 0) {
            cir.setReturnValue(AugmentedShulkerBoxScreenHandler.create(
                    syncId, playerInventory,
                    new BlockEntityInventory(stack, 9 * Utils.getInvRows(level), BlockEntityType.SHULKER_BOX),
                    level,
                    Utils.getDisplayName(stack)
            ));
        }
    }
    //#else
    //$$ @Inject(method = "lambda$init$7", at = @At("RETURN"), cancellable = true)
    //$$ private static void augmentedScreenHandler(ItemStack stack, int syncId, PlayerInventory playerInventory, PlayerEntity player, CallbackInfoReturnable<ScreenHandler> cir) {
    //$$     int level = EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, stack);
    //$$     if (level != 0) {
    //$$         cir.setReturnValue(AugmentedShulkerBoxScreenHandler.create(
    //$$                 syncId, playerInventory,
    //$$                 new ShulkerInventory(stack, 9 * Utils.getInvRows(level), BlockEntityType.SHULKER_BOX),
    //$$                 level,
    //$$                 Utils.getDisplayName(stack)
    //$$         ));
    //$$     }
    //$$ }
    //#endif
}
