package de.rubixdev.enchanted_shulkers.asm;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;

public class EnumInjector implements Runnable {
    @Override
    public void run() {
        final String EnchantmentTarget = FabricLoader.getInstance()
                .getMappingResolver()
                .mapClassName("intermediary", "net.minecraft.class_1886");
        ClassTinkerers.enumBuilder(EnchantmentTarget)
                .addEnumSubclass("PORTABLE_CONTAINER", "de.rubixdev.enchanted_shulkers.asm.PortableContainerTarget")
                .build();
    }
}
