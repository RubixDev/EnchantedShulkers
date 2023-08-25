package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.Mod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC < 11800
//$$ import net.minecraft.block.entity.BlockEntity;
//$$ import net.minecraft.block.entity.ShulkerBoxBlockEntity;
//$$ import net.minecraft.block.entity.EnderChestBlockEntity;
//$$ import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
//$$ import net.minecraft.util.math.BlockPos;
//$$ import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
//#endif

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(
            method = "onGameJoin",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;sendClientSettings()V"))
    private void sendPacket(GameJoinS2CPacket packet, CallbackInfo ci) {
        this.sendPacket(ClientPlayNetworking.createC2SPacket(Mod.CLIENT_INSTALLED_PACKET_ID, PacketByteBufs.empty()));
    }

    //#if MC < 11800
    //$$ @Inject(method = "onBlockEntityUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;getBlockEntityType()I", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    //$$ private void readNbt(BlockEntityUpdateS2CPacket packet, CallbackInfo ci, BlockPos blockPos, BlockEntity blockEntity) {
    //$$     if (
    //$$             packet.getBlockEntityType() == Mod.BLOCK_ENTITY_TYPE_SHULKER_BOX && blockEntity instanceof ShulkerBoxBlockEntity
    //$$             || packet.getBlockEntityType() == Mod.BLOCK_ENTITY_TYPE_ENDER_CHEST && blockEntity instanceof EnderChestBlockEntity
    //$$     ) {
    //$$         blockEntity.readNbt(packet.getNbt());
    //$$     }
    //$$ }
    //#endif
}
