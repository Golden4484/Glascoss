package com.glascoss.ai.tools;

import com.glascoss.ai.RecipeManager;
import com.glascoss.ai.RecipeManager.NbtRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;

public class ListRecipesTool implements Tool {
    @Override
    public String getName() { return "list_recipes"; }

    @Override
    public String getDescription() { return "List all saved NBT recipes. No parameters needed."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        List<NbtRecipe> all = RecipeManager.getAllRecipes();
        if (all.isEmpty()) {
            return "No recipes saved. Use define_recipe to create one.";
        }
        StringBuilder sb = new StringBuilder("Saved recipes:\n");
        for (NbtRecipe r : all) {
            sb.append("- ").append(r.name).append(": ").append(r.item);
            if (r.count > 1) sb.append(" x").append(r.count);
            sb.append("\n");
        }
        return sb.toString().trim();
    }
}
