package de.rubixdev.enchantedshulkers.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 26.2 port. Yarn's {@code GrindstoneScreenHandler} is Mojmap's {@code GrindstoneMenu}. The
 * original injected via a local-capture at the exact {@code ItemStack;removeSubNbt} call site
 * inside {@code grind()} - that method (and vanilla's own former NBT-based curse removal it
 * anchored on) no longer exists at all: 26.2's grindstone logic
 * (`mergeEnchantsFrom`/`removeNonCursesFrom` in `GrindstoneMenu`) already operates on
 * {@code ItemEnchantments} natively, with no NBT call to hook. Re-anchored on the return value of
 * the (private, but mixin-injectable) {@code computeResult} method instead, which is simpler than
 * the original technique.
 *
 * <p>TODO(26.2): the original also cancels {@code updateResult} (Mojmap: likely
 * {@code createResult}/{@code slotsChanged}) to prevent combining two shulker boxes into a stack -
 * not container-storage related, dropped for this pass; not yet re-verified against 26.2's actual
 * {@code slotsChanged}/{@code createResult} shape.
 */
@Mixin(net.minecraft.world.inventory.GrindstoneMenu.class)
public class GrindstoneMenuMixin {
    @Shadow
    @Final
    private ContainerLevelAccess access;

    @ModifyReturnValue(method = "computeResult", at = @At("RETURN"))
    private ItemStack enchantedShulkers$trimAugmentedInv(ItemStack result) {
        if (!Utils.canAugment(result)) return result;

        RegistryAccess registryAccess = access.evaluate((level, pos) -> level.registryAccess(), null);
        if (registryAccess == null) return result;

        Holder<Enchantment> augmentHolder = Utils.enchantmentHolder(registryAccess, Mod.AUGMENT_KEY);
        int level = EnchantmentHelper.getItemEnchantmentLevel(augmentHolder, result);
        if (level > 0) return result; // still augmented, nothing to trim

        NonNullList<ItemStack> inv = Utils.getContainerInventory(result, augmentHolder);
        if (inv.size() > ShulkerBoxBlockEntity.CONTAINER_SIZE) {
            NonNullList<ItemStack> trimmed = NonNullList.withSize(ShulkerBoxBlockEntity.CONTAINER_SIZE, ItemStack.EMPTY);
            for (int i = 0; i < ShulkerBoxBlockEntity.CONTAINER_SIZE; i++) {
                trimmed.set(i, inv.get(i));
            }
            Utils.setContainerInventory(result, trimmed);
        }
        return result;
    }
}
