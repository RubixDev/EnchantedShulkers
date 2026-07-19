package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.enchantment.RefillEnchantment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2 port. Yarn's {@code ServerPlayerEntity;playerTick()} injected right after an inner
 * {@code PlayerEntity;tick()} call; in Mojmap, {@code ServerPlayer} does not appear to declare a
 * separate tick override under a distinct name (only {@link Player#tick()} was found) - mixed
 * into {@code Player.tick()} at {@code TAIL} instead (server-side players only), which is
 * semantically equivalent (runs once after the player's per-tick update) without depending on the
 * exact original anchor point. TODO(26.2): re-verify against 26.2's actual ServerPlayer/Player
 * tick method split if this turns out not to fire at the right point at runtime.
 */
@Mixin(Player.class)
public abstract class ServerPlayerEntityMixin {
    @Unique private int previousSlot = -1;
    @Unique private ItemStack previousMainStack = ItemStack.EMPTY;
    @Unique private ItemStack previousOffStack = ItemStack.EMPTY;

    @Inject(method = "tick", at = @At("TAIL"))
    private void enchantedShulkers$tick(CallbackInfo ci) {
        if (!(((Object) this) instanceof ServerPlayer player)) return;

        int currentSlot = player.getInventory().getSelectedSlot();
        ItemStack currentMainStack = player.getMainHandItem();
        ItemStack currentOffStack = player.getInventory().getItem(Inventory.SLOT_OFFHAND);

        RefillEnchantment.onPlayerTick(
            player,
            false, // TODO(26.2): "inventory open" tracking (former InventoryState/networking packets) not ported yet
            currentSlot,
            currentMainStack,
            currentOffStack,
            previousSlot,
            previousMainStack,
            previousOffStack
        );

        previousSlot = currentSlot;
        previousMainStack = currentMainStack.copy();
        previousOffStack = currentOffStack.copy();
    }
}
