package com.glascoss.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class GlascossCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("glascoss")
                .then(CommandManager.literal("apikey")
                        .then(CommandManager.argument("key", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String key = StringArgumentType.getString(ctx, "key");
                                    com.glascoss.config.ModConfig.setApiKey(key);
                                    com.glascoss.ai.GeminiClient.getInstance().setApiKey(key);
                                    ctx.getSource().sendFeedback(() ->
                                            net.minecraft.text.Text.translatable("commands.glascoss.apikey.success"), false);
                                    return 1;
                                })))
        );
    }
}
