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
}
