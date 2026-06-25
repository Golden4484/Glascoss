package com.glascoss.ai;

import com.glascoss.ai.tools.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

public class ToolRegistry {
    private static final Map<String, Tool> tools = new HashMap<>();

    static {
        register(new PlaceBlockTool());
        register(new MineBlockTool());
        register(new GiveItemTool());
        register(new RunCommandTool());
        register(new FillAreaTool());
        register(new LocateAndTeleportTool());
        register(new CreateFollowerTool());
        register(new StopFollowerTool());
        register(new LookupTool());
        register(new CreateTriggerTool());
        register(new StopTriggersTool());
        register(new WriteBlocksTool());
        register(new ReplaceBuildTool());
        register(new DefineRecipeTool());
        register(new GiveRecipeTool());
        register(new ListRecipesTool());
        register(new DeleteRecipeTool());
        register(new SaveBlueprintTool());
        register(new BuildBlueprintTool());
        register(new ListBlueprintsTool());
        register(new DeleteBlueprintTool());
    }

    public static void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    public static Tool getTool(String name) {
        return tools.get(name);
    }

    public static Map<String, Tool> getAllTools() {
        return tools;
    }

    public static String executeTool(String name, String argsStr, ServerWorld world, PlayerEntity player) {
        Tool tool = tools.get(name);
        if (tool == null) return "Unknown tool: " + name;

        return tool.execute(argsStr, world, player);
    }

    public static String getToolDescriptions() {
        StringBuilder sb = new StringBuilder();
        for (Tool tool : tools.values()) {
            sb.append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
        }
        return sb.toString();
    }
}
