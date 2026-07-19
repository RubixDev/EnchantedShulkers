package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 26.2 port - core pass. Yarn->Mojmap: {@code DefaultedList}-&gt;{@code NonNullList}, the
 * {@code inventory} field is now named {@code items}, {@code readNbt}/{@code writeNbt}(NbtCompound)
 * are now {@code loadAdditional}/{@code saveAdditional}({@link ValueInput}/{@link ValueOutput}) -
 * a real API redesign (codec-based typed read/write replacing raw NBT compound manipulation), not
 * just a rename; {@code size()} is now {@code getContainerSize()}.
 *
 * <p>TODO(26.2): the original also (a) overrides {@code getContainerName}/{@code createScreenHandler}
 * to tint the chest's display name and open a custom {@code AugmentedScreenHandler} when augmented
 * - both depend on the {@code screen/} package, not ported yet; (b) overrides
 * {@code toInitialChunkDataNbt}/{@code toUpdatePacket} to force a full block-entity sync packet so
 * the enchantment glint updates for nearby clients immediately - deferred pending the renderer
 * mixins pass. Only enchantment persistence and inventory-size updates are ported here.
 */
@Mixin(ChestBlockEntity.class)
public abstract class ChestBlockEntityMixin implements EnchantableBlockEntity {
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
    private void enchantedShulkers$getContainerSize(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(items.size());
    }
}
