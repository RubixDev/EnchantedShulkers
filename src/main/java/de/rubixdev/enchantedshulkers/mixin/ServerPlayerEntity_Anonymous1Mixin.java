package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.interfaces.HasClientMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$1")
public class ServerPlayerEntity_Anonymous1Mixin implements HasClientMod {
    @Unique private boolean hasClientMod = false;
    @SuppressWarnings("FieldMayBeFinal")
    @Unique private boolean inInitPhase =
    //#if MC >= 12002
        false;
    //#else
    //$$     true;
    //#endif

    @Override
    public void enchantedShulkers$setTrue() {
        this.hasClientMod = true;
    }

    //#if MC < 12002
    //$$ @Override
    //$$ public void enchantedShulkers$submit() {
    //$$     this.inInitPhase = false;
    //$$ }
    //#endif

    //////////////////////////////////////////////

    @ModifyVariable(method = "updateState", at = @At("HEAD"), index = 2, argsOnly = true)
    private DefaultedList<ItemStack> updateStateStacks(DefaultedList<ItemStack> stacks) {
        if (this.hasClientMod || this.inInitPhase) return stacks;

        DefaultedList<ItemStack> newStacks = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); ++i) {
            newStacks.set(i, setLore(stacks.get(i).copy()));
        }
        return newStacks;
    }

    @ModifyVariable(method = "updateState", at = @At("HEAD"), index = 3, argsOnly = true)
    private ItemStack updateStateCursorStack(ItemStack stack) {
        if (this.hasClientMod || this.inInitPhase) return stack;
        return setLore(stack.copy());
    }

    @ModifyVariable(method = "updateSlot", at = @At("HEAD"), index = 3, argsOnly = true)
    private ItemStack updateSlot(ItemStack stack) {
        if (this.hasClientMod || this.inInitPhase) return stack;
        return setLore(stack.copy());
    }

    @ModifyVariable(method = "updateCursorStack", at = @At("HEAD"), index = 2, argsOnly = true)
    private ItemStack updateCursorStack(ItemStack stack) {
        if (this.hasClientMod || this.inInitPhase) return stack;
        return setLore(stack.copy());
    }

    //////////////////////////////////////////////

    @Unique
    private static ItemStack setLore(ItemStack stack) {
        NbtList lore = new NbtList();
        addEnchantment(lore, stack, Mod.SIPHON_ENCHANTMENT, "Siphon");
        addEnchantment(lore, stack, Mod.REFILL_ENCHANTMENT, "Refill");
        addEnchantment(lore, stack, Mod.VACUUM_ENCHANTMENT, "Vacuum");
        addEnchantment(lore, stack, Mod.VOID_ENCHANTMENT, "Void");
        addEnchantment(lore, stack, Mod.AUGMENT_ENCHANTMENT, "Augment");

        if (!lore.isEmpty()) {
            NbtCompound nbt = stack.getOrCreateSubNbt("display");
            nbt.put("Lore", lore);
        }

        return stack;
    }

    @Unique
    private static void addEnchantment(NbtList lore, ItemStack stack, Enchantment enchantment, String name) {
        int level = EnchantmentHelper.getLevel(enchantment, stack);
        if (level <= 0) {
            level = getStoredLevel(enchantment, stack);
        }
        if (level > 0) {
            lore.add(NbtString.of(enchantmentText(name, level, enchantment)));
        }
    }

    @Unique
    private static String enchantmentText(String name, int level, Enchantment enchantment) {
        MutableText mutableText = Text.literal(name);
        mutableText.formatted(Formatting.GRAY).styled(style -> style.withItalic(false));
        if (level != 1 || enchantment.getMaxLevel() != 1) {
            mutableText.append(ScreenTexts.SPACE).append(Text.translatable("enchantment.level." + level));
        }
        return Text.Serialization.toJsonString(mutableText);
    }

    @Unique
    private static int getStoredLevel(Enchantment enchantment, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        NbtList nbtList = EnchantedBookItem.getEnchantmentNbt(stack);
        Map<Enchantment, Integer> storedEnchantments = EnchantmentHelper.fromNbt(nbtList);
        return storedEnchantments.getOrDefault(enchantment, 0);
    }
}
