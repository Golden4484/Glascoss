package com.glascoss.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ModNetworking {
    public static final Identifier CHAT_CHANNEL = new Identifier("glascoss", "chat");
    public static final Identifier CHAT_RESPONSE_CHANNEL = new Identifier("glascoss", "chat_response");

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(CHAT_CHANNEL, ChatPacket::handle);
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(CHAT_RESPONSE_CHANNEL, ChatResponsePacket::handle);
    }
}
