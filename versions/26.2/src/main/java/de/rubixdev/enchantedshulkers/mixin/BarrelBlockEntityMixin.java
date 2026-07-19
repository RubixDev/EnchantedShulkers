package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2 port - parallel structure to ChestBlockEntityMixin (BarrelBlockEntity's {@code items}
 * field / {@code loadAdditional}/{@code saveAdditional}/{@code getContainerSize} shape is
 * identical - confirmed via javap). TODO(26.2): same deferrals as ChestBlockEntityMixin
 * (display-name tinting, AugmentedScreenHandler, client-sync packet override).
 */
@Mixin(BarrelBlockEntity.class)
public abstract class BarrelBlockEntityMixin implements EnchantableBlockEntity {
    @Shadow
    private NonNullList<ItemStack> items;

    @Unique private ItemEnchantments enchantments = ItemEnchantments.EMPTY;
    @Unique private int augmentLevel = 0;

    @NotNull
    @Override
    public ItemEnchantments enchantedShulkers$getEnchantments() {
        return enchantments;
    }

    @Override
    public void enchantedShulkers$setEnchantments(@NotNull ItemEnchantments enchantments) {
        this.enchantments = enchantments;
    }

    @Unique
    private void updateInventorySize() {
        int newSize = 9 * Utils.getInvRows(augmentLevel, 5);
        if (items.size() >= newSize) return;

        NonNullList<ItemStack> newInv = NonNullList.withSize(newSize, ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            newInv.set(i, items.get(i));
        }
        items = newInv;
    }

    @Inject(method = "loadAdditional", at = @At("HEAD"))
    private void enchantedShulkers$loadAdditional(ValueInput input, CallbackInfo ci) {
        enchantments = input.read("Enchantments", ItemEnchantments.CODEC).orElse(ItemEnchantments.EMPTY);
        var augmentHolder = input.lookup().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Mod.AUGMENT_KEY);
        augmentLevel = enchantments.getLevel(augmentHolder);
        updateInventorySize();
    }

    @Inject(method = "saveAdditional", at = @At("HEAD"))
    private void enchantedShulkers$saveAdditional(ValueOutput output, CallbackInfo ci) {
        if (!enchantments.isEmpty()) {
            output.store("Enchantments", ItemEnchantments.CODEC, enchantments);
        }
    }

    @Inject(method = "getContainerSize", at = @At("HEAD"), cancellable = true)
    private void enchantedShulkers$getContainerSize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(items.size());
    }
}
