package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class StopFollowerTool implements Tool {
    @Override
    public String getName() { return "stop_followers"; }

    @Override
    public String getDescription() { return "Stop all active followers."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            MinecraftServer server = world.getServer();
            ServerCommandSource source = player.getCommandSource().withEntity(player).withLevel(2);

            server.getCommandManager().executeWithPrefix(source, "/kill @e[tag=glascoss_follower]");
            server.getCommandManager().executeWithPrefix(source, "/schedule clear glascoss:follow");

            return "All followers dismissed.";
        } catch (Exception e) {
            return "stop_followers error: " + e.getMessage();
        }
    }
}
