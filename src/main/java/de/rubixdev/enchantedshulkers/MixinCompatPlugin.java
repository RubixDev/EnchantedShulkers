package de.rubixdev.enchantedshulkers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinCompatPlugin implements IMixinConfigPlugin {
    static final Map<String, String[]> incompatibilities = new HashMap<>() {
        {
            put("de.rubixdev.enchantedshulkers.mixin.client.BuiltinModelItemRendererMixin", new String[] {"optifabric"
            });
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
