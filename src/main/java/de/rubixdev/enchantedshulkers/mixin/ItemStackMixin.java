package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Utils;
import java.util.Objects;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract NbtCompound getOrCreateSubNbt(String key);

    @Shadow
    public abstract @Nullable NbtCompound getNbt();

    @Inject(method = "addEnchantment", at = @At("TAIL"))
    public void addEnchantment(Enchantment enchantment, int level, CallbackInfo ci) {
        if (!Utils.canEnchant(this.getItem()) && !Utils.canAugment(this.getItem())) return;
        NbtCompound tag = getOrCreateSubNbt("BlockEntityTag");
        tag.put("Enchantments", Objects.requireNonNull(this.getNbt()).get("Enchantments"));
    }
}
