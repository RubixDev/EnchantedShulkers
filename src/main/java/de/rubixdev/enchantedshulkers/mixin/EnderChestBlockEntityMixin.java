package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderChestBlockEntity.class)
public abstract class EnderChestBlockEntityMixin extends BlockEntityMixin implements EnchantableBlockEntity {
    @Unique
    private NbtList enchantments = new NbtList();

    @Override
    public NbtList enchantedShulkers$getEnchantments() {
        return this.enchantments;
    }

    @Override
    public void enchantedShulkers$setEnchantments(NbtList enchantments) {
        this.enchantments = enchantments;
    }

    @Override
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        super.readNbt(nbt, ci);
        if (nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
            enchantedShulkers$setEnchantments(nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE));
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        super.writeNbt(nbt, ci);
        nbt.put("Enchantments", this.enchantments);
    }

    @Override
    public void toInitialChunkDataNbt(CallbackInfoReturnable<NbtCompound> cir) {
        cir.setReturnValue(this.enchantedShulkers$toClientNbt());
    }

    @Override
    public void toUpdatePacket(CallbackInfoReturnable<@Nullable Packet<ClientPlayPacketListener>> cir) {
        cir.setReturnValue(BlockEntityUpdateS2CPacket.create((BlockEntity) (Object) this));
    }
}
