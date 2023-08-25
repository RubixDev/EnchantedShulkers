package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends LootableContainerBlockEntity
        implements EnchantableBlockEntity {
    @Unique
    private NbtList enchantments = new NbtList();

    protected ShulkerBoxBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public NbtList enchantedShulkers$getEnchantments() {
        return this.enchantments;
    }

    @Override
    public void enchantedShulkers$setEnchantments(NbtList enchantments) {
        this.enchantments = enchantments;
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
            enchantedShulkers$setEnchantments(nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE));
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    public void writeNbt(
            NbtCompound nbt,
            //#if MC >= 11800
            CallbackInfo ci
            //#else
            //$$ CallbackInfoReturnable<NbtCompound> cir
            //#endif
    ) {
        nbt.put("Enchantments", this.enchantments);
    }

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
    //$$     return new BlockEntityUpdateS2CPacket(this.getPos(), Mod.BLOCK_ENTITY_TYPE_SHULKER_BOX, this.toInitialChunkDataNbt());
    //$$ }
    //#endif

    @Inject(method = "getContainerName", at = @At(value = "RETURN"), cancellable = true)
    public void getContainerName(CallbackInfoReturnable<Text> cir) {
        MutableText text = cir.getReturnValue().copy();
        if (WorldConfig.coloredNames() && !enchantments.isEmpty()) {
            text.setStyle(Style.EMPTY.withFormatting(Formatting.AQUA));
        }
        cir.setReturnValue(text);
    }
}
