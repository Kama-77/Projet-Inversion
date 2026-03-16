package com.kama.inversion;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public final class InversionNetworking {
    private InversionNetworking() {}

    public static void sendState(ServerPlayerEntity player, boolean enabled) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(enabled);
        ServerPlayNetworking.send(player, KamasInversionMod.INVERSION_STATE, buf);
    }
}

