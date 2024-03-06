package de.rubixdev.enchantedshulkers.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;

//#if MC >= 12001
import de.rubixdev.enchantedshulkers.enchantment.RefillEnchantment;
import de.rubixdev.enchantedshulkers.interfaces.ProjectileHolder;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.enderitemc.enderitemod.tools.EnderiteCrossbow;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Restriction(require = @Condition("enderitemod"))
@Mixin(EnderiteCrossbow.class)
public class Enderite_EnderiteCrossbowMixin {
    @Inject(
        method = "loadProjectile",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.AFTER
        )
    )
    private static void refillProjectiles(
        LivingEntity shooter,
        ItemStack crossbow,
        ItemStack projectile,
        boolean simulated,
        boolean creative,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(shooter instanceof ServerPlayerEntity player)) return;
        Integer slot = ((ProjectileHolder) player).enchantedShulkers$getProjectileSlot(projectile);
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
//$$ public class Enderite_EnderiteCrossbowMixin {}
//#endif
