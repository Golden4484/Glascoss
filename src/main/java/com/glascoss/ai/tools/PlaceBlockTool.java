package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class PlaceBlockTool implements Tool {
    @Override
    public String getName() { return "place_block"; }

    @Override
    public String getDescription() { return "Place a block at specific coordinates. Parameters: x, y, z (ints), block (string with optional block states)"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            BlockPos ppos = player.getBlockPos();
            int x = ArgParser.resolveCoord(args.get("x"), ppos.getX());
            int y = ArgParser.resolveCoord(args.get("y"), ppos.getY());
            int z = ArgParser.resolveCoord(args.get("z"), ppos.getZ());
            String blockId = ArgParser.getString(args, "block", "minecraft:stone");

            BlockState state = parseBlockState(blockId);
            if (state == null) return "Invalid block: " + blockId;

            BlockPos pos = new BlockPos(x, y, z);
            world.setBlockState(pos, state);

            return "Placed " + blockId + " at (" + x + ", " + y + ", " + z + ")";
        } catch (Exception e) {
            return "Failed to place block: " + e.getMessage();
        }
    }

    private BlockState parseBlockState(String input) {
        String blockName;
        String statePart = "";

        int bracketIdx = input.indexOf('[');
        if (bracketIdx > 0) {
            blockName = input.substring(0, bracketIdx);
            statePart = input.substring(bracketIdx + 1, input.lastIndexOf(']'));
        } else {
            blockName = input;
        }

        Identifier id = Identifier.tryParse(blockName);
        if (id == null) return null;
        Block block = Registries.BLOCK.get(id);
        if (block == null || block == net.minecraft.block.Blocks.AIR) return block == net.minecraft.block.Blocks.AIR && !blockName.contains("air") ? null : block.getDefaultState();

        BlockState state = block.getDefaultState();
        if (statePart.isEmpty()) return state;

        for (String pair : statePart.split(",")) {
            pair = pair.trim();
            int eqIdx = pair.indexOf('=');
            if (eqIdx <= 0) continue;
            String key = pair.substring(0, eqIdx).trim();
            String val = pair.substring(eqIdx + 1).trim();

            Property<?> prop = block.getStateManager().getProperty(key);
            if (prop != null) {
                state = applyProperty(state, prop, val);
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<?> prop, String value) {
        Property<T> typedProp = (Property<T>) prop;
        return typedProp.parse(value).map(val -> state.with(typedProp, val)).orElse(state);
    }
}
