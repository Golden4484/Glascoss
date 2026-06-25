package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.CommandFilter;
import com.glascoss.ai.RecipeManager;
import com.glascoss.ai.RecipeManager.NbtRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class DefineRecipeTool implements Tool {
    @Override
    public String getName() { return "define_recipe"; }

    @Override
    public String getDescription() { return "Create and save an NBT item recipe. Parameters: name (string), item (string), count (int, default 1), nbt (string, NBT JSON). Example: define_recipe(name=supersword, item=minecraft:diamond_sword, nbt={Enchantments:[{id:sharpness,lvl:10}]})"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String name = ArgParser.getString(args, "name", "").trim().replaceAll("[^a-zA-Z0-9_-]", "");
            String item = ArgParser.getString(args, "item", "minecraft:stick");
            int count = ArgParser.getInt(args, "count", 1);
            String nbt = ArgParser.getString(args, "nbt", "");

            if (name.isBlank()) return "Recipe name is required.";
            if (!item.contains(":")) item = "minecraft:" + item;

            count = Math.max(1, Math.min(64, count));

            RecipeManager.saveRecipe(new NbtRecipe(name, item, count, nbt));
            return "Recipe '" + name + "' saved. Use give_recipe(name=" + name + ") to get it.";
        } catch (Exception e) {
            return "define_recipe error: " + e.getMessage();
        }
    }
}
