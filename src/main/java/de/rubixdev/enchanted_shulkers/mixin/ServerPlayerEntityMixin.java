package de.rubixdev.enchanted_shulkers.mixin;

import de.rubixdev.enchanted_shulkers.enchantment.RefillEnchantment;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(
            method = "playerTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;tick()V"))
    public void playerTick(CallbackInfo ci) {
        RefillEnchantment.onPlayerTick((ServerPlayerEntity) (Object) this);
    }
}
