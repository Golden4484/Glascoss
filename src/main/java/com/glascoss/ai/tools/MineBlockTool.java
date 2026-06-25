package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class MineBlockTool implements Tool {
    @Override
    public String getName() { return "mine_block"; }

    @Override
    public String getDescription() { return "Break a block at specific coordinates. Parameters: x, y, z (ints)"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            BlockPos ppos = player.getBlockPos();
            int x = ArgParser.resolveCoord(args.get("x"), ppos.getX());
            int y = ArgParser.resolveCoord(args.get("y"), ppos.getY());
            int z = ArgParser.resolveCoord(args.get("z"), ppos.getZ());

            BlockPos pos = new BlockPos(x, y, z);
            if (world.isAir(pos)) {
                return "No block at (" + x + ", " + y + ", " + z + ")";
            }

            world.removeBlock(pos, false);
            return "Mined block at (" + x + ", " + y + ", " + z + ")";
        } catch (Exception e) {
            return "Failed to mine block: " + e.getMessage();
        }
    }
}
