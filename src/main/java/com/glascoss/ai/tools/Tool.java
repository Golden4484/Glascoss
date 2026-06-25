package com.glascoss.ai.tools;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface Tool {
    String getName();
    String getDescription();
    String execute(String argsJson, ServerWorld world, PlayerEntity player);
}
