package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.config.ClientConfig;
import de.rubixdev.enchantedshulkers.network.InventoryCloseC2SPacket;
import de.rubixdev.enchantedshulkers.network.InventoryOpenC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable public Screen currentScreen;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void sendScreenChangePacket(Screen screen, CallbackInfo ci) {
        if (ClientConfig.refillInInventory() || MinecraftClient.getInstance().getNetworkHandler() == null) return;
        if (screen != null) {
            if (!(screen instanceof AbstractInventoryScreen<?>)) return;
            ClientPlayNetworking.send(InventoryOpenC2SPacket.INSTANCE);
        } else {
            if (currentScreen == null) return;
            ClientPlayNetworking.send(InventoryCloseC2SPacket.INSTANCE);
        }
    }
}
