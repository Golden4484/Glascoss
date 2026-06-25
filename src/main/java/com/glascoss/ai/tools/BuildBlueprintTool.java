package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.BlueprintManager;
import com.glascoss.ai.BlueprintManager.Blueprint;
import com.glascoss.ai.BlueprintManager.Step;
import com.glascoss.ai.CommandFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class BuildBlueprintTool implements Tool {
    @Override
    public String getName() { return "build_blueprint"; }

    @Override
    public String getDescription() { return "Build a saved blueprint at a location. Parameters: name (string), x (int), y (int), z (int), direction (north/south/east/west, default same as player)"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String name = ArgParser.getString(args, "name", "").trim();
            int originX = ArgParser.resolveCoord(ArgParser.getString(args, "x", "~"), (int) player.getX());
            int originY = ArgParser.resolveCoord(ArgParser.getString(args, "y", "~"), (int) player.getY());
            int originZ = ArgParser.resolveCoord(ArgParser.getString(args, "z", "~"), (int) player.getZ());
            String direction = ArgParser.getString(args, "direction", getFacing(player.getYaw()));

            Blueprint bp = BlueprintManager.getBlueprint(name);
            if (bp == null) {
                return "Blueprint '" + name + "' not found. Use save_blueprint first or list_blueprints to see saved ones.";
            }

            int dx = 0, dz = 0;
            switch (direction) {
                case "north" -> dz = -1;
                case "south" -> dz = 1;
                case "east" -> dx = 1;
                case "west" -> dx = -1;
                default -> { dz = -1; direction = "north"; }
            }

            int blocksPlaced = 0;
            int stepsDone = 0;

            for (Step step : bp.steps) {
                String cmd;
                if (step.type.equals("fill")) {
                    int fx1 = originX + Math.min(step.x1, step.x2) * dx;
                    int fz1 = originZ + Math.min(step.z1, step.z2) * dz;
                    int fx2 = originX + Math.max(step.x1, step.x2) * dx;
                    int fz2 = originZ + Math.max(step.z1, step.z2) * dz;
                    if (dx == 0) { fx1 = originX + Math.min(step.x1, step.x2); fx2 = originX + Math.max(step.x1, step.x2); }
                    if (dz == 0) { fz1 = originZ + Math.min(step.z1, step.z2); fz2 = originZ + Math.max(step.z1, step.z2); }

                    int fy1 = originY + Math.min(step.y1, step.y2);
                    int fy2 = originY + Math.max(step.y1, step.y2);
                    int minX = Math.min(fx1, fx2), maxX = Math.max(fx1, fx2);
                    int minY = Math.min(fy1, fy2), maxY = Math.max(fy1, fy2);
                    int minZ = Math.min(fz1, fz2), maxZ = Math.max(fz1, fz2);
                    cmd = String.format("fill %d %d %d %d %d %d %s", minX, minY, minZ, maxX, maxY, maxZ, step.block);
                } else {
                    int px = originX + step.x * dx;
                    int pz = originZ + step.z * dz;
                    if (dx == 0) px = originX + step.x;
                    if (dz == 0) pz = originZ + step.z;
                    int py = originY + step.y;

                    // Handle block states that include facing — rotate the facing
                    String block = step.block;
                    if (dx != 0 || dz != 0) {
                        block = rotateFacing(block, direction);
                    }
                    cmd = String.format("setblock %d %d %d %s", px, py, pz, block);
                }

                int result = CommandFilter.executeSilently(cmd, world, player);
                if (result > 0) {
                    blocksPlaced++;
                }
                stepsDone++;
            }

            return "Built '" + name + "' at [" + originX + ", " + originY + ", " + originZ + "] facing " + direction + ". " + stepsDone + " steps, ~" + blocksPlaced + " blocks placed.";
        } catch (Exception e) {
            return "build_blueprint error: " + e.getMessage();
        }
    }

    private String getFacing(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw < 45 || yaw >= 315) return "south";
        if (yaw < 135) return "west";
        if (yaw < 225) return "north";
        return "east";
    }

    private String rotateFacing(String block, String direction) {
        // Simple rotation of facing=north/south/east/west in block states
        if (!block.contains("facing=")) return block;

        String[] dirs = {"north", "east", "south", "west"};
        java.util.Map<String, String> rotation = java.util.Map.of(
            "north", "east", "east", "south", "south", "west", "west", "north"
        );

        for (String from : dirs) {
            if (block.contains("facing=" + from)) {
                String to = rotation.get(from);
                return block.replace("facing=" + from, "facing=" + to);
            }
        }
        return block;
    }
}
