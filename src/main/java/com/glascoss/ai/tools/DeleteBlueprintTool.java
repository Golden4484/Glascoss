package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.BlueprintManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class DeleteBlueprintTool implements Tool {
    @Override
    public String getName() { return "delete_blueprint"; }

    @Override
    public String getDescription() { return "Delete a saved blueprint. Parameters: name (string). Use list_blueprints to see all."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String name = ArgParser.getString(args, "name", "").trim();

            if (BlueprintManager.deleteBlueprint(name)) {
                return "Blueprint '" + name + "' deleted.";
            }
            return "Blueprint '" + name + "' not found.";
        } catch (Exception e) {
            return "delete_blueprint error: " + e.getMessage();
        }
    }
}
