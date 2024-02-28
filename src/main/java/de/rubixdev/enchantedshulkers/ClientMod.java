package de.rubixdev.enchantedshulkers;

import com.google.common.collect.ImmutableList;
import de.rubixdev.enchantedshulkers.config.ClientConfig;

import java.util.*;

import de.rubixdev.enchantedshulkers.config.InvalidOptionValueException;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.*;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

//#if MC >= 12001
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
//#endif

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@Environment(EnvType.CLIENT)
public class ClientMod implements ClientModInitializer {
    public static final SpriteIdentifier CLOSED_ENDER_TEXTURE_ID =
            new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, new Identifier(Mod.MOD_ID, "entity/chest/closed_ender"));
    public static final List<String> COLORS = Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map(DyeColor::getName).toList();
    public static final SpriteIdentifier CLOSED_SHULKER_TEXTURE_ID = new SpriteIdentifier(
            TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, new Identifier(Mod.MOD_ID, "entity/shulker/closed_shulker"));
    public static final List<SpriteIdentifier> CLOSED_COLORED_SHULKER_BOXES_TEXTURE_IDS = COLORS.stream()
            .map(string -> new SpriteIdentifier(
                    TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE,
                    new Identifier(Mod.MOD_ID, "entity/shulker/closed_shulker_" + string)))
            .collect(ImmutableList.toImmutableList());

    public static final ModelPart CLOSED_BOX;

    static {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild(
                "box",
                ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, 8.0f, -8.0f, 16.0f, 16.0f, 16.0f),
                ModelTransform.NONE);
        CLOSED_BOX = TexturedModelData.of(modelData, 64, 32).createModel();
    }

    private static boolean hasCloth = false;

    @Override
    public void onInitializeClient() {
        // let the server know that we have the client mod installed
        //#if MC >= 12001
        PolymerClientNetworking.setClientMetadata(Mod.HANDSHAKE_PACKET_ID, NbtInt.of(1));
        //#endif

        // update config from server
        ClientPlayNetworking.registerGlobalReceiver(Mod.CONFIG_SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            Mod.LOGGER.info("Received world config from server");
            NbtCompound config = buf.readNbt();
            if (config == null) {
                Mod.LOGGER.warn("Received server config is null");
                return;
            }
            for (String option : config.getKeys()) {
                NbtElement nbtValue = Objects.requireNonNull(config.get(option));
                String value = nbtValue.asString();
                if (nbtValue instanceof NbtByte nbtBool) {
                    value = String.valueOf(nbtBool.byteValue() != 0);
                }
                try {
                    WorldConfig.setOption(option, value);
                } catch (InvalidOptionValueException e) {
                    Mod.LOGGER.error("Received server config value for '" + option + "' is invalid: " + e.getMessage());
                }
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> WorldConfig.detachServer());

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
