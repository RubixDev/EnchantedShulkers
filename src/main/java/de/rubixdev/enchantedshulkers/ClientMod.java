package de.rubixdev.enchantedshulkers;

import com.google.common.collect.ImmutableList;
import de.rubixdev.enchantedshulkers.config.ClientConfig;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@Environment(EnvType.CLIENT)
public class ClientMod implements ClientModInitializer {
    public static final SpriteIdentifier CLOSED_ENDER_TEXTURE_ID =
            new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, new Identifier("entity/chest/closed_ender"));
    public static final SpriteIdentifier CLOSED_SHULKER_TEXTURE_ID = new SpriteIdentifier(
            TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, new Identifier("entity/shulker/closed_shulker"));
    public static final List<SpriteIdentifier> CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS = Stream.of(
                    "white",
                    "orange",
                    "magenta",
                    "light_blue",
                    "yellow",
                    "lime",
                    "pink",
                    "gray",
                    "light_gray",
                    "cyan",
                    "purple",
                    "blue",
                    "brown",
                    "green",
                    "red",
                    "black")
            .map(string -> new SpriteIdentifier(
                    TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE,
                    new Identifier("entity/shulker/closed_shulker_" + string)))
            .collect(ImmutableList.toImmutableList());

    private static boolean hasCloth = false;

    @Override
    public void onInitializeClient() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config")) return;
        hasCloth = true;
        ClientConfig.init();
    }

    public static boolean glintWhenPlaced() {
        if (!hasCloth) return true;
        return ClientConfig.getInner().glintWhenPlaced;
    }

    public static boolean customModels() {
        if (!hasCloth) return true;
        return glintWhenPlaced() && ClientConfig.getInner().customModels;
    }

    public static boolean refillInInventory() {
        if (!hasCloth) return false;
        return ClientConfig.getInner().refillInInventory;
    }
}
