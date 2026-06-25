package com.glascoss.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class ChatResponsePacket {
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        String response = buf.readString();

        client.execute(() -> {
            if (client.player != null) {
                client.player.sendMessage(
                        Text.literal("§b[Glascoss] §f" + response),
                        false
                );
            }
        });
    }
}
