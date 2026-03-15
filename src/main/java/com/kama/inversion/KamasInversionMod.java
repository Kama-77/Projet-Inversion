package com.kama.inversion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KamasInversionMod implements ModInitializer {
    public static final String MOD_ID = "kamas_inversion_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier TOGGLE_INVERSION = new Identifier(MOD_ID, "toggle_inversion");
    public static final Identifier INVERSION_STATE = new Identifier(MOD_ID, "inversion_state");

    @Override
    public void onInitialize() {
        LOGGER.info("Kama's Inversion Mod a bien ete charge !");

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_INVERSION, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                boolean enabled = InversionState.toggle(player);
                InversionNetworking.sendState(player, enabled);
            });
        });
    }
}

