package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.Map;

public class GiveItemTool implements Tool {
    @Override
    public String getName() { return "give_item"; }

    @Override
    public String getDescription() { return "Give an item to the player. Parameters: item (string), count (int, default 1)"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String itemId = ArgParser.getString(args, "item", "minecraft:stone");
            int count = ArgParser.getInt(args, "count", 1);

            Identifier id = Identifier.tryParse(itemId);
            if (id == null) return "Invalid item id: " + itemId;
            Item item = Registries.ITEM.get(id);

            ItemStack stack = new ItemStack(item, count);
            player.getInventory().insertStack(stack);

            return "Given " + count + "x " + itemId + " to " + player.getName().getString();
        } catch (Exception e) {
            return "Failed to give item: " + e.getMessage();
        }
    }
}
