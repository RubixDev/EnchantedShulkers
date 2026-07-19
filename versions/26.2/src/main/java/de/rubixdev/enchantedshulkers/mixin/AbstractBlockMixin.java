package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2 port. Yarn's {@code AbstractBlock} is Mojmap's {@link BlockBehaviour}; {@code getDroppedStacks}
 * is now {@code getDrops}. The original mirrored the broken block entity's enchantment NBT onto
 * the dropped ItemStack's NBT directly ({@code drop.getNbt()}/{@code setNbt()}/{@code getSubNbt()}
 * /{@code removeSubNbt()}) - all gone in 26.2 (ItemStack has no NBT API at all). Rewritten onto
 * {@link DataComponents#ENCHANTMENTS} via {@link EnchantmentHelper#updateEnchantments}.
 */
@Mixin(BlockBehaviour.class)
public class AbstractBlockMixin {
    @Inject(method = "getDrops", at = @At("RETURN"))
    public void enchantedShulkers$getDrops(
        BlockState state,
        LootParams.Builder builder,
        CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity)) return;

        var enchantments = enchantableBlockEntity.enchantedShulkers$getEnchantments();
        List<ItemStack> drops = cir.getReturnValue();
        for (ItemStack drop : drops) {
            if (!(drop.getItem() instanceof BlockItem blockItem)) continue;
            if (!Utils.canEnchant(blockItem) && !Utils.canAugment(blockItem)) continue;
            if (enchantments.isEmpty()) continue;
            EnchantmentHelper.updateEnchantments(drop, mutable -> {
                for (var entry : enchantments.entrySet()) {
                    mutable.set(entry.getKey(), entry.getIntValue());
                }
            });
        }
    }
}
