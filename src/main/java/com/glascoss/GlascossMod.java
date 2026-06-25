package com.glascoss;

import com.glascoss.ai.BlueprintManager;
import com.glascoss.ai.GeminiClient;
import com.glascoss.ai.PersonalityManager;
import com.glascoss.ai.RecipeManager;
import com.glascoss.command.GlascossCommand;
import com.glascoss.config.ModConfig;
import com.glascoss.memory.MemoryManager;
import com.glascoss.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlascossMod implements ModInitializer {
    public static final String MOD_ID = "glascoss";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Glascoss");

        ModConfig.load();
        PersonalityManager.load();
        RecipeManager.load();
        BlueprintManager.load();

        String apiKey = ModConfig.getApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            GeminiClient.getInstance().setApiKey(apiKey);
        }

        ModNetworking.registerServer();
        MemoryManager.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                GlascossCommand.register(dispatcher)
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("Saving Glascoss memory...");
            MemoryManager.saveAll(server);
        });

        LOGGER.info("Glascoss initialized!");
    }
}
