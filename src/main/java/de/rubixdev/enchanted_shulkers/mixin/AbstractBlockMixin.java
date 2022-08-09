package de.rubixdev.enchanted_shulkers.mixin;

import de.rubixdev.enchanted_shulkers.EnchantableBlockEntity;
import de.rubixdev.enchanted_shulkers.Utils;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {
    @Inject(method = "getDroppedStacks", at = @At("RETURN"))
    public void getDroppedStacks(
            BlockState state, LootContext.Builder builder, CallbackInfoReturnable<List<ItemStack>> cir) {
        BlockEntity blockEntity = builder.getNullable(LootContextParameters.BLOCK_ENTITY);
        List<ItemStack> drops = cir.getReturnValue();

        if (!(blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity)) return;

        NbtList enchantments = enchantableBlockEntity.getEnchantments();

        NbtCompound nbt = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();
        blockEntityTag.put("Enchantments", enchantments);
        nbt.put("BlockEntityTag", blockEntityTag);
        nbt.put("Enchantments", enchantments);

        for (ItemStack drop : drops) {
            if (!(drop.getItem() instanceof BlockItem blockItem)) continue;
            if (!Utils.canEnchant(blockItem)) continue;
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
}
