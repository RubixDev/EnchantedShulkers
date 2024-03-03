package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {

    @Shadow
    @Final
    public Inventory input;

    @Shadow
    @Final
    private Inventory result;

    protected GrindstoneScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(
        method = "grind",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;removeSubNbt(Ljava/lang/String;)V",
            ordinal = 0,
            shift = At.Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void trimAugmentedInv(
        ItemStack item,
        int damage,
        int amount,
        CallbackInfoReturnable<ItemStack> cir,
        ItemStack itemStack
    ) {
        if (Utils.canAugment(itemStack) && EnchantmentHelper.getLevel(Mod.AUGMENT_ENCHANTMENT, itemStack) > 0) {
            DefaultedList<ItemStack> inv = Utils.getContainerInventory(itemStack);
            if (inv.size() > ShulkerBoxBlockEntity.INVENTORY_SIZE) {
                Utils.setContainerInventory(
                    itemStack,
                    DefaultedList.copyOf(
                        ItemStack.EMPTY,
                        inv.subList(0, ShulkerBoxBlockEntity.INVENTORY_SIZE).toArray(ItemStack[]::new)
                    )
                );
            }
        }
    }

    @Inject(method = "updateResult", at = @At("HEAD"), cancellable = true)
    private void preventShulkerStacking(CallbackInfo ci) {
        if (Utils.isShulkerBox(this.input.getStack(0)) && Utils.isShulkerBox(this.input.getStack(1))) {
            this.result.setStack(0, ItemStack.EMPTY);
            this.sendContentUpdates();
            ci.cancel();
        }
    }
}
