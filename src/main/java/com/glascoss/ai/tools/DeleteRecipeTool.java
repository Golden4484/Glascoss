package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.RecipeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class DeleteRecipeTool implements Tool {
    @Override
    public String getName() { return "delete_recipe"; }

    @Override
    public String getDescription() { return "Delete a saved NBT recipe. Parameters: name (string). Use list_recipes to see all."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String name = ArgParser.getString(args, "name", "").trim();

            if (RecipeManager.deleteRecipe(name)) {
                return "Recipe '" + name + "' deleted.";
            }
            return "Recipe '" + name + "' not found.";
        } catch (Exception e) {
            return "delete_recipe error: " + e.getMessage();
        }
    }
}
