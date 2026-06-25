package com.glascoss.mixin;

import com.glascoss.GlascossMod;
import com.glascoss.ai.CommandFilter;
import com.glascoss.ai.GeminiClient;
import com.glascoss.ai.ToolRegistry;
import com.glascoss.config.ModConfig;
import com.glascoss.memory.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerChatMixin {
    @Shadow public ServerPlayerEntity player;

    private static final Pattern TOOL_PATTERN = Pattern.compile("\\[TOOL:\\s*(\\w+)\\(([^)]*)\\)\\]");
    private static final Pattern TP_COMMAND = Pattern.compile("^/?(tp |locate |teleport )", Pattern.CASE_INSENSITIVE);

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        String message = packet.chatMessage().trim();

        if (message.startsWith("/")) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        if (ModConfig.isDisableOnOnline() && server.isRemote()) return;

        ci.cancel();

        server.getPlayerManager().broadcast(
                Text.translatable("glascoss.chat.player_chat", player.getName().getString(), message), false);

        if (!GeminiClient.getInstance().isInitialized()) return;

        WorldMemory worldMemory = MemoryManager.getWorldMemory(server.getOverworld());
        ConversationHistory history = worldMemory.getConversationHistory(player.getUuid());

        GeminiClient.getInstance().sendRawPrompt(message, player).thenAccept(response -> {
            if (response == null || response.isBlank()) return;

            CommandFilter.clear();
            String processed = processToolCalls(response, player);
            List<String> firstPassCmds = new ArrayList<>(CommandFilter.getLastCommands());
            List<String> toolErrors = extractErrors(firstPassCmds);
            CommandFilter.clear();

            String finalResponse;
            List<String> allCommands = new ArrayList<>();

            if (!toolErrors.isEmpty()) {
                String errorSummary = toolErrors.stream()
                        .map(e -> "  " + e)
                        .collect(Collectors.joining("\n"));
                String fixPrompt = String.format(
                        "[SYSTEM: Your previous response was: \"%s\"\n" +
                        "The following command errors occurred:\n%s\n" +
                        "Your response was hidden from the player. Respond naturally about the failure and offer to fix it.]\n" +
                        "Player said: %s",
                        response, errorSummary, message);

                try {
                    String fixResponse = GeminiClient.getInstance().sendRawPrompt(fixPrompt, player).get();
                    if (fixResponse != null && !fixResponse.isBlank()) {
                        CommandFilter.clear();
                        String fixed = processToolCalls(fixResponse, player);
                        finalResponse = executeStrayCommands(fixed, player);
                        allCommands.addAll(CommandFilter.getLastCommands());
                    } else {
                        finalResponse = processed;
                        allCommands.addAll(firstPassCmds);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    GlascossMod.LOGGER.error("Agent loop retry failed", e);
                    finalResponse = processed;
                    allCommands.addAll(firstPassCmds);
                }
            } else {
                finalResponse = executeStrayCommands(processed, player);
                allCommands.addAll(firstPassCmds);
                allCommands.addAll(CommandFilter.getLastCommands());
            }

            CommandFilter.clear();

            String finalTrimmed = finalResponse.isBlank() ? "..." : finalResponse;

            server.execute(() -> {
                server.getPlayerManager().broadcast(Text.translatable("glascoss.chat.broadcast", finalTrimmed), false);

                if (ModConfig.isShowCommands() && !allCommands.isEmpty()) {
                    for (String cmd : allCommands) {
                        if (TP_COMMAND.matcher(cmd).find()) continue;
                        if (cmd.startsWith("{log}")) continue;
                        server.getPlayerManager().broadcast(Text.translatable("glascoss.chat.command_display", cmd), false);
                    }
                }

                history.addEntry(new ConversationEntry("player", message));
                history.addEntry(new ConversationEntry("glascoss", finalTrimmed));
                MemoryView.addChatEntry("<" + player.getName().getString() + "> " + message);
                MemoryView.addChatEntry("<Glascoss> " + finalTrimmed);
            });
        }).exceptionally(e -> {
            GlascossMod.LOGGER.error("Async chat error", e);
            return null;
        });
    }

    private String processToolCalls(String response, ServerPlayerEntity player) {
        Matcher toolMatcher = TOOL_PATTERN.matcher(response);
        StringBuffer sb = new StringBuffer();
        while (toolMatcher.find()) {
            String toolName = toolMatcher.group(1);
            String args = toolMatcher.group(2);
            String toolResult = ToolRegistry.executeTool(toolName, args, player.getServerWorld(), player);
            MemoryView.addActionEntry(toolName + "(" + args + ") -> " + truncate(toolResult, 50));
            toolMatcher.appendReplacement(sb, Matcher.quoteReplacement(toolResult));
        }
        toolMatcher.appendTail(sb);
        return sb.toString().trim();
    }

    /** Executes any remaining /command patterns in the AI response silently, removing them from the text */
    private String executeStrayCommands(String text, ServerPlayerEntity player) {
        Pattern cmdPat = Pattern.compile("(?:^|\\s)((?:/[\\w@:-]+)(?:\\s+[^\\s,.!?]+)+)(?=\\s|$|[.,!?])");
        Matcher m = cmdPat.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String command = m.group(1);
            ToolRegistry.executeTool("run_command", "command=" + command, player.getServerWorld(), player);
            m.appendReplacement(sb, Matcher.quoteReplacement(""));
        }
        m.appendTail(sb);
        return sb.toString().trim();
    }

    private String truncate(String text, int maxChars) {
        return text != null && text.length() > maxChars
                ? text.substring(0, maxChars) + "..."
                : (text == null ? "" : text);
    }

    private List<String> extractErrors(List<String> commands) {
        return commands.stream()
                .filter(c -> c.startsWith("{log}"))
                .collect(Collectors.toList());
    }
}
