package de.rubixdev.enchantedshulkers;

import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinCompatPlugin implements IMixinConfigPlugin {
    static final Map<String, String[]> incompatibilities = new HashMap<>() {
        {
            put("de.rubixdev.enchantedshulkers.mixin.client.BuiltinModelItemRendererMixin", new String[] {"optifabric"});
        }
    };
    static final Map<String, String[]> deps = new HashMap<>() {
        {
            put("de.rubixdev.enchantedshulkers.mixin.client.compat.SplitShulkers_ShulkerBoxBlockEntityRendererMixin", new String[] {"splitshulkers"});
            put("de.rubixdev.enchantedshulkers.mixin.client.compat.SplitShulkers_BuiltinModelItemRendererMixin", new String[] {"splitshulkers"});
            put("de.rubixdev.enchantedshulkers.mixin.client.compat.ReinforcedShulkerBoxBlockEntityRendererMixin", new String[] {"reinfshulker"});
            put("de.rubixdev.enchantedshulkers.mixin.client.compat.ReinforcedShulkerBoxesClientModMixin", new String[] {"reinfshulker"});
        }
    };

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (deps.containsKey(mixinClassName)) {
            return Arrays.stream(deps.get(mixinClassName)).allMatch(modId -> FabricLoader.getInstance().isModLoaded(modId));
        }

        String[] classNameParts = mixinClassName.split("\\.");
        String lastPart = classNameParts[classNameParts.length - 1];
        if (lastPart.contains("_")) {
            String modId = lastPart.replaceFirst("^.*?_", "");
            String mixinClass = mixinClassName.replaceFirst("_.*$", "");
            if (incompatibilities.containsKey(mixinClass)
                    && Arrays.asList(incompatibilities.get(mixinClass)).contains(modId))
                return FabricLoader.getInstance().isModLoaded(modId);
        }

        String[] mods = incompatibilities.get(mixinClassName);
        if (mods == null) return true;
        for (String modId : mods) {
            if (FabricLoader.getInstance().isModLoaded(modId)) return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
