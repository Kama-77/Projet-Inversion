package com.kama.inversion;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.network.ServerPlayerEntity;

public final class InversionState {
    private static final Set<UUID> ENABLED = ConcurrentHashMap.newKeySet();

    private InversionState() {}

    public static boolean isEnabled(ServerPlayerEntity player) {
        return ENABLED.contains(player.getUuid());
    }

    public static boolean toggle(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        if (ENABLED.contains(id)) {
            ENABLED.remove(id);
            return false;
        }
        ENABLED.add(id);
        return true;
    }
}
