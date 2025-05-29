package de.rubixdev.enchantedshulkers.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 12006
import com.llamalad7.mixinextras.sugar.Local;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
//#else
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif

@SuppressWarnings("CancellableInjectionUsage") // the methods are overriden in
                                               // child mixins and there they
                                               // need to be cancellable
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    //#if MC >= 12006
    @Shadow @Final private static Logger LOGGER;

    @Shadow public abstract ComponentMap getComponents();
    //#endif

    @Inject(method = "toInitialChunkDataNbt", at = @At("HEAD"), cancellable = true)
    protected void toInitialChunkDataNbt(CallbackInfoReturnable<NbtCompound> cir) {}

    @Inject(method = "toUpdatePacket", at = @At("HEAD"), cancellable = true)
    protected void toUpdatePacket(CallbackInfoReturnable<@Nullable Packet<ClientPlayPacketListener>> cir) {}

    //#if MC >= 12006
    @Inject(
            method = "readComponents(Lnet/minecraft/component/ComponentMap;Lnet/minecraft/component/ComponentChanges;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;readComponents(Lnet/minecraft/block/entity/BlockEntity$ComponentsAccess;)V")
    )
    private void readEnchantments(
        ComponentMap defaultComponents,
        ComponentChanges components,
        CallbackInfo ci,
        @Local(ordinal = 1) ComponentMap componentMap
    ) {
        if (!(this instanceof EnchantableBlockEntity enchantableBlockEntity)) return;
        var enchants = componentMap.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null) enchantableBlockEntity.enchantedShulkers$setEnchantments(enchants);
    }

    @Inject(method = "setComponents", at = @At("HEAD"))
    private void readEnchantments(ComponentMap components, CallbackInfo ci) {
        if (!(this instanceof EnchantableBlockEntity enchantableBlockEntity)) return;
        var enchants = components.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants != null) enchantableBlockEntity.enchantedShulkers$setEnchantments(enchants);
    }

    @Inject(method = "read", at = @At("HEAD"))
    private void readEnchantments(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        if (!(this instanceof EnchantableBlockEntity enchantableBlockEntity)) return;
        // TODO: this duplicates parsing work, but because Augment has to be read before the inventory nbt and
        //  I don't want to store the enchants in nbt, this'll have to do for now
        BlockEntity.Components.CODEC
                .parse(registryLookup.getOps(NbtOps.INSTANCE), nbt)
                .resultOrPartial(error -> LOGGER.warn("Failed to load components: {}", error))
                .ifPresent(components -> {
                    var enchants = components.get(DataComponentTypes.ENCHANTMENTS);
                    if (enchants != null) enchantableBlockEntity.enchantedShulkers$setEnchantments(enchants);
                });
    }

    @Inject(method = "removeFromCopiedStackNbt", at = @At("HEAD"))
    private void removeEnchantmentsNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this instanceof EnchantableBlockEntity) nbt.remove("Enchantments");
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {}
    //#else
    //$$ @Inject(method = "readNbt", at = @At("HEAD"))
    //$$ protected void readNbt(NbtCompound nbt, CallbackInfo ci) {}
    //$$
    //$$ @Inject(method = "writeNbt", at = @At("HEAD"))
    //$$ protected void writeNbt(NbtCompound nbt, CallbackInfo ci) {}
    //#endif
}
