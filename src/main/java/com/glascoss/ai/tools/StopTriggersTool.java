package com.glascoss.ai.tools;

import com.glascoss.ai.CommandFilter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;

public class StopTriggersTool implements Tool {
    @Override
    public String getName() { return "stop_triggers"; }

    @Override
    public String getDescription() { return "Stop all active triggers and dismiss trigger entities."; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            CommandFilter.executeSilently("kill @e[tag=glascoss_trigger]", world, player);
            CommandFilter.executeSilently("schedule clear glascoss:triggers", world, player);
            return "All triggers stopped and entities dismissed.";
        } catch (Exception e) {
            return "stop_triggers error: " + e.getMessage();
        }
    }
}
