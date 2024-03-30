package de.rubixdev.enchantedshulkers.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.block.entity.ChestBlockEntity$1")
public class ChestBlockEntity_ViewerCountManagerMixin {
    @Shadow
    @Final
    ChestBlockEntity field_27211;

    @ModifyReturnValue(method = "isPlayerViewing", at = @At("RETURN"))
    private boolean isPlayerViewing(boolean original, PlayerEntity player) {
        if (original) return true;
        Inventory inventory = Utils.getInventory(player.currentScreenHandler);
        return inventory == field_27211
            || inventory instanceof DoubleInventory doubleInv && doubleInv.isPart(field_27211);
    }
}
