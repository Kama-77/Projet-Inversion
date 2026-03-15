package com.kama.inversion;

public final class InversionHooks {
    private static final ThreadLocal<Boolean> INTERNAL = ThreadLocal.withInitial(() -> false);

    private InversionHooks() {}

    public static boolean isInternal() {
        return INTERNAL.get();
    }

    public static void runInternal(Runnable action) {
        boolean prev = INTERNAL.get();
        INTERNAL.set(true);
        try {
            action.run();
        } finally {
            INTERNAL.set(prev);
        }
    }
}

