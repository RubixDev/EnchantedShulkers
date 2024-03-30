package de.rubixdev.enchantedshulkers.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.block.entity.BarrelBlockEntity$1")
public class BarrelBlockEntity_ViewerCountManagerMixin {
    @Shadow
    @Final
    BarrelBlockEntity field_27208;

    @ModifyReturnValue(method = "isPlayerViewing", at = @At("RETURN"))
    private boolean isPlayerViewing(boolean original, PlayerEntity player) {
        return original || Utils.getInventory(player.currentScreenHandler) == field_27208;
    }
}
