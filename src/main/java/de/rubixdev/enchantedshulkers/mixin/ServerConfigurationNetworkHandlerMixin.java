package de.rubixdev.enchantedshulkers.mixin;

import org.spongepowered.asm.mixin.Mixin;
//#if MC >= 12002
import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.interfaces.HasClientMod;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerConfigurationNetworkHandler.class)
public class ServerConfigurationNetworkHandlerMixin implements HasClientMod {
    @Unique private boolean hasClientMod = false;

    @Override
    public void enchantedShulkers$setTrue() {
        this.hasClientMod = true;
    }

    @Inject(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onReady(ReadyC2SPacket packet, CallbackInfo ci, PlayerManager playerManager, Text text, ServerPlayerEntity serverPlayerEntity) {
        Mod.LOGGER.info("Player " + serverPlayerEntity.getEntityName() + " has logged in with" + (this.hasClientMod ? "" : "out") + " the client-side mod.");
        if (this.hasClientMod) ((HasClientMod) serverPlayerEntity).enchantedShulkers$setTrue();
    }
}
//#else
//$$ @Mixin(net.minecraft.SharedConstants.class)
//$$ public abstract class ServerConfigurationNetworkHandlerMixin {}
//#endif
