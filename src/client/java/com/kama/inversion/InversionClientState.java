package com.kama.inversion;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InversionClientState {
    private static boolean enabled = false;

    // Stocke les UUID de tous les joueurs en mode inversé (visible par les autres)
    private static final Set<UUID> INVERTED_PLAYERS = ConcurrentHashMap.newKeySet();

    private InversionClientState() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static void setPlayerInverted(UUID uuid, boolean inverted) {
        if (inverted) {
            INVERTED_PLAYERS.add(uuid);
        } else {
            INVERTED_PLAYERS.remove(uuid);
        }
    }

    public static boolean isPlayerInverted(UUID uuid) {
        return INVERTED_PLAYERS.contains(uuid);
    }

    public static void clearAll() {
        INVERTED_PLAYERS.clear();
    }
}
