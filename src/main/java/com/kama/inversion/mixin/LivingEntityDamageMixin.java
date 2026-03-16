package com.kama.inversion.mixin;

import com.kama.inversion.InversionHooks;
import com.kama.inversion.InversionState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void kamas_inversion_mod$invertDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity player)) return;
        if (!InversionState.isEnabled(player)) return;
        if (InversionHooks.isInternal()) return;
        if (amount <= 0.0f) return;

        InversionHooks.runInternal(() -> player.heal(amount));
        cir.setReturnValue(true);
        cir.cancel();
    }
}

