package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
//#if MC >= 12004
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//#else
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {
    /**
     * Only for creative mode, for all other means of breaking see {@link de.rubixdev.enchantedshulkers.mixin.AbstractBlockMixin#getDroppedStacks(BlockState, LootContextParameterSet.Builder, CallbackInfoReturnable)}
     */
    @Inject(
            method = "onBreak",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;hasCustomName()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onBreak(
            World world,
            BlockPos pos,
            BlockState state,
            PlayerEntity player,
            //#if MC >= 12004
            CallbackInfoReturnable<BlockState> cir,
            //#else
            //$$ CallbackInfo ci,
            //#endif
            BlockEntity blockEntity,
            ShulkerBoxBlockEntity shulkerBoxBlockEntity,
            ItemStack itemStack) {
        if (!(blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity)) return;
        itemStack.setSubNbt("Enchantments", enchantableBlockEntity.enchantedShulkers$getEnchantments());
    }
}
