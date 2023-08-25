package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
//#if MC >= 11800
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
//#else
//$$ import de.rubixdev.enchantedshulkers.Mod;
//#endif
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EnderChestBlockEntity.class)
public abstract class EnderChestBlockEntityMixin extends BlockEntity implements EnchantableBlockEntity {
    @Unique
    private NbtList enchantments = new NbtList();

    public EnderChestBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public NbtList enchantedShulkers$getEnchantments() {
        return this.enchantments;
    }

    @Override
    public void enchantedShulkers$setEnchantments(NbtList enchantments) {
        this.enchantments = enchantments;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
            enchantedShulkers$setEnchantments(nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE));
        }
    }

    @Override
    //#if MC >= 11800
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put("Enchantments", this.enchantments);
    }
    //#else
    //$$ public NbtCompound writeNbt(NbtCompound nbt) {
    //$$     super.writeNbt(nbt);
    //$$     nbt.put("Enchantments", this.enchantments);
    //$$     return nbt;
    //$$ }
    //#endif

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.enchantedShulkers$toClientNbt();
    }

    @Nullable
    @Override
    //#if MC >= 11800
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    //#else
    //$$ public BlockEntityUpdateS2CPacket toUpdatePacket() {
    //$$     return new BlockEntityUpdateS2CPacket(this.getPos(), Mod.BLOCK_ENTITY_TYPE_ENDER_CHEST, this.toInitialChunkDataNbt());
    //$$ }
    //#endif
}
