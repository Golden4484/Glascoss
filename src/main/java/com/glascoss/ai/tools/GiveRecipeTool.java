package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.CommandFilter;
import com.glascoss.ai.RecipeManager;
import com.glascoss.ai.RecipeManager.NbtRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class GiveRecipeTool implements Tool {
    @Override
    public String getName() { return "give_recipe"; }

    @Override
    public String getDescription() { return "Give an item from a saved NBT recipe. Parameters: name (string). Use define_recipe first to create recipes."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String name = ArgParser.getString(args, "name", "").trim();

            NbtRecipe recipe = RecipeManager.getRecipe(name);
            if (recipe == null) {
                return "Recipe '" + name + "' not found. Available: " +
                    RecipeManager.getAllRecipes().stream().map(r -> r.name).reduce((a, b) -> a + ", " + b).orElse("(none)");
            }

            String giveCmd;
            if (recipe.nbt != null && !recipe.nbt.isBlank()) {
                giveCmd = String.format("give @p %s%s %d", recipe.item, recipe.nbt, recipe.count);
            } else {
                giveCmd = String.format("give @p %s %d", recipe.item, recipe.count);
            }

            CommandFilter.executeSilently(giveCmd, world, player);
            return "";
        } catch (Exception e) {
            return "give_recipe error: " + e.getMessage();
        }
    }
}
