package de.rubixdev.enchantedshulkers.mixin.client.compat;

import org.spongepowered.asm.mixin.Mixin;
//#if MC < 12002
//$$ import atonkish.reinfcore.util.ReinforcingMaterial;
//$$ import atonkish.reinfshulker.ReinforcedShulkerBoxesClientMod;
//$$ import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
//$$ import net.minecraft.block.Block;
//$$ import net.minecraft.block.entity.BlockEntity;
//$$ import net.minecraft.client.render.VertexConsumerProvider;
//$$ import net.minecraft.client.render.model.json.ModelTransformationMode;
//$$ import net.minecraft.client.util.math.MatrixStack;
//$$ import net.minecraft.item.ItemStack;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
//$$
//$$ @Mixin(ReinforcedShulkerBoxesClientMod.class)
//$$ public class ReinforcedShulkerBoxesClientModMixin {
//$$     @Inject(
//$$             method = "lambda$initializeReinforcedShulkerBoxesClient$0",
//$$             at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/entity/BlockEntityRenderDispatcher;renderEntity(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)Z"),
//$$             locals = LocalCapture.CAPTURE_FAILSOFT)
//$$     private static void addEnchantments(
//$$             ReinforcingMaterial material,
//$$             Block block,
//$$             ItemStack stack,
//$$             ModelTransformationMode mode,
//$$             MatrixStack matrices,
//$$             VertexConsumerProvider vertexConsumers,
//$$             int light,
//$$             int overlay,
//$$             CallbackInfo ci,
//$$             BlockEntity blockEntity
//$$     ) {
//$$         if (!(blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity)) return;
//$$         enchantableBlockEntity.enchantedShulkers$setEnchantments(stack.getEnchantments());
//$$     }
//$$ }
//#else
@Mixin(net.minecraft.SharedConstants.class)
public abstract class ReinforcedShulkerBoxesClientModMixin {}
//#endif
