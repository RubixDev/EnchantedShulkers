package de.rubixdev.enchantedshulkers.asm

//#if MC < 12006
//$$ import com.chocohead.mm.api.ClassTinkerers
//$$ import net.fabricmc.loader.api.FabricLoader
//#endif

class EnumInjector : Runnable {
    override fun run() {
        //#if MC < 12006
        //$$ @Suppress("LocalVariableName", "ktlint:standard:property-naming")
        //$$ val EnchantmentTarget = FabricLoader.getInstance().mappingResolver
        //$$     .mapClassName("intermediary", "net.minecraft.class_1886")
        //$$ ClassTinkerers.enumBuilder(EnchantmentTarget)
        //$$     .addEnumSubclass("PORTABLE_CONTAINER", "de.rubixdev.enchantedshulkers.asm.PortableContainerTarget")
        //$$     .addEnumSubclass("AUGMENTABLE_CONTAINER", "de.rubixdev.enchantedshulkers.asm.AugmentableContainerTarget")
        //$$     .build()
        //#endif
    }
}
