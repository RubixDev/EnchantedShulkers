package de.rubixdev.enchantedshulkers.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;

//#if MC >= 12001
import de.rubixdev.enchantedshulkers.enchantment.RefillEnchantment;
import de.rubixdev.enchantedshulkers.interfaces.ProjectileHolder;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.enderitemc.enderitemod.tools.EnderiteBow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Restriction(require = @Condition("enderitemod"))
@Mixin(EnderiteBow.class)
public class Enderite_EnderiteBowMixin {
    @Inject(
        method = "onStoppedUsing",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V", shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void refillProjectiles(
        ItemStack stack,
        World world,
        LivingEntity user,
        int remainingUseTicks,
        CallbackInfo ci,
        PlayerEntity playerEntity,
        boolean bl,
        ItemStack itemStack
    ) {
        if (!(playerEntity instanceof ServerPlayerEntity player)) return;
        Integer slot = ((ProjectileHolder) player).enchantedShulkers$getProjectileSlot(itemStack);
        if (
            slot != null
                && RefillEnchantment
                    .refill(player, slot, ((ProjectileHolder) player).enchantedShulkers$getProjectileType(), 1)
        ) {
            PolymerUtils.reloadInventory(player);
        }
    }
}
//#else
//$$ @Mixin(net.minecraft.SharedConstants.class)
//$$ public class Enderite_EnderiteBowMixin {}
//#endif
