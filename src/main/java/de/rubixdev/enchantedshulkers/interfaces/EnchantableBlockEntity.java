package de.rubixdev.enchantedshulkers.interfaces;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public interface EnchantableBlockEntity {
    NbtList enchantedShulkers$getEnchantments();

    void enchantedShulkers$setEnchantments(NbtList enchantments);

    default NbtCompound enchantedShulkers$toClientNbt() {
        // Only send the enchantments to the client to reduce packet size
        NbtCompound nbt = new NbtCompound();
        nbt.put("Enchantments", enchantedShulkers$getEnchantments());
        return nbt;
    }
}
