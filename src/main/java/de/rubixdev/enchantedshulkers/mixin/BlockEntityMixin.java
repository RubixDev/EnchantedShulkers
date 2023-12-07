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

@SuppressWarnings("CancellableInjectionUsage") // the methods are overriden in child mixins and there they need to be cancellable
@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
    @Inject(method = "toInitialChunkDataNbt", at = @At("HEAD"), cancellable = true)
    protected void toInitialChunkDataNbt(CallbackInfoReturnable<NbtCompound> cir) {}

    @Inject(method = "toUpdatePacket", at = @At("HEAD"), cancellable = true)
    protected void toUpdatePacket(CallbackInfoReturnable<@Nullable Packet<ClientPlayPacketListener>> cir) {}

    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void readNbt(NbtCompound nbt, CallbackInfo ci) {}

    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void writeNbt(NbtCompound nbt, CallbackInfo ci) {}
}
