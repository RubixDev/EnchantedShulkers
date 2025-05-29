package de.rubixdev.enchantedshulkers.mixin;

import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;

//#if MC >= 12006
import de.rubixdev.enchantedshulkers.enchantment.RefillEnchantment;
import de.rubixdev.enchantedshulkers.interfaces.ProjectileHolder;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#endif

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
    //#if MC >= 12006
    @Inject(
        method = "getProjectile",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;",
            shift = At.Shift.AFTER
        )
    )
    private static void refillProjectiles(
        ItemStack weaponStack,
        ItemStack projectileStack,
        LivingEntity shooter,
        boolean multishot,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!(shooter instanceof ServerPlayerEntity player)) return;
        Integer slot = ((ProjectileHolder) player).enchantedShulkers$getProjectileSlot(projectileStack);
        if (
            slot != null
                && RefillEnchantment
                    .refill(player, slot, ((ProjectileHolder) player).enchantedShulkers$getProjectileType(), 1)
        ) {
            PolymerUtils.reloadInventory(player);
        }
    }
    //#endif
}
