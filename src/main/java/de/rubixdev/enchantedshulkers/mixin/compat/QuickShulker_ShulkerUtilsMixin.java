package de.rubixdev.enchantedshulkers.mixin.compat;

import net.kyrptonaught.shulkerutils.ItemStackInventory;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShulkerUtils.class)
public class QuickShulker_ShulkerUtilsMixin {
    // might not be necessary once https://github.com/kyrptonaught/shulkerutils/pull/1 gets merged
    @Inject(
            method = "getInventoryFromShulker",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/BlockItem;getBlock()Lnet/minecraft/block/Block;", shift = At.Shift.AFTER),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void augmentInvSize(ItemStack stack, CallbackInfoReturnable<ItemStackInventory> cir, Block shulker) {
        if (shulker instanceof BlockWithEntity bwe) {
            BlockEntity blockEntity = bwe.createBlockEntity(BlockPos.ORIGIN, null);
            if (blockEntity instanceof ShulkerBoxBlockEntity shulkerEntity) {
                blockEntity.readNbt(BlockItem.getBlockEntityNbt(stack));
                cir.setReturnValue(new ItemStackInventory(stack, shulkerEntity.size()));
            }
        }
    }
}
