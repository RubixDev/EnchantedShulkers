package de.rubixdev.enchantedshulkers.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.network.ClientLoginNetworkHandler;
//#if MC >= 12002
import de.rubixdev.enchantedshulkers.Mod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//#endif

@Mixin(ClientLoginNetworkHandler.class)
public class ClientLoginNetworkHandlerMixin {
    //#if MC >= 12002
    @Shadow @Final private ClientConnection connection;

    @Inject(method = "onSuccess", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;setPacketListener(Lnet/minecraft/network/listener/PacketListener;)V"))
    private void configPhaseStart(LoginSuccessS2CPacket packet, CallbackInfo ci) {
        this.connection.send(ClientPlayNetworking.createC2SPacket(Mod.CLIENT_INSTALLED_PACKET_ID, PacketByteBufs.empty()));
    }
    //#endif
}
