package com.kama.inversion;

import com.kama.inversion.ShaderUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class KamasInversionClient implements ClientModInitializer {
    private static final String CATEGORY = "key.categories.kamas_inversion_mod";

    private static final Identifier INVERT_SHADER =
            new Identifier(KamasInversionMod.MOD_ID, "shaders/post/invert.json");

    private static final KeyBinding TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "key.kamas_inversion_mod.toggle_inversion",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    CATEGORY
            )
    );

    @Override
    public void onInitializeClient() {

        // Packet pour le joueur lui-même : shader + message
        ClientPlayNetworking.registerGlobalReceiver(KamasInversionMod.INVERSION_STATE, (client, handler, buf, responseSender) -> {
            boolean enabled = buf.readBoolean();
            client.execute(() -> {
                InversionClientState.setEnabled(enabled);

                if (enabled) {
                    try {
                        ShaderUtil.loadPostProcessor(client.gameRenderer, INVERT_SHADER);
                    } catch (Exception e) {
                        KamasInversionMod.LOGGER.error("Erreur chargement shader: {}", e.getMessage());
                    }
                } else {
                    try {
                        client.gameRenderer.disablePostProcessor();
                    } catch (Exception e) {
                        KamasInversionMod.LOGGER.error("Erreur disable shader: {}", e.getMessage());
                    }
                }

                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null) {
                    mc.player.sendMessage(Text.literal("Mode inverse: " + (enabled ? "ON" : "OFF")), true);
                }
            });
        });

        // Packet broadcast : mettre à jour l'état des autres joueurs pour le rendu du skin
        ClientPlayNetworking.registerGlobalReceiver(KamasInversionMod.INVERSION_STATE_BROADCAST, (client, handler, buf, responseSender) -> {
            UUID uuid = buf.readUuid();
            boolean enabled = buf.readBoolean();
            client.execute(() -> {
                InversionClientState.setPlayerInverted(uuid, enabled);
            });
        });

        // Tick event pour maintenir le shader actif après un changement de perspective (F5)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (client.world == null) return;
            if (!InversionClientState.isEnabled()) return;
            if (client.gameRenderer == null) return;

            // Recharger le shader seulement si le post-processor a été réinitialisé
            if (ShaderUtil.isPostProcessorNull(client.gameRenderer)) {
                ShaderUtil.loadPostProcessor(client.gameRenderer, INVERT_SHADER);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_KEY.wasPressed()) {
                if (client.getNetworkHandler() == null) return;
                ClientPlayNetworking.send(KamasInversionMod.TOGGLE_INVERSION, PacketByteBufs.empty());
            }
        });
    }
}
