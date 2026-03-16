package com.kama.inversion.mixin;

import com.kama.inversion.InversionEffectStorage;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public abstract class HungerManagerMixin {

    /**
     * Entoure l'appel à player.heal() dans HungerManager.update()
     * avec le flag FOOD_HEALING pour que LivingEntityHealMixin
     * sache que ce heal vient de la régénération alimentaire.
     */
    @Inject(
        method = "update(Lnet/minecraft/entity/player/PlayerEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V",
            shift = At.Shift.BEFORE
        )
    )
    private void kamas_inversion_mod$beforeFoodHeal(PlayerEntity player, CallbackInfo ci) {
        InversionEffectStorage.FOOD_HEALING.set(true);
    }

    @Inject(
        method = "update(Lnet/minecraft/entity/player/PlayerEntity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V",
            shift = At.Shift.AFTER
        )
    )
    private void kamas_inversion_mod$afterFoodHeal(PlayerEntity player, CallbackInfo ci) {
        InversionEffectStorage.FOOD_HEALING.set(false);
    }
}
