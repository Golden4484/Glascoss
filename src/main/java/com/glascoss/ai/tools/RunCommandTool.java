package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.CommandFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.Set;

public class RunCommandTool implements Tool {
    private static final Set<String> BLOCKED = Set.of(
            "stop", "ban", "ban-ip", "pardon", "pardon-ip",
            "op", "deop", "whitelist"
    );

    @Override
    public String getName() { return "run_command"; }

    @Override
    public String getDescription() { return "Execute any Minecraft command. Parameters: command (string)"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String command = ArgParser.getString(args, "command", "").trim();

            if (command.isBlank()) return "No command provided.";
            if (command.startsWith("/")) command = command.substring(1);

            String baseCommand = command.split(" ")[0].toLowerCase();
            if (BLOCKED.contains(baseCommand)) {
                return "Command not allowed: /" + baseCommand;
            }

            int result = CommandFilter.executeSilently(command, world, player);
            return "";
        } catch (Exception e) {
            return "Command error: " + e.getMessage();
        }
    }
}
