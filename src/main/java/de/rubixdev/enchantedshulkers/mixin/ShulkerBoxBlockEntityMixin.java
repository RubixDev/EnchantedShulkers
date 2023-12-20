package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import de.rubixdev.enchantedshulkers.screen.AugmentedShulkerBoxScreenHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.IntStream;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends BlockEntityMixin
        implements EnchantableBlockEntity {
    @Shadow private DefaultedList<ItemStack> inventory;
    @Unique
    private NbtList enchantments = new NbtList();

    @Override
    public NbtList enchantedShulkers$getEnchantments() {
        return this.enchantments;
    }

    @Override
    public void enchantedShulkers$setEnchantments(NbtList enchantments) {
        this.enchantments = enchantments;
        this.updateInventorySize();
    }

    @Unique
    private void updateInventorySize() {
        int newSize = 9 * Utils.getInvRows(Utils.getLevelFromNbt(Mod.AUGMENT_ENCHANTMENT, this.enchantments));
        if (this.inventory.size() >= newSize) return;

        DefaultedList<ItemStack> newInv = DefaultedList.ofSize(newSize, ItemStack.EMPTY);
        for (int i = 0; i < this.inventory.size(); i++) {
            newInv.set(i, this.inventory.get(i));
        }
        this.inventory = newInv;
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
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

    @Inject(method = "createScreenHandler", at = @At("HEAD"), cancellable = true)
    private void augmentedScreenHandler(int syncId, PlayerInventory playerInventory, CallbackInfoReturnable<ScreenHandler> cir) {
        int level = Utils.getLevelFromNbt(Mod.AUGMENT_ENCHANTMENT, this.enchantments);
        if (level != 0) {
            cir.setReturnValue(new AugmentedShulkerBoxScreenHandler(syncId, playerInventory, (Inventory) this, level));
        }
    }

    @Inject(method = "getAvailableSlots", at = @At("HEAD"), cancellable = true)
    private void augmentedInvSize(Direction side, CallbackInfoReturnable<int[]> cir) {
        int level = Utils.getLevelFromNbt(Mod.AUGMENT_ENCHANTMENT, this.enchantments);
        if (level != 0) {
            cir.setReturnValue(IntStream.range(0, 9 * Utils.getInvRows(level)).toArray());
        }
    }
}
