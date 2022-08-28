package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.ClientMod;
import de.rubixdev.enchantedshulkers.Mod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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
    @Nullable
    public Screen currentScreen;

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void printScreen(Screen screen, CallbackInfo ci) {
        if (ClientMod.refillInInventory()) return;
        if (screen != null) {
            if (!(screen instanceof AbstractInventoryScreen<?>)) return;
            ClientPlayNetworking.send(Mod.INVENTORY_OPEN_PACKET_ID, PacketByteBufs.empty());
        } else {
            if (currentScreen == null) return;
            ClientPlayNetworking.send(Mod.INVENTORY_CLOSE_PACKET_ID, PacketByteBufs.empty());
        }
    }
}
