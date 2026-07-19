package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.enchantment.SiphonEnchantment;
import de.rubixdev.enchantedshulkers.enchantment.VacuumEnchantment;
import de.rubixdev.enchantedshulkers.enchantment.VoidEnchantment;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 26.2 port. Yarn's {@code PlayerInventory} is Mojmap's {@code Inventory}; {@code insertStack} is {@code add}. */
@Mixin(Inventory.class)
public class PlayerInventoryMixin {
    @Shadow
    @Final
    public Player player;

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    public void enchantedShulkers$add(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Holder<Enchantment> siphonHolder = Utils.enchantmentHolder(serverPlayer.registryAccess(), Mod.SIPHON_KEY);
        if (SiphonEnchantment.onItemPickup(serverPlayer, stack, siphonHolder) && stack.isEmpty()) {
            cir.setReturnValue(true);
        } else if (VacuumEnchantment.onItemPickup(serverPlayer, stack) && stack.isEmpty()) {
            cir.setReturnValue(true);
        } else if (VoidEnchantment.onItemPickup(serverPlayer, stack)) {
            cir.setReturnValue(true);
        }
    }
}
