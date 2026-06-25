package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.CommandFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class FillAreaTool implements Tool {
    @Override
    public String getName() { return "fill_area"; }

    @Override
    public String getDescription() { return "Fill a region with blocks. Parameters: x1, y1, z1, x2, y2, z2 (ints, supports ~ syntax), block (string)"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            BlockPos ppos = player.getBlockPos();
            int x1 = ArgParser.resolveCoord(args.get("x1"), ppos.getX());
            int y1 = ArgParser.resolveCoord(args.get("y1"), ppos.getY());
            int z1 = ArgParser.resolveCoord(args.get("z1"), ppos.getZ());
            int x2 = ArgParser.resolveCoord(args.get("x2"), ppos.getX());
            int y2 = ArgParser.resolveCoord(args.get("y2"), ppos.getY());
            int z2 = ArgParser.resolveCoord(args.get("z2"), ppos.getZ());
            String block = ArgParser.getString(args, "block", "minecraft:stone");

            int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
            int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);

            String cmd = String.format("fill %d %d %d %d %d %d %s", minX, minY, minZ, maxX, maxY, maxZ, block);
            int result = CommandFilter.executeSilently(cmd, world, player);

            if (result > 0) {
                int count = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
                return "Filled " + count + "x " + block + " from (" + minX + "," + minY + "," + minZ + ") to (" + maxX + "," + maxY + "," + maxZ + ")";
            }
            return "Failed to fill area. Check coordinates and block name.";
        } catch (Exception e) {
            return "fill_area error: " + e.getMessage();
        }
    }
}
