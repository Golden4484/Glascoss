package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.CommandFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class ReplaceBuildTool implements Tool {
    @Override
    public String getName() { return "replace_build"; }

    @Override
    public String getDescription() { return "Replace blocks in a previously built structure. Parameters: x1, y1, z1, x2, y2, z2 (int), block (string). Use the same coordinates as the original fill_area."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            int x1 = ArgParser.resolveCoord(ArgParser.getString(args, "x1", "~"), (int) player.getX());
            int y1 = ArgParser.resolveCoord(ArgParser.getString(args, "y1", "~"), (int) player.getY());
            int z1 = ArgParser.resolveCoord(ArgParser.getString(args, "z1", "~"), (int) player.getZ());
            int x2 = ArgParser.resolveCoord(ArgParser.getString(args, "x2", "~"), (int) player.getX());
            int y2 = ArgParser.resolveCoord(ArgParser.getString(args, "y2", "~"), (int) player.getY());
            int z2 = ArgParser.resolveCoord(ArgParser.getString(args, "z2", "~"), (int) player.getZ());
            String block = ArgParser.getString(args, "block", "minecraft:stone");

            if (!block.contains(":")) block = "minecraft:" + block;

            // Sort coordinates
            int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

            String fillCmd = String.format("fill %d %d %d %d %d %d %s replace",
                    minX, minY, minZ, maxX, maxY, maxZ, block);

            int result = CommandFilter.executeSilently(fillCmd, world, player);
            if (result <= 0) {
                return "Replace failed. Try a simpler block name.";
            }

            int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
            return "Replaced " + volume + " blocks with " + block + " in the specified area.";
        } catch (Exception e) {
            return "replace_build error: " + e.getMessage();
        }
    }
}
