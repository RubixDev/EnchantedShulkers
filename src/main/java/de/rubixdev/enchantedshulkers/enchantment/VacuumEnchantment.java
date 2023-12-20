package de.rubixdev.enchantedshulkers.enchantment;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class VacuumEnchantment extends ContainerEnchantment {
    public VacuumEnchantment() {
        super(Mod.PORTABLE_CONTAINER_TARGET);
    }

    @Override
    protected boolean generate() {
        return WorldConfig.generateVacuum();
    }

    public static boolean onItemPickup(ServerPlayerEntity player, ItemStack stack) {
        if (player.isCreative() && !WorldConfig.creativeVacuum()) return false;
        return SiphonEnchantment.onItemPickup(player, stack, Mod.VACUUM_ENCHANTMENT, false, WorldConfig.weakerVacuum());
    }
}
