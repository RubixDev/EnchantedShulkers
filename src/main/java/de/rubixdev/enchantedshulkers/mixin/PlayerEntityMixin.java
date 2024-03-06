package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.interfaces.ProjectileHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements ProjectileHolder {
    @Unique private Integer projectileSlot;
    @Unique private ItemStack projectileStack;
    @Unique private ItemStack projectileType;

    @Inject(method = "getProjectileType", at = @At("HEAD"))
    private void reset(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        projectileSlot = null;
        projectileStack = null;
        projectileType = null;
    }

    @Inject(
        method = "getProjectileType",
        at = @At(value = "RETURN", ordinal = 2),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void saveProjectileSlot(
        ItemStack stack,
        CallbackInfoReturnable<ItemStack> cir,
        Predicate<ItemStack> predicate,
        ItemStack itemStack,
        int i,
        ItemStack itemStack2
    ) {
        if (!WorldConfig.refillProjectiles()) return;
        projectileSlot = i;
        projectileStack = itemStack2;
        projectileType = itemStack2.copy();
    }

    @Nullable @Override
    public Integer enchantedShulkers$getProjectileSlot(@NotNull ItemStack expectedStack) {
        return expectedStack == projectileStack ? projectileSlot : null;
    }

    @NotNull // will only be called when `projectileSlot` is not null
    @Override
    public ItemStack enchantedShulkers$getProjectileType() {
        return projectileType;
    }
}
