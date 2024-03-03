package de.rubixdev.enchantedshulkers.asm

import com.chocohead.mm.api.ClassTinkerers
import net.fabricmc.loader.api.FabricLoader

class EnumInjector : Runnable {
    override fun run() {
        @Suppress("LocalVariableName", "ktlint:standard:property-naming")
        val EnchantmentTarget = FabricLoader.getInstance().mappingResolver
            .mapClassName("intermediary", "net.minecraft.class_1886")
        ClassTinkerers.enumBuilder(EnchantmentTarget)
            .addEnumSubclass("PORTABLE_CONTAINER", "de.rubixdev.enchantedshulkers.asm.PortableContainerTarget")
            .addEnumSubclass("AUGMENTABLE_CONTAINER", "de.rubixdev.enchantedshulkers.asm.AugmentableContainerTarget")
            .build()
    }
}
