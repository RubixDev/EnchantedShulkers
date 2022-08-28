package de.rubixdev.enchantedshulkers;

import de.rubixdev.enchantedshulkers.config.ClientConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@Environment(EnvType.CLIENT)
public class ClientMod implements ClientModInitializer {
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
