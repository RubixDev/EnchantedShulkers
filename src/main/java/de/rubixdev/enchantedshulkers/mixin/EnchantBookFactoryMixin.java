package de.rubixdev.enchantedshulkers.mixin;

import net.minecraft.village.TradeOffers;
import org.spongepowered.asm.mixin.*;

//#if MC >= 12002
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;
//#endif

@Mixin(TradeOffers.EnchantBookFactory.class)
public class EnchantBookFactoryMixin {
    //#if MC >= 12002
    @Shadow private List<Enchantment> possibleEnchantments;

    @Unique private boolean isAllEnchants = false;

    @Inject(method = "<init>(I)V", at = @At("TAIL"))
    private void isAllEnchants(int experience, CallbackInfo ci) {
        this.isAllEnchants = true;
    }

    @Inject(method = "create", at = @At("HEAD"))
    private void ensureAllEnchantments(Entity entity, Random random, CallbackInfoReturnable<TradeOffer> cir) {
        // in some cases where other mods register Villagers or Villager Trades before a mod registers an Enchantment,
        // all trade-able enchantments get computed before all enchantments are registered.
        // This injection causes the enchantment offers list to be recomputed each time.
        if (this.isAllEnchants) {
            this.possibleEnchantments = getEnchantments();
        }
    }

    @Unique
    private static List<Enchantment> getEnchantments() {
        return Registries.ENCHANTMENT.stream().filter(Enchantment::isAvailableForEnchantedBookOffer).toList();
    }
    //#endif
}
