package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.Utils;
import de.rubixdev.enchantedshulkers.config.WorldConfig;
import de.rubixdev.enchantedshulkers.interfaces.EnchantableBlockEntity;
import de.rubixdev.enchantedshulkers.screen.AugmentedScreenHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.IntStream;

//#if MC >= 12006
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
//#else
//$$ import net.minecraft.nbt.NbtList;
//$$ import net.minecraft.nbt.NbtOps;
//#endif

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin extends BlockEntityMixin
    implements EnchantableBlockEntity, NamedScreenHandlerFactory {
    @Shadow
    private DefaultedList<ItemStack> inventory;

    @Shadow
    @Nullable public abstract DyeColor getColor();

    //#if MC >= 12006
    @Unique private ItemEnchantmentsComponent enchantments = ItemEnchantmentsComponent.DEFAULT;

    @Override
    public @NotNull ItemEnchantmentsComponent enchantedShulkers$getEnchantments() {
        return enchantments;
    }

    @Override
    public void enchantedShulkers$setEnchantments(@NotNull ItemEnchantmentsComponent enchantments) {
        this.enchantments = enchantments;
        updateInventorySize();
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
    //$$     updateInventorySize();
    //$$ }
    //#endif

    @Unique private void updateInventorySize() {
        int newSize = 9 * Utils.getInvRows(Utils.getLevel(Mod.AUGMENT_ENCHANTMENT, enchantments));
        if (inventory.size() >= newSize) return;

        DefaultedList<ItemStack> newInv = DefaultedList.ofSize(newSize, ItemStack.EMPTY);
        for (int i = 0; i < inventory.size(); i++) {
            newInv.set(i, inventory.get(i));
        }
        inventory = newInv;
    }

    //#if MC >= 12006
    @Inject(method = "readNbt", at = @At("HEAD"))
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo ci) {
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
    //$$ @Inject(method = "readNbt", at = @At("HEAD"))
    //$$ public void readNbt(NbtCompound nbt, CallbackInfo ci) {
    //$$     if (nbt.contains("Enchantments", NbtElement.LIST_TYPE)) {
    //$$         enchantedShulkers$setEnchantments(nbt.getList("Enchantments", NbtElement.COMPOUND_TYPE));
    //$$     }
    //$$ }
    //$$
    //$$ @Inject(method = "writeNbt", at = @At("TAIL"))
    //$$ public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
    //$$     nbt.put("Enchantments", this.enchantments);
    //$$ }
    //#endif

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
    private void augmentedScreenHandler(
        int syncId,
        PlayerInventory playerInventory,
        CallbackInfoReturnable<ScreenHandler> cir
    ) {
        int level = Utils.getLevel(Mod.AUGMENT_ENCHANTMENT, enchantments);
        if (level != 0) {
            cir.setReturnValue(
                AugmentedScreenHandler
                    .create(syncId, playerInventory, (Inventory) this, level, getDisplayName(), getColor(), true, null)
            );
        }
    }

    @Inject(method = "getAvailableSlots", at = @At("HEAD"), cancellable = true)
    private void augmentedInvSize(Direction side, CallbackInfoReturnable<int[]> cir) {
        int level = Utils.getLevel(Mod.AUGMENT_ENCHANTMENT, enchantments);
        if (level != 0) {
            cir.setReturnValue(IntStream.range(0, 9 * Utils.getInvRows(level)).toArray());
        }
    }
}
