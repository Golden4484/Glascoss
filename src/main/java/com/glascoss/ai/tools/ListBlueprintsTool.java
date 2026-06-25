package com.glascoss.ai.tools;

import com.glascoss.ai.BlueprintManager;
import com.glascoss.ai.BlueprintManager.Blueprint;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class ListBlueprintsTool implements Tool {
    @Override
    public String getName() { return "list_blueprints"; }

    @Override
    public String getDescription() { return "List all saved construction blueprints. No parameters needed."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        List<Blueprint> all = BlueprintManager.getAllBlueprints();
        if (all.isEmpty()) {
            return "No blueprints saved. Use save_blueprint to create one.";
        }
        StringBuilder sb = new StringBuilder("Saved blueprints:\n");
        for (Blueprint b : all) {
            sb.append("- ").append(b.name).append(" (").append(b.steps.size()).append(" steps)\n");
        }
        return sb.toString().trim();
    }
}
