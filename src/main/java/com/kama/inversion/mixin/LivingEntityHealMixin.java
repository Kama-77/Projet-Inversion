package com.kama.inversion.mixin;

import com.kama.inversion.InversionHooks;
import com.kama.inversion.InversionState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityHealMixin {
    @Inject(method = "heal(F)V", at = @At("HEAD"), cancellable = true)
    private void kamas_inversion_mod$invertHeal(float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity player)) return;
        if (!InversionState.isEnabled(player)) return;
        if (InversionHooks.isInternal()) return;

        InversionHooks.runInternal(() -> player.damage(player.getDamageSources().magic(), amount));
        ci.cancel();
    }
}

