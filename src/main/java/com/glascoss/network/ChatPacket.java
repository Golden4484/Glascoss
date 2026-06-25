package com.glascoss.network;

import com.glascoss.ai.GeminiClient;
import com.glascoss.ai.ToolRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatPacket {
    private static final Pattern TOOL_PATTERN = Pattern.compile("\\[TOOL:\\s*(\\w+)\\(([^)]*)\\)\\]");

    public static void handle(MinecraftServer server, ServerPlayerEntity player,
                               ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        String message = buf.readString();
        if (message == null || message.isEmpty()) return;

        server.execute(() -> {
            GeminiClient gemini = GeminiClient.getInstance();
            ServerWorld world = (ServerWorld) player.getWorld();

            gemini.sendMessage(message, player).thenAccept(response -> {
                server.execute(() -> {
                    Matcher matcher = TOOL_PATTERN.matcher(response);
                    StringBuilder chatResponse = new StringBuilder();
                    int lastEnd = 0;

                    while (matcher.find()) {
                        chatResponse.append(response, lastEnd, matcher.start());
                        lastEnd = matcher.end();

                        String toolName = matcher.group(1);
                        String args = matcher.group(2);
                        String result = ToolRegistry.executeTool(toolName, args, world, player);
                        chatResponse.append("§7[").append(result).append("] ");
                    }

                    chatResponse.append(response, lastEnd, response.length());
                    String finalResponse = chatResponse.toString();

                    PacketByteBuf responseBuf = PacketByteBufs.create();
                    responseBuf.writeString(finalResponse);
                    ServerPlayNetworking.send(player, ModNetworking.CHAT_RESPONSE_CHANNEL, responseBuf);
                });
            });
        });
    }
}
