package de.rubixdev.enchantedshulkers.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.rubixdev.enchantedshulkers.config.ClientConfig;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * This mixin is responsible for disabling the normal rendering logic for
 * barrels when they can be enchanted, as in that case a custom
 * BlockEntityRenderer is used instead.
 */
@Mixin(BarrelBlock.class)
public abstract class BarrelBlockMixin extends AbstractBlockMixin {
    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private BlockRenderType renderBlockEntityInstead(BlockRenderType original) {
        return ClientConfig.glintWhenPlaced() && WorldConfig.augmentableChestsAndBarrels()
            ? BlockRenderType.ENTITYBLOCK_ANIMATED
            : original;
    }

    @Override
    protected VoxelShape getCullingShape(VoxelShape original) {
        // I'd like this to also depend on the config, but it seems that if it's
        // set to `original` during the game launch, it doesn't matter what we
        // set it to afterward
        return VoxelShapes.empty();
    }
}
