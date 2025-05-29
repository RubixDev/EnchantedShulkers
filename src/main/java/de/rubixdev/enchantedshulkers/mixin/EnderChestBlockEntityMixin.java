package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//#if MC >= 12006
import de.rubixdev.enchantedshulkers.Utils;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
//#else
//$$ import net.minecraft.nbt.NbtList;
//#endif

@Mixin(EnderChestBlockEntity.class)
public abstract class EnderChestBlockEntityMixin extends BlockEntityMixin implements EnchantableBlockEntity {
    //#if MC >= 12006
    @Unique
    private ItemEnchantmentsComponent enchantments = ItemEnchantmentsComponent.DEFAULT;

    @Override
    public @NotNull ItemEnchantmentsComponent enchantedShulkers$getEnchantments() {
        return enchantments;
    }

    @Override
    public void enchantedShulkers$setEnchantments(@NotNull ItemEnchantmentsComponent enchantments) {
        this.enchantments = enchantments;
    }
    //#else
    //$$ @Unique private NbtList enchantments = new NbtList();
    //$$
    //$$ @Override
    //$$ public @NotNull NbtList enchantedShulkers$getEnchantments() {
    //$$     return this.enchantments;
    //$$ }
    //$$
    //$$ @Override
    //$$ public void enchantedShulkers$setEnchantments(@NotNull NbtList enchantments) {
    //$$     this.enchantments = enchantments;
    //$$ }
    //#endif

    //#if MC >= 12006
    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
        // read from NBT from old world saves
        if (nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
            var enchants = Utils.readEnchantmentsFromNbt(nbt);
            enchantedShulkers$setEnchantments(enchants);
            BlockEntity.Components.CODEC.encodeStart(
                    NbtOps.INSTANCE,
                    ComponentMap.builder().addAll(getComponents()).add(DataComponentTypes.ENCHANTMENTS, enchants).build()
            ).resultOrPartial().ifPresent(newNbt -> {
                var newNbtCmp = (NbtCompound) newNbt;
                for (var key : newNbtCmp.getKeys()) {
                    nbt.put(key, newNbtCmp.get(key));
                }
            });
        }
    }
    //#else
    //$$ @Override
    //$$ public void readNbt(NbtCompound nbt, CallbackInfo ci) {
    //$$     super.readNbt(nbt, ci);
    //$$     if (nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
    //$$         enchantedShulkers$setEnchantments(nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE));
    //$$     }
    //$$ }
    //$$
    //$$ @Override
    //$$ public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
    //$$     super.writeNbt(nbt, ci);
    //$$     nbt.put("Enchantments", this.enchantments);
    //$$ }
    //#endif

    @Override
    public void toInitialChunkDataNbt(CallbackInfoReturnable<NbtCompound> cir) {
        cir.setReturnValue(enchantedShulkers$toClientNbt());
    }

    @Override
    public void toUpdatePacket(CallbackInfoReturnable<@Nullable Packet<ClientPlayPacketListener>> cir) {
        cir.setReturnValue(BlockEntityUpdateS2CPacket.create((BlockEntity) (Object) this));
    }
}
