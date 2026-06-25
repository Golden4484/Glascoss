package com.glascoss.ai;

import com.glascoss.config.ModConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.List;

public class CommandFilter {
    private static final ThreadLocal<List<String>> lastCommands = ThreadLocal.withInitial(ArrayList::new);

    public static void clear() {
        lastCommands.get().clear();
    }

    public static List<String> getLastCommands() {
        return new ArrayList<>(lastCommands.get());
    }

    public static int executeSilently(String command, ServerWorld world, PlayerEntity player) {
        MinecraftServer server = world.getServer();
        if (command.startsWith("/")) command = command.substring(1);

        lastCommands.get().add("/" + command);

        StringBuilder output = new StringBuilder();
        CommandOutput capture = new CommandOutput() {
            @Override public void sendMessage(Text message) {
                if (output.length() > 0) output.append("\n");
                output.append(message.getString());
            }
            @Override public boolean shouldReceiveFeedback() { return false; }
            @Override public boolean shouldTrackOutput() { return true; }
            @Override public boolean shouldBroadcastConsoleToOps() { return false; }
        };

        ServerCommandSource source = new ServerCommandSource(
                capture,
                player.getPos(),
                Vec2f.ZERO,
                world,
                2,
                player.getName().getString(),
                player.getDisplayName(),
                server,
                null
        );

        int result = server.getCommandManager().executeWithPrefix(source, "/" + command);
        String resultStr = output.toString();

        if (result < 0 || resultStr.toLowerCase().contains("unknown command")
                || resultStr.toLowerCase().contains("comando desconhecido")
                || resultStr.toLowerCase().contains("não encontrado")
                || resultStr.toLowerCase().contains("not found")
                || resultStr.toLowerCase().contains("failed")
                || resultStr.toLowerCase().contains("erro")
                || resultStr.toLowerCase().contains("error")) {
            lastCommands.get().add("{log} " + resultStr);
        }

        return result;
    }
}
