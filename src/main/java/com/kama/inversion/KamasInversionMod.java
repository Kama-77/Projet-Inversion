package com.kama.inversion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KamasInversionMod implements ModInitializer {
    public static final String MOD_ID = "kamas_inversion_mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier TOGGLE_INVERSION           = new Identifier(MOD_ID, "toggle_inversion");
    public static final Identifier INVERSION_STATE            = new Identifier(MOD_ID, "inversion_state");
    public static final Identifier INVERSION_STATE_BROADCAST  = new Identifier(MOD_ID, "inversion_state_broadcast");

    @Override
    public void onInitialize() {
        LOGGER.info("Kama's Inversion Mod a bien ete charge !");

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_INVERSION, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                // 1. Changer l'état EN PREMIER
                boolean enabled = InversionState.toggle(player);

                // 2. Inverser les effets APRÈS le toggle (swapPlayerEffects lit isEnabled())
                InversionEffects.swapPlayerEffects(player);

                // 3. Envoyer l'état au joueur lui-même (pour le shader)
                InversionNetworking.sendState(player, enabled);

                // 4. Broadcaster UUID + état à TOUS les joueurs (pour le rendu du skin)
                PacketByteBuf broadcastBuf = PacketByteBufs.create();
                broadcastBuf.writeUuid(player.getUuid());
                broadcastBuf.writeBoolean(enabled);
                for (ServerPlayerEntity other : PlayerLookup.all(server)) {
                    ServerPlayNetworking.send(other, INVERSION_STATE_BROADCAST, PacketByteBufs.copy(broadcastBuf));
                }
            });
        });
    }
}
