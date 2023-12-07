package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends BlockEntityMixin
        implements EnchantableBlockEntity {
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

    @Inject(method = "readNbt", at = @At("TAIL"))
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
            enchantedShulkers$setEnchantments(nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE));
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
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

    @Inject(method = "getContainerName", at = @At(value = "RETURN"), cancellable = true)
    public void getContainerName(CallbackInfoReturnable<Text> cir) {
        MutableText text = cir.getReturnValue().copy();
        if (WorldConfig.coloredNames() && !enchantments.isEmpty()) {
            text.setStyle(Style.EMPTY.withFormatting(Formatting.AQUA));
        }
        cir.setReturnValue(text);
    }
}
