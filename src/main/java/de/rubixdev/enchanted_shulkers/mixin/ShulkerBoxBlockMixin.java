package de.rubixdev.enchanted_shulkers.mixin;

import de.rubixdev.enchanted_shulkers.EnchantedBlockEntity;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin {
    // For creative mode
    @Inject(
            method = "onBreak",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/ShulkerBoxBlockEntity;hasCustomName()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onBreak(
            World world,
            BlockPos pos,
            BlockState state,
            PlayerEntity player,
            CallbackInfo ci,
            BlockEntity blockEntity,
            ShulkerBoxBlockEntity shulkerBoxBlockEntity,
            ItemStack itemStack) {
        if (!(blockEntity instanceof EnchantedBlockEntity enchantedBlockEntity)) return;
        itemStack.setSubNbt("Enchantments", enchantedBlockEntity.getEnchantments());
    }

    // For all other means of breaking
    @Inject(method = "getDroppedStacks", at = @At("RETURN"))
    public void getDroppedStacks(
            BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
        List<ItemStack> drops = cir.getReturnValue();

        if (!(blockEntity instanceof EnchantedBlockEntity enchantedBlockEntity)) return;

        NbtList enchantments = enchantedBlockEntity.getEnchantments();

        NbtCompound nbt = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();
        blockEntityTag.put("Enchantments", enchantments);
        nbt.put("BlockEntityTag", blockEntityTag);
        nbt.put("Enchantments", enchantments);

        for (ItemStack drop : drops) {
            if (!(drop.getItem() instanceof BlockItem blockItem)) continue;
            if (!(blockItem.getBlock() instanceof ShulkerBoxBlock)) continue;
            NbtCompound existing = drop.getNbt();
            if (existing == null) {
                drop.setNbt(nbt.copy());
            } else {
                drop.setNbt(nbt.copyFrom(existing));
            }
        }
    }
}
