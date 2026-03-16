package com.kama.inversion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InversionEffects {

    private InversionEffects() {}

    private static final Map<StatusEffect, StatusEffect> OPPOSITES = Map.ofEntries(
            Map.entry(StatusEffects.STRENGTH,            StatusEffects.WEAKNESS),
            Map.entry(StatusEffects.WEAKNESS,            StatusEffects.STRENGTH),

            Map.entry(StatusEffects.SPEED,               StatusEffects.SLOWNESS),
            Map.entry(StatusEffects.SLOWNESS,            StatusEffects.SPEED),

            Map.entry(StatusEffects.NIGHT_VISION,        StatusEffects.BLINDNESS),
            Map.entry(StatusEffects.BLINDNESS,           StatusEffects.NIGHT_VISION),

            Map.entry(StatusEffects.INVISIBILITY,        StatusEffects.GLOWING),
            Map.entry(StatusEffects.GLOWING,             StatusEffects.INVISIBILITY),

            Map.entry(StatusEffects.LUCK,                StatusEffects.UNLUCK),
            Map.entry(StatusEffects.UNLUCK,              StatusEffects.LUCK),

            Map.entry(StatusEffects.HERO_OF_THE_VILLAGE, StatusEffects.BAD_OMEN),
            Map.entry(StatusEffects.BAD_OMEN,            StatusEffects.HERO_OF_THE_VILLAGE),

            Map.entry(StatusEffects.DARKNESS,            StatusEffects.NIGHT_VISION),

            Map.entry(StatusEffects.HASTE,               StatusEffects.MINING_FATIGUE),
            Map.entry(StatusEffects.MINING_FATIGUE,      StatusEffects.HASTE),

            Map.entry(StatusEffects.DOLPHINS_GRACE,      StatusEffects.SLOWNESS),

            Map.entry(StatusEffects.HUNGER,              StatusEffects.SATURATION),
            Map.entry(StatusEffects.SATURATION,          StatusEffects.HUNGER)
    );

    /**
     * Effets qui sont simplement supprimés en mode inversé
     * et restaurés quand on repasse en mode normal.
     */
    private static final Set<StatusEffect> SUPPRESSED = Set.of(
            StatusEffects.NAUSEA
    );

    /**
     * Effets composites : en mode inversé ils sont supprimés
     * et remplacés par une liste d'effets alternatifs.
     */
    private static final Map<StatusEffect, List<StatusEffect>> COMPOSITE = Map.of(
            StatusEffects.CONDUIT_POWER, List.of(StatusEffects.BLINDNESS, StatusEffects.SLOWNESS)
    );

    public static StatusEffect getOpposite(StatusEffect effect) {
        return OPPOSITES.get(effect);
    }

    public static boolean isSuppressed(StatusEffect effect) {
        return SUPPRESSED.contains(effect);
    }

    public static boolean isComposite(StatusEffect effect) {
        return COMPOSITE.containsKey(effect);
    }

    public static List<StatusEffect> getCompositeEffects(StatusEffect effect) {
        return COMPOSITE.getOrDefault(effect, List.of());
    }

    public static StatusEffectInstance invertInstance(StatusEffectInstance instance) {
        StatusEffect opposite = getOpposite(instance.getEffectType());
        if (opposite == null) return null;
        return new StatusEffectInstance(
                opposite,
                instance.getDuration(),
                instance.getAmplifier(),
                instance.isAmbient(),
                instance.shouldShowParticles(),
                instance.shouldShowIcon()
        );
    }

    public static void swapPlayerEffects(ServerPlayerEntity player) {
        boolean isNowInverted = InversionState.isEnabled(player);
        KamasInversionMod.LOGGER.info("=== swapPlayerEffects === isNowInverted: {}", isNowInverted);

        List<StatusEffectInstance> originals = new ArrayList<>(InversionEffectStorage.getAll(player));
        KamasInversionMod.LOGGER.info("Nombre d'effets dans le storage: {}", originals.size());

        InversionEffectStorage.clear(player);

        for (StatusEffectInstance original : originals) {

            // Cas effets composites (ex: conduit power)
            if (isComposite(original.getEffectType())) {
                List<StatusEffect> compositeEffects = getCompositeEffects(original.getEffectType());
                if (isNowInverted) {
                    // Lire la durée restante avant suppression
                    StatusEffectInstance current = player.getStatusEffect(original.getEffectType());
                    int remainingDuration = current != null ? current.getDuration() : original.getDuration();

                    player.removeStatusEffect(original.getEffectType());

                    // Stocker l'original avec durée restante
                    InversionEffectStorage.store(player, new StatusEffectInstance(
                            original.getEffectType(),
                            remainingDuration,
                            original.getAmplifier(),
                            original.isAmbient(),
                            original.shouldShowParticles(),
                            original.shouldShowIcon()
                    ));

                    // Appliquer les effets de remplacement
                    InversionEffectStorage.SWAPPING.set(true);
                    try {
                        for (StatusEffect composite : compositeEffects) {
                            player.addStatusEffect(new StatusEffectInstance(
                                    composite, remainingDuration, original.getAmplifier(),
                                    original.isAmbient(), original.shouldShowParticles(), original.shouldShowIcon()
                            ), null);
                        }
                    } finally {
                        InversionEffectStorage.SWAPPING.set(false);
                    }
                } else {
                    // Retirer les effets de remplacement
                    InversionEffectStorage.SWAPPING.set(true);
                    try {
                        for (StatusEffect composite : compositeEffects) {
                            player.removeStatusEffect(composite);
                        }
                        // Restaurer l'original
                        player.addStatusEffect(new StatusEffectInstance(
                                original.getEffectType(),
                                original.getDuration(),
                                original.getAmplifier(),
                                original.isAmbient(),
                                original.shouldShowParticles(),
                                original.shouldShowIcon()
                        ), null);
                    } finally {
                        InversionEffectStorage.SWAPPING.set(false);
                    }
                    InversionEffectStorage.store(player, original);
                }
                continue;
            }

            // Cas effets supprimés (ex: nausée)
            if (isSuppressed(original.getEffectType())) {
                if (isNowInverted) {
                    StatusEffectInstance current = player.getStatusEffect(original.getEffectType());
                    int remainingDuration = current != null ? current.getDuration() : original.getDuration();

                    // Ne pas stocker si durée trop courte (< 5 secondes = 100 ticks)
                    if (remainingDuration < 100) {
                        player.removeStatusEffect(original.getEffectType());
                        continue;
                    }

                    player.removeStatusEffect(original.getEffectType());
                    InversionEffectStorage.store(player, new StatusEffectInstance(
                            original.getEffectType(),
                            remainingDuration,
                            original.getAmplifier(),
                            original.isAmbient(),
                            original.shouldShowParticles(),
                            original.shouldShowIcon()
                    ));
                } else {
                    // Ne pas restaurer si durée trop courte (< 5 secondes = 100 ticks)
                    if (original.getDuration() < 100) {
                        continue;
                    }
                    InversionEffectStorage.store(player, original);
                    InversionEffectStorage.SWAPPING.set(true);
                    try {
                        player.addStatusEffect(new StatusEffectInstance(
                                original.getEffectType(),
                                original.getDuration(),
                                original.getAmplifier(),
                                original.isAmbient(),
                                original.shouldShowParticles(),
                                original.shouldShowIcon()
                        ), null);
                    } finally {
                        InversionEffectStorage.SWAPPING.set(false);
                    }
                }
                continue;
            }

            StatusEffectInstance inverted = invertInstance(original);
            if (inverted == null) continue;
            StatusEffect oppositeOfOriginal = getOpposite(original.getEffectType());
            if (oppositeOfOriginal == null) continue;

            boolean isAlreadyCoveredAsInverse = originals.stream()
                    .anyMatch(o -> o.getEffectType() == getOpposite(original.getEffectType())
                            && getOpposite(o.getEffectType()) == original.getEffectType()
                            && originals.indexOf(o) < originals.indexOf(original));
            if (isAlreadyCoveredAsInverse) {
                KamasInversionMod.LOGGER.info("Skip doublon: {}", original.getEffectType().getName());
                continue;
            }

            KamasInversionMod.LOGGER.info("Original dans storage: {}", original.getEffectType().getName());

            StatusEffectInstance currentOriginal = player.getStatusEffect(original.getEffectType());
            StatusEffectInstance currentInverted  = player.getStatusEffect(inverted.getEffectType());
            KamasInversionMod.LOGGER.info("Effet original actif sur joueur: {}", currentOriginal != null ? original.getEffectType().getName() : "null");
            KamasInversionMod.LOGGER.info("Effet inverse actif sur joueur: {}", currentInverted != null ? inverted.getEffectType().getName() : "null");

            StatusEffectInstance current = currentOriginal != null ? currentOriginal : currentInverted;
            if (current == null) {
                KamasInversionMod.LOGGER.info("Aucun effet actif trouve, on skip");
                continue;
            }

            int remainingDuration = current.getDuration();
            KamasInversionMod.LOGGER.info("Duree restante: {}", remainingDuration);

            player.removeStatusEffect(original.getEffectType());
            player.removeStatusEffect(inverted.getEffectType());

            InversionEffectStorage.store(player, new StatusEffectInstance(
                    original.getEffectType(),
                    remainingDuration,
                    original.getAmplifier(),
                    original.isAmbient(),
                    original.shouldShowParticles(),
                    original.shouldShowIcon()
            ));

            StatusEffectInstance base = isNowInverted ? inverted : original;
            KamasInversionMod.LOGGER.info("Application de l'effet: {}", base.getEffectType().getName());

            InversionEffectStorage.SWAPPING.set(true);
            try {
                player.addStatusEffect(new StatusEffectInstance(
                        base.getEffectType(),
                        remainingDuration,
                        base.getAmplifier(),
                        base.isAmbient(),
                        base.shouldShowParticles(),
                        base.shouldShowIcon()
                ), null);
            } finally {
                InversionEffectStorage.SWAPPING.set(false);
            }
        }
    }
}
