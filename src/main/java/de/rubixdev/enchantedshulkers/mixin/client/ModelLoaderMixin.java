package de.rubixdev.enchantedshulkers.mixin.client;

import de.rubixdev.enchantedshulkers.SpriteAtlasStorage;
import java.util.stream.Stream;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @Unique
    private SpriteAtlasTexture.Data data;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void init(
            ResourceManager resourceManager,
            BlockColors blockColors,
            Profiler profiler,
            int mipmapLevel,
            CallbackInfo ci) {
        Stream<Identifier> idStream = Stream.concat(
                Stream.of(
                        SpriteAtlasStorage.CLOSED_SHULKER_TEXTURE_ID.getTextureId(),
                        SpriteAtlasStorage.CLOSED_ENDER_TEXTURE_ID.getTextureId()),
                SpriteAtlasStorage.CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS.stream()
                        .map(SpriteIdentifier::getTextureId));
        data = SpriteAtlasStorage.closedContainersAtlasTexture.stitch(resourceManager, idStream, profiler, mipmapLevel);
    }

    @Inject(
            method = "upload",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
                            shift = At.Shift.AFTER))
    private void upload(
            TextureManager textureManager, Profiler profiler, CallbackInfoReturnable<SpriteAtlasManager> cir) {
        SpriteAtlasStorage.closedContainersAtlasTexture.upload(data);
        textureManager.registerTexture(
                SpriteAtlasStorage.CLOSED_CONTAINERS_ATLAS_TEXTURE_ID, SpriteAtlasStorage.closedContainersAtlasTexture);
        textureManager.bindTexture(SpriteAtlasStorage.CLOSED_CONTAINERS_ATLAS_TEXTURE_ID);
        SpriteAtlasStorage.closedContainersAtlasTexture.applyTextureFilter(data);
    }
}
