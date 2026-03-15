package com.kama.inversion;

public final class InversionClientState {
    private static boolean enabled = false;

    private InversionClientState() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }
}

