package de.rubixdev.enchantedshulkers.mixin.server;

import de.rubixdev.enchantedshulkers.interfaces.HasClientMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
@Environment(EnvType.SERVER)
public class ServerPlayerEntityMixin implements HasClientMod {
    @Override
    public void set(boolean hasClientMod) {
        ((HasClientMod) this.screenHandlerSyncHandler).set(hasClientMod);
    }

    @Shadow
    @Final
    private ScreenHandlerSyncHandler screenHandlerSyncHandler;
}
