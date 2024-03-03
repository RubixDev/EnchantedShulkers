package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$4")
public class GrindstoneScreenHandler_ResultSlotMixin {
    @Shadow
    @Final
    ScreenHandlerContext field_16779;

    @Shadow
    @Final
    GrindstoneScreenHandler field_16780;

    @Inject(
        method = "onTakeItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"
        )
    )
    private void dropAugmentItems(PlayerEntity player, ItemStack resultStack, CallbackInfo ci) {
        this.field_16779.run((world, blockPos) -> {
            if (world instanceof ServerWorld) {
                for (int i = 0; i < this.field_16780.input.size(); i++) {
                    ItemStack stack = this.field_16780.input.getStack(i);
                    if (Utils.canAugment(stack) && EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, stack) > 0) {
                        DefaultedList<ItemStack> inv = Utils.getContainerInventory(stack);
                        for (int j = ShulkerBoxBlockEntity.INVENTORY_SIZE; j < inv.size(); j++) {
                            player.getInventory().offerOrDrop(inv.get(j));
                        }
                    }
                }
            }
        });
    }
}
