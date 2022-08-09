package de.rubixdev.enchanted_shulkers.mixin;

import de.rubixdev.enchanted_shulkers.Config;
import de.rubixdev.enchanted_shulkers.EnchantableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderChestBlock.class)
public abstract class EnderChestBlockMixin {
    @Redirect(
            method = "onUse",
            at =
                    @At(
                            value = "FIELD",
                            target = "Lnet/minecraft/block/EnderChestBlock;CONTAINER_NAME:Lnet/minecraft/text/Text;",
                            opcode = Opcodes.GETSTATIC))
    private Text colorizeName(BlockState state, World world, BlockPos pos) {
        Text original = EnderChestBlock.CONTAINER_NAME;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(Config.colorizeContainerNames()
                && blockEntity instanceof EnchantableBlockEntity enchantableBlockEntity
                && !enchantableBlockEntity.getEnchantments().isEmpty())) return original;

        MutableText text = original.copy();
        text.setStyle(Style.EMPTY.withFormatting(Formatting.AQUA));
        return text;
    }
}
