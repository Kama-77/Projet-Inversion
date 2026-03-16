package com.kama.inversion.mixin;

import com.kama.inversion.InversionEffectStorage;
import com.kama.inversion.InversionEffects;
import com.kama.inversion.InversionState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PlayerEffectMixin {

    /**
     * ThreadLocal pour éviter la récursion infinie lors de l'application
     * de l'effet inversé ou original via ce même mixin.
     */
    private static final ThreadLocal<Boolean> APPLYING = ThreadLocal.withInitial(() -> false);

    /**
     * Intercepte l'ajout d'un effet de statut.
     * - Stocke toujours l'effet original
     * - Applique l'effet inversé si le joueur est en mode inversé
     * - Applique l'effet normal sinon
     */
    @Inject(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void kamas_inversion_mod$handleIncomingEffect(
            StatusEffectInstance effect,
            net.minecraft.entity.Entity source,
            CallbackInfoReturnable<Boolean> cir) {

        // Si on est déjà en train d'appliquer un effet via ce mixin, on laisse passer
        if (APPLYING.get()) return;

        // Si on est en train de faire un swap, on laisse passer sans toucher
        if (InversionEffectStorage.SWAPPING.get()) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity player)) return;

        // Vérifie si cet effet est supprimé en mode inversé
        if (InversionEffects.isSuppressed(effect.getEffectType())) {
            if (!APPLYING.get()) {
                InversionEffectStorage.store(player, new StatusEffectInstance(
                        effect.getEffectType(),
                        effect.getDuration(),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.shouldShowParticles(),
                        effect.shouldShowIcon()
                ));
            }
            if (InversionState.isEnabled(player)) {
                // On stocke mais on n'applique pas l'effet
                cir.cancel();
                cir.setReturnValue(false);
            }
            return;
        }

        // Vérifie si cet effet a un opposé, sinon on ne fait rien
        StatusEffectInstance inverted = InversionEffects.invertInstance(effect);
        if (inverted == null) return;

        // Stocker l'effet original (on est forcément hors swap ici)
        if (!APPLYING.get()) {
            InversionEffectStorage.store(player, new StatusEffectInstance(
                    effect.getEffectType(),
                    effect.getDuration(),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.shouldShowParticles(),
                    effect.shouldShowIcon()
            ));
        }

        // Appliquer l'effet inversé si en mode inversé, sinon l'original
        StatusEffectInstance toApply = InversionState.isEnabled(player) ? inverted : effect;

        cir.cancel();
        APPLYING.set(true);
        try {
            cir.setReturnValue(player.addStatusEffect(toApply, source));
        } finally {
            APPLYING.set(false);
        }
    }
}
