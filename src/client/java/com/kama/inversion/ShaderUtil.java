package com.kama.inversion;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ShaderUtil {

    private ShaderUtil() {}

    // Cache des méthodes/champs trouvés pour éviter de chercher à chaque appel
    private static Method cachedLoadMethod = null;
    private static Field cachedPostProcessorField = null;

    public static void loadPostProcessor(GameRenderer renderer, Identifier id) {
        if (cachedLoadMethod == null) {
            for (Method m : GameRenderer.class.getDeclaredMethods()) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 1 && params[0] == Identifier.class
                        && m.getReturnType() == void.class) {
                    m.setAccessible(true);
                    cachedLoadMethod = m;
                    break;
                }
            }
        }

        if (cachedLoadMethod == null) {
            KamasInversionMod.LOGGER.error("Impossible de trouver la méthode loadPostProcessor");
            return;
        }

        try {
            cachedLoadMethod.invoke(renderer, id);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // Ignorer silencieusement les erreurs pendant le chargement des ressources
            KamasInversionMod.LOGGER.debug("loadPostProcessor ignoré (ressources pas prêtes): {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch (Exception e) {
            KamasInversionMod.LOGGER.error("Erreur appel loadPostProcessor: {}", e.getMessage());
        }
    }

    public static boolean isPostProcessorNull(GameRenderer renderer) {
        if (cachedPostProcessorField == null) {
            // Chercher le champ PostEffectProcessor dans GameRenderer
            for (Field f : GameRenderer.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().contains("PostEffect")
                        || f.getType().getSimpleName().contains("class_279")) {
                    f.setAccessible(true);
                    cachedPostProcessorField = f;
                    break;
                }
            }
        }

        if (cachedPostProcessorField == null) return true;

        try {
            return cachedPostProcessorField.get(renderer) == null;
        } catch (Exception e) {
            return true;
        }
    }
}
