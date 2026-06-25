package com.glascoss.ai.tools;

import com.glascoss.GlascossMod;
import com.glascoss.ai.ArgParser;
import com.glascoss.ai.CommandFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CreateFollowerTool implements Tool {
    private static boolean datapackLoaded = false;

    @Override
    public String getName() { return "create_follower"; }

    @Override
    public String getDescription() { return "Summon a mob that follows you in a loop. Parameters: entity (string), name (string). Use stop_followers to stop."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String entityType = ArgParser.getString(args, "entity", "minecraft:zombie");
            String name = ArgParser.getString(args, "name", "Seguidor");

            if (!entityType.contains(":")) entityType = "minecraft:" + entityType;

            MinecraftServer server = world.getServer();

            try {
                String summon = String.format(
                    "summon %s ~ ~1 ~ {Tags:[\"glascoss_follower\"],CustomName:'\"%s\"',CustomNameVisible:1b,PersistenceRequired:1b,Silent:1b,CanPickUpLoot:0b,Attributes:[{Name:\"generic.follow_range\",Base:64}]}",
                    entityType, name
                );
                int result = CommandFilter.executeSilently(summon, world, player);
                if (result < 0) throw new RuntimeException("Summon failed: " + result);
            } catch (Exception e) {
                return "Failed to summon " + entityType + ". Use basic entity names like minecraft:zombie, minecraft:armor_stand, minecraft:wolf.";
            }

            if (!datapackLoaded) {
                if (!ensureFollowerDatapack(server, world)) {
                    return "Summoned " + name + " but the follow loop failed to start. Try again or use stop_followers.";
                }
                datapackLoaded = true;
            }

            try {
                server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/schedule function glascoss:follow 1t");
            } catch (Exception e) {
                GlascossMod.LOGGER.error("schedule failed, trying reload", e);
                server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/reload");
                try {
                    server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/schedule function glascoss:follow 1t");
                } catch (Exception e2) {
                    return "Summoned " + name + " but follow loop unavailable. Try again.";
                }
            }

            return "Summoned " + name + " (" + entityType + ") to follow you. Use stop_followers to dismiss.";
        } catch (Exception e) {
            GlascossMod.LOGGER.error("create_follower error", e);
            return "Failed to create follower: " + e.getMessage();
        }
    }

    private boolean ensureFollowerDatapack(MinecraftServer server, ServerWorld world) {
        try {
            Path packsDir = server.getSavePath(WorldSavePath.DATAPACKS);
            Path dpDir = packsDir.resolve("glascoss_follow");

            if (Files.exists(dpDir.resolve("pack.mcmeta"))) {
                return true;
            }

            Path funcDir = dpDir.resolve("data").resolve("glascoss").resolve("functions");
            Files.createDirectories(funcDir);

            Files.writeString(dpDir.resolve("pack.mcmeta"),
                    "{\"pack\":{\"pack_format\":15,\"description\":\"Glascoss follower loop\"}}");

            Files.writeString(funcDir.resolve("follow.mcfunction"),
                    "execute as @e[tag=glascoss_follower] at @s run tp @s @p\n" +
                    "schedule function glascoss:follow 1t\n");

            server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/reload");
            return true;
        } catch (Exception e) {
            GlascossMod.LOGGER.error("Failed to create follower datapack", e);
            return false;
        }
    }
}
