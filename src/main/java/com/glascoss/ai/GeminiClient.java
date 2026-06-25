package com.glascoss.ai;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import com.glascoss.GlascossMod;
import com.glascoss.config.ModConfig;
import com.glascoss.memory.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GeminiClient {
    private Client client;
    private String apiKey;
    private String model;
    private boolean initialized = false;

    private static GeminiClient instance;

    public static GeminiClient getInstance() {
        if (instance == null) instance = new GeminiClient();
        return instance;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        this.model = ModConfig.getModel();
        this.initialized = false;

        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                this.client = Client.builder().apiKey(apiKey).build();
                this.initialized = true;
            } catch (Exception e) {
                GlascossMod.LOGGER.error("Failed to initialize Gemini client", e);
                this.initialized = false;
            }
        } else {
            this.client = null;
        }
    }

    public boolean isInitialized() {
        return initialized && client != null;
    }

    public String detectPlayerLanguage() {
        try {
            return MinecraftClient.getInstance().getLanguageManager().getLanguage();
        } catch (Exception e) {
            return "en_us";
        }
    }

    public CompletableFuture<String> sendMessage(String userMessage, PlayerEntity player) {
        return sendWithHistory(userMessage, player, true);
    }

    public CompletableFuture<String> sendRawPrompt(String prompt, PlayerEntity player) {
        return sendWithHistory(prompt, player, false);
    }

    private CompletableFuture<String> sendWithHistory(String input, PlayerEntity player, boolean saveToHistory) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isInitialized()) {
                return Text.translatable("glascoss.chat.api_not_configured").getString();
            }

            try {
                ServerWorld world = (ServerWorld) player.getWorld();
                String worldId = MemoryManager.getWorldId(world);
                WorldMemory worldMemory = MemoryManager.getWorldMemory(world);
                ConversationHistory history = worldMemory.getConversationHistory(player.getUuid());

                String language = detectPlayerLanguage();
                int trustScore = PersonalityEngine.getTrustScore(worldId, player.getUuid());
                String mood = PersonalityEngine.getCurrentMood(worldId, player.getUuid());
                WorldMemory.SemanticSummary semantic = worldMemory.getSemanticSummary();

                String dynamicPrompt = PersonalityEngine.generateDynamicPrompt(player, trustScore, language, semantic, worldMemory);

                List<Content> contents = new ArrayList<>();

                if (history.hasSummary()) {
                    contents.add(Content.builder()
                            .role("user")
                            .parts(List.of(Part.fromText(
                                    "[SYSTEM: Previous context summary: " + history.getSummary() + "]"
                            )))
                            .build());
                    contents.add(Content.builder()
                            .role("model")
                            .parts(List.of(Part.fromText("Understood. I remember that context.")))
                            .build());
                }

                for (ConversationEntry entry : history.getRecentEntries(15)) {
                    String role = entry.getSpeaker().equals("player") ? "user" : "model";
                    contents.add(Content.builder()
                            .role(role)
                            .parts(List.of(Part.fromText(entry.getMessage())))
                            .build());
                }

                List<String> relevantFacts = worldMemory.getRelevantFacts(input);
                StringBuilder contextMsg = new StringBuilder();
                boolean isDay = world.getTimeOfDay() % 24000 < 12000;
                String facing = getFacing(player.getYaw());
                String biome = world.getBiome(player.getBlockPos()).getKey()
                        .map(key -> key.getValue().toString().replace("minecraft:", ""))
                        .orElse("unknown");
                contextMsg.append("[subject=").append(player.getName().getString())
                        .append(", pos=").append(player.getBlockPos().toShortString())
                        .append(", facing=").append(facing)
                        .append(", ").append(getDirectionOffsets(player.getYaw()))
                        .append(", biome=").append(biome)
                        .append(", day=").append(isDay)
                        .append(", lang=").append(language)
                        .append(", trust=").append(trustScore)
                        .append(", mood=").append(mood)
                        .append(", hp=").append(String.format("%.1f", player.getHealth()))
                        .append("/").append(String.format("%.1f", player.getMaxHealth()))
                        .append(", hunger=").append(player.getHungerManager().getFoodLevel())
                        .append(", saturation=").append(String.format("%.1f", player.getHungerManager().getSaturationLevel()))
                        .append(", armor=").append(player.getArmor());

                var mainHand = player.getMainHandStack();
                if (!mainHand.isEmpty()) {
                    contextMsg.append(", hand=").append(mainHand.getItem().toString().replace("minecraft:", ""));
                }
                var offHand = player.getOffHandStack();
                if (!offHand.isEmpty()) {
                    contextMsg.append(", offhand=").append(offHand.getItem().toString().replace("minecraft:", ""));
                }
                var effects = player.getActiveStatusEffects();
                if (!effects.isEmpty()) {
                    contextMsg.append(", effects=");
                    boolean first = true;
                    for (var entry : effects.entrySet()) {
                        if (!first) contextMsg.append(";");
                        first = false;
                        var instance = entry.getValue();
                        contextMsg.append(entry.getKey().toString().replace("minecraft:", ""))
                                .append(" ").append(instance.getAmplifier() + 1)
                                .append("(").append(instance.getDuration() / 20).append("s)");
                    }
                }
                if (worldMemory.consumeDied(player.getUuid())) {
                    contextMsg.append(", died=true");
                }

                if (!relevantFacts.isEmpty()) {
                    contextMsg.append(", memory=").append(String.join("; ", relevantFacts));
                }
                contextMsg.append("] ").append(input);

                contents.add(Content.builder()
                        .role("user")
                        .parts(List.of(Part.fromText(contextMsg.toString())))
                        .build());

                GenerateContentConfig config = GenerateContentConfig.builder()
                        .systemInstruction(Content.fromParts(Part.fromText(dynamicPrompt)))
                        .build();

                StringBuilder response = new StringBuilder();
                ResponseStream<GenerateContentResponse> stream = client.models.generateContentStream(
                        model, contents, config
                );

                for (GenerateContentResponse res : stream) {
                    if (res.candidates() == null || res.candidates().isEmpty()) continue;
                    var candidate = res.candidates().get().get(0);
                    if (candidate.content() == null || candidate.content().isEmpty()) continue;
                    var content = candidate.content().get();
                    if (content.parts() == null || content.parts().isEmpty()) continue;
                    for (Part part : content.parts().get()) {
                        part.text().ifPresent(response::append);
                    }
                }
                stream.close();

                String aiResponse = response.toString().trim();
                if (aiResponse.isBlank()) return "...";

                if (saveToHistory) {
                    history.addEntry(new ConversationEntry("player", input));
                    history.addEntry(new ConversationEntry("glascoss", aiResponse));
                    worldMemory.incrementInteractionCount(player.getUuid());

                    PersonalityEngine.addInteraction(worldId, player.getUuid());
                    if (isPolite(input)) {
                        PersonalityEngine.addPoliteInteraction(worldId, player.getUuid());
                    }
                }

                return aiResponse;

            } catch (Exception e) {
                GlascossMod.LOGGER.error("Gemini API error", e);
                String msg = e.getMessage();
                if (msg != null && (msg.contains("Quota") || msg.contains("quota") || msg.contains("rate") || msg.contains("RESOURCE_EXHAUSTED"))) {
                    String delay = "?";
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("retry in (\\d+\\.?\\d*)s").matcher(msg);
                    if (m.find()) delay = m.group(1) + "s";
                    return Text.translatable("glascoss.chat.error_quota", delay).getString();
                }
                return Text.translatable("glascoss.chat.error_generic", msg != null ? msg : "unknown").getString();
            }
        });
    }

    private boolean isPolite(String msg) {
        if (msg == null) return false;
        String lower = msg.toLowerCase().trim();
        return lower.contains("please") || lower.contains("obrigado") || lower.contains("obrigada")
                || lower.contains("desculpa") || lower.contains("desculpe") || lower.contains("sorry")
                || lower.contains("thank") || lower.contains("thanks")
                || lower.contains("por favor") || lower.contains("pfv")
                || lower.contains("please") || lower.contains("pls")
                || lower.contains("poderia") || lower.contains("pode me")
                || lower.startsWith("vc pode") || lower.startsWith("voce pode");
    }

    public void setModel(String model) { this.model = model; }
    public String getModel() { return model; }

    private String getFacing(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw < 22.5 || yaw >= 337.5) return "S";
        if (yaw < 67.5) return "SW";
        if (yaw < 112.5) return "W";
        if (yaw < 157.5) return "NW";
        if (yaw < 202.5) return "N";
        if (yaw < 247.5) return "NE";
        if (yaw < 292.5) return "E";
        return "SE";
    }

    /**
     * Returns a string like "dirs: front=~ ~ ~-D, back=~ ~ ~+D, right=~+D ~ ~, left=~-D ~ ~"
     * where D is a placeholder you replace with the distance in blocks.
     * e.g. "frente 5" → front=~ ~ ~-5 if facing north.
     */
    private String getDirectionOffsets(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw < 22.5 || yaw >= 337.5) { // S
            return "dirs: front=~ ~ ~+D, back=~ ~ ~-D, right=~-D ~ ~, left=~+D ~ ~";
        } else if (yaw < 67.5) { // SW
            return "dirs: front=~+D ~ ~+D, back=~-D ~ ~-D, right=~-D ~ ~+D, left=~+D ~ ~-D";
        } else if (yaw < 112.5) { // W
            return "dirs: front=~-D ~ ~, back=~+D ~ ~, right=~ ~ ~-D, left=~ ~ ~+D";
        } else if (yaw < 157.5) { // NW
            return "dirs: front=~-D ~ ~-D, back=~+D ~ ~+D, right=~+D ~ ~-D, left=~-D ~ ~+D";
        } else if (yaw < 202.5) { // N
            return "dirs: front=~ ~ ~-D, back=~ ~ ~+D, right=~+D ~ ~, left=~-D ~ ~";
        } else if (yaw < 247.5) { // NE
            return "dirs: front=~-D ~ ~+D, back=~+D ~ ~-D, right=~+D ~ ~+D, left=~-D ~ ~-D";
        } else if (yaw < 292.5) { // E
            return "dirs: front=~+D ~ ~, back=~-D ~ ~, right=~ ~ ~+D, left=~ ~ ~-D";
        } else { // SE
            return "dirs: front=~+D ~ ~+D, back=~-D ~ ~-D, right=~-D ~ ~+D, left=~+D ~ ~-D";
        }
    }
}
