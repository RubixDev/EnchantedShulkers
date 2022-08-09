package de.rubixdev.enchanted_shulkers.mixin;

import de.rubixdev.enchanted_shulkers.Utils;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "set", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void set(Map<Enchantment, Integer> enchantments, ItemStack stack, CallbackInfo ci, NbtList nbtList) {
        if (!Utils.canEnchant(stack)) return;
        NbtCompound tag = stack.getOrCreateSubNbt("BlockEntityTag");
        if (nbtList.isEmpty()) {
            tag.remove("Enchantments");
            if (tag.isEmpty()) stack.removeSubNbt("BlockEntityTag");
        } else if (!stack.isOf(Items.ENCHANTED_BOOK)) {
            tag.put("Enchantments", nbtList);
        }
    }
}
