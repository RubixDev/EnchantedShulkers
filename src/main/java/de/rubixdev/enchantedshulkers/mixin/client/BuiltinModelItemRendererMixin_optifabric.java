package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin_optifabric {
    @Shadow
    @Final
    private static ShulkerBoxBlockEntity RENDER_SHULKER_BOX;

    @Shadow
    @Final
    private static ShulkerBoxBlockEntity[] RENDER_SHULKER_BOX_DYED;

    @Shadow
    @Final
    private EnderChestBlockEntity renderChestEnder;

    @Shadow
    @Final
    private BlockEntityRenderDispatcher blockEntityRenderDispatcher;

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    public void render(
            ItemStack stack,
            ModelTransformation.Mode mode,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay,
            CallbackInfo ci) {
        if (!(stack.getItem() instanceof BlockItem item)) return;
        Block block = item.getBlock();
        BlockEntity blockEntity;
        if (block instanceof ShulkerBoxBlock) {
            DyeColor dyeColor = ShulkerBoxBlock.getColor(item);
            blockEntity = dyeColor == null ? RENDER_SHULKER_BOX : RENDER_SHULKER_BOX_DYED[dyeColor.getId()];
        } else if (block == Blocks.ENDER_CHEST) {
            blockEntity = this.renderChestEnder;
        } else {
            return;
        }
        if (!(blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity)) return;
        enchantableBlockEntity.setEnchantments(stack.getEnchantments());
        this.blockEntityRenderDispatcher.renderEntity(blockEntity, matrices, vertexConsumers, light, overlay);
        ci.cancel();
    }
}
