package de.rubixdev.enchantedshulkers.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "getDroppedStacks", at = @At("RETURN"))
    public void getDroppedStacks(
        BlockState state,
        LootContextParameterSet.Builder builder,
        CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        //#if MC >= 12000
        BlockEntity blockEntity = builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        //#else
        //$$ BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
        //#endif
        List<ItemStack> drops = cir.getReturnValue();

        if (!(blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity)) return;

        NbtList enchantments = enchantableBlockEntity.enchantedShulkers$getEnchantments();

        NbtCompound nbt = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();
        blockEntityTag.put("Enchantments", enchantments);
        nbt.put("BlockEntityTag", blockEntityTag);
        nbt.put("Enchantments", enchantments);

        for (ItemStack drop : drops) {
            if (!(drop.getItem() instanceof BlockItem blockItem)) continue;
            if (!Utils.canEnchant(blockItem) && !Utils.canAugment(blockItem)) continue;
            NbtCompound existing = drop.getNbt();
            if (existing == null) {
                // Don't set empty Enchantments
                if (enchantments.isEmpty()) continue;
                drop.setNbt(nbt.copy());
            } else {
                // Remove `Enchantments` tags, when enchantments are empty
                if (enchantments.isEmpty()) {
                    drop.removeSubNbt("Enchantments");
                    NbtCompound existingBlockEntityTag = drop.getSubNbt("BlockEntityTag");
                    if (existingBlockEntityTag == null) continue;
                    existingBlockEntityTag.remove("Enchantments");
                    if (existingBlockEntityTag.isEmpty()) drop.removeSubNbt("BlockEntityTag");
                    continue;
                }
                drop.setNbt(nbt.copyFrom(existing));
            }
        }
    }

    @ModifyReturnValue(method = "getCullingShape", at = @At("RETURN"))
    protected VoxelShape getCullingShape(VoxelShape original) {
        return original;
    }
}
