package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.CommandFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CreateTriggerTool implements Tool {
    private static boolean datapackLoaded = false;

    @Override
    public String getName() { return "create_trigger"; }

    @Override
    public String getDescription() { return "Create a trigger that runs a command when a condition is met. Parameters: type (proximity), command (string), distance (int, default 15), entity (string, default minecraft:zombie). Use stop_triggers to stop."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String type = ArgParser.getString(args, "type", "proximity");
            String command = ArgParser.getString(args, "command", "tp @s @p");
            int distance = ArgParser.getInt(args, "distance", 15);
            String entityType = ArgParser.getString(args, "entity", "minecraft:zombie");

            if (!entityType.contains(":")) entityType = "minecraft:" + entityType;

            MinecraftServer server = world.getServer();

            // Summon the entity
            String summon = String.format(
                "summon %s ~ ~1 ~ {Tags:[\"glascoss_trigger\"],CustomNameVisible:0b,PersistenceRequired:1b,Silent:1b,NoAI:1b}",
                entityType
            );
            CommandFilter.executeSilently(summon, world, player);

            if (!ensureTriggerDatapack(server, world, command, distance, player)) {
                return "Trigger created but the loop may not work. Try /reload.";
            }
            datapackLoaded = true;

            CommandFilter.executeSilently("schedule function glascoss:triggers 1t", world, player);

            return "Trigger created: " + entityType + " will execute '/" + command + "' when you are " + distance + "+ blocks away. Use stop_triggers to stop.";
        } catch (Exception e) {
            return "create_trigger error: " + e.getMessage();
        }
    }

    private boolean ensureTriggerDatapack(MinecraftServer server, ServerWorld world, String triggerCommand, int distance, PlayerEntity player) {
        try {
            Path packsDir = server.getSavePath(net.minecraft.util.WorldSavePath.DATAPACKS);
            Path dpDir = packsDir.resolve("glascoss_triggers");

            if (!Files.exists(dpDir.resolve("pack.mcmeta"))) {
                Path funcDir = dpDir.resolve("data").resolve("glascoss").resolve("functions");
                Files.createDirectories(funcDir);

                Files.writeString(dpDir.resolve("pack.mcmeta"),
                        "{\"pack\":{\"pack_format\":15,\"description\":\"Glascoss trigger loop\"}}");

                String cmd = triggerCommand.replace("\"", "\\\"");
                Files.writeString(funcDir.resolve("triggers.mcfunction"),
                    "execute as @e[tag=glascoss_trigger] at @s if entity @p[distance=.." + distance + "] run " + cmd + "\n" +
                    "schedule function glascoss:triggers 1t\n");

                CommandFilter.executeSilently("reload", world, player);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
