package de.rubixdev.enchantedshulkers.mixin.compat;

import com.illusivesoulworks.shulkerboxslot.ShulkerBoxAccessoryInventory;
import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.screen.AugmentedShulkerBoxScreenHandler;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxAccessoryInventory.class)
public class ShulkerBoxSlot_ShulkerBoxAccessoryInventoryMixin {
    @Shadow @Final private ItemStack shulkerBox;

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 27), require = 1)
    private int augmentInvSize(int constant, ItemStack shulkerBox) {
        int level = EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, shulkerBox);
        return level == 0 ? constant : 9 * Utils.getInvRows(level);
    }

    @Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
    private void augmentedScreenHandler(int i, PlayerInventory playerInventory, PlayerEntity playerEntity, CallbackInfoReturnable<ScreenHandler> cir) {
        int level = EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, this.shulkerBox);
        if (level != 0) {
            cir.setReturnValue(new AugmentedShulkerBoxScreenHandler(i, playerInventory, (Inventory) this, level));
        }
    }
}
