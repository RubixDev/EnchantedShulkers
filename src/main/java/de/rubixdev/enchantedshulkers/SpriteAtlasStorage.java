package de.rubixdev.enchantedshulkers;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SpriteAtlasStorage {
    public static final Identifier CLOSED_CONTAINERS_ATLAS_TEXTURE_ID =
            new Identifier("enchantedshulkers:textures/atlas/closed_containers.png");
    public static final SpriteIdentifier CLOSED_ENDER_TEXTURE_ID = new SpriteIdentifier(
            CLOSED_CONTAINERS_ATLAS_TEXTURE_ID, new Identifier("enchantedshulkers:entity/chest/closed_ender"));
    public static final SpriteIdentifier CLOSED_SHULKER_TEXTURE_ID = new SpriteIdentifier(
            CLOSED_CONTAINERS_ATLAS_TEXTURE_ID, new Identifier("enchantedshulkers:entity/shulker/closed_shulker"));
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
                    CLOSED_CONTAINERS_ATLAS_TEXTURE_ID,
                    new Identifier("enchantedshulkers:entity/shulker/closed_shulker_" + string)))
            .collect(ImmutableList.toImmutableList());
    public static SpriteAtlasTexture closedContainersAtlasTexture =
            new SpriteAtlasTexture(CLOSED_CONTAINERS_ATLAS_TEXTURE_ID);
}
