package com.kama.inversion;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InversionEffectStorage {

    private static final Map<UUID, Map<StatusEffect, StatusEffectInstance>> STORAGE = new ConcurrentHashMap<>();

    // ThreadLocal pour bloquer le stockage pendant un swap
    public static final ThreadLocal<Boolean> SWAPPING = ThreadLocal.withInitial(() -> false);

    // ThreadLocal pour indiquer que le heal vient de la régénération alimentaire
    public static final ThreadLocal<Boolean> FOOD_HEALING = ThreadLocal.withInitial(() -> false);

    private InversionEffectStorage() {}

    public static void store(ServerPlayerEntity player, StatusEffectInstance instance) {
        STORAGE
            .computeIfAbsent(player.getUuid(), id -> new ConcurrentHashMap<>())
            .put(instance.getEffectType(), instance);
    }

    public static StatusEffectInstance getOriginal(ServerPlayerEntity player, StatusEffect type) {
        Map<StatusEffect, StatusEffectInstance> map = STORAGE.get(player.getUuid());
        if (map == null) return null;
        return map.get(type);
    }

    public static Collection<StatusEffectInstance> getAll(ServerPlayerEntity player) {
        Map<StatusEffect, StatusEffectInstance> map = STORAGE.get(player.getUuid());
        if (map == null) return Collections.emptyList();
        return map.values();
    }

    public static void remove(ServerPlayerEntity player, StatusEffect type) {
        Map<StatusEffect, StatusEffectInstance> map = STORAGE.get(player.getUuid());
        if (map != null) map.remove(type);
    }

    public static void clear(ServerPlayerEntity player) {
        STORAGE.remove(player.getUuid());
    }
}
