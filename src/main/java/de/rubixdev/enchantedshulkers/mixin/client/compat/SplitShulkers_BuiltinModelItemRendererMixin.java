package de.rubixdev.enchantedshulkers.mixin.client.compat;

import com.bawnorton.mixinsquared.TargetHandler;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Restriction(require = @Condition("splitshulkers"))
@Mixin(value = BuiltinModelItemRenderer.class, priority = 1100)
public class SplitShulkers_BuiltinModelItemRendererMixin {
    @SuppressWarnings({"MixinAnnotationTarget", "InvalidMemberReference", "UnresolvedMixinReference"}) // MixinSquared
    @TargetHandler(
            mixin = "cursedflames.splitshulkers.mixin.client.MixinBlockEntityWithoutLevelRenderer",
            name = "onRenderByItem")
    @Redirect(
            method = "@MixinSquared:Handler",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;renderEntity(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)Z"))
    private boolean splitShulkersCompat(BlockEntityRenderDispatcher instance, BlockEntity blockEntity, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, ItemStack stack) {
        if ((blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity)) {
            enchantableBlockEntity.enchantedShulkers$setEnchantments(stack.getEnchantments());
        }
        return instance.renderEntity(blockEntity, matrix, vertexConsumerProvider, light, overlay);
    }
}
