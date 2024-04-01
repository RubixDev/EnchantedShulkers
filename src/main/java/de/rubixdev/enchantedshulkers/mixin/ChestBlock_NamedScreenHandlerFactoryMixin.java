package de.rubixdev.enchantedshulkers.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import de.rubixdev.enchantedshulkers.screen.AugmentedScreenHandler;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

// the Minecraft Dev plugin doesn't seem to like double nested anonymous classes
@SuppressWarnings({ "UnresolvedMixinReference", "MixinAnnotationTarget" })
@Mixin(targets = "net.minecraft.block.ChestBlock$2$1")
public abstract class ChestBlock_NamedScreenHandlerFactoryMixin {
    @Shadow
    @Final
    ChestBlockEntity field_17358;
    @Shadow
    @Final
    ChestBlockEntity field_17359;
    @Shadow
    @Final
    Inventory field_17360;

    @Shadow
    public abstract Text getDisplayName();

    @ModifyReturnValue(method = "createMenu", at = @At(value = "RETURN", ordinal = 0))
    private ScreenHandler augmentedScreenHandler(
        ScreenHandler original,
        int syncId,
        PlayerInventory playerInventory,
        PlayerEntity playerEntity
    ) {
        int level1 = field_17358 instanceof EnchantableBlockEntity e
            ? Utils.getLevelFromNbt(Mod.AUGMENT_ENCHANTMENT, e.enchantedShulkers$getEnchantments())
            : 0;
        int level2 = field_17359 instanceof EnchantableBlockEntity e
            ? Utils.getLevelFromNbt(Mod.AUGMENT_ENCHANTMENT, e.enchantedShulkers$getEnchantments())
            : 0;
        if (level1 != 0 || level2 != 0) {
            return AugmentedScreenHandler
                .create(syncId, playerInventory, field_17360, level1, getDisplayName(), null, false, level2);
        }
        return original;
    }
}
