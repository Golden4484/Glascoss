package com.glascoss.memory;

import com.google.gson.*;
import java.time.Instant;
import java.util.*;

public class WorldMemory {
    public static final int VERSION = 2;

    private final UUID worldId;
    private int version;
    private final Map<UUID, KnownPlayer> knownPlayers;
    private final Map<UUID, ConversationHistory> conversations;
    private final List<BuildLog> buildLog;
    private final Map<String, Boolean> flags;
    private final SemanticSummary semanticSummary;
    private Map<String, Object> trustScores;
    private int totalInteractionCount;
    private String activePersonality;

    public WorldMemory(UUID worldId) {
        this.worldId = worldId;
        this.version = VERSION;
        this.knownPlayers = new HashMap<>();
        this.conversations = new HashMap<>();
        this.buildLog = new ArrayList<>();
        this.flags = new HashMap<>();
        this.semanticSummary = new SemanticSummary();
        this.trustScores = new HashMap<>();
        this.totalInteractionCount = 0;
        this.activePersonality = "glascoss";
    }

    // --- Version ---
    public int getVersion() { return version; }
    public void setVersion(int v) { this.version = v; }

    // --- Known Players ---
    public void addKnownPlayer(UUID uuid, String name) {
        knownPlayers.computeIfAbsent(uuid, id -> new KnownPlayer(name));
    }

    public KnownPlayer getKnownPlayer(UUID uuid) { return knownPlayers.get(uuid); }
    public int getKnownPlayerCount() { return knownPlayers.size(); }
    public Map<UUID, KnownPlayer> getKnownPlayers() { return knownPlayers; }

    public int getInteractionCount(UUID playerUuid) {
        KnownPlayer p = knownPlayers.get(playerUuid);
        return p != null ? p.interactionCount : 0;
    }

    public void incrementInteractionCount(UUID playerUuid) {
        KnownPlayer p = knownPlayers.computeIfAbsent(playerUuid, id -> new KnownPlayer("unknown"));
        p.interactionCount++;
        totalInteractionCount++;
    }

    // --- Conversations ---
    public ConversationHistory getConversationHistory(UUID playerUuid) {
        return conversations.computeIfAbsent(playerUuid, id -> new ConversationHistory(id));
    }

    public void setConversation(UUID playerUuid, ConversationHistory history) {
        conversations.put(playerUuid, history);
    }

    public Map<UUID, ConversationHistory> getAllConversations() { return conversations; }

    // --- Build Log ---
    public void addBuildLog(BuildLog log) { buildLog.add(log); }
    public List<BuildLog> getBuildLog() { return buildLog; }

    // --- Flags ---
    public void setFlag(String key, boolean value) { flags.put(key, value); }
    public boolean getFlag(String key) { return flags.getOrDefault(key, false); }

    // --- Semantic Summary ---
    public SemanticSummary getSemanticSummary() { return semanticSummary; }

    public void addSemanticFact(String fact) {
        if (!semanticSummary.facts.contains(fact)) {
            semanticSummary.facts.add(fact);
            if (semanticSummary.facts.size() > 30) semanticSummary.facts.remove(0);
        }
    }

    public List<String> getRelevantFacts(String keyword) {
        if (keyword == null || keyword.isBlank()) return new ArrayList<>(semanticSummary.facts);
        String lower = keyword.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String fact : semanticSummary.facts) {
            if (fact.toLowerCase().contains(lower)) result.add(fact);
        }
        return result;
    }

    // --- Trust Scores ---
    @SuppressWarnings("unchecked")
    public Map<String, Object> getTrustScores() {
        if (trustScores == null) trustScores = new HashMap<>();
        return trustScores;
    }

    public void setTrustScores(Map<String, Object> scores) {
        this.trustScores = scores != null ? scores : new HashMap<>();
    }

    // --- Active Personality ---
    public String getActivePersonality() { return activePersonality; }

    public void setActivePersonality(String id) {
        this.activePersonality = id != null ? id : "glascoss";
    }

    public void resetConversations() {
        conversations.clear();
        semanticSummary.facts.clear();
        totalInteractionCount = 0;
    }

    // --- Died flag ---
    public boolean hasDied(UUID playerUuid) {
        return getFlag("died_" + playerUuid);
    }

    public void setDied(UUID playerUuid) {
        setFlag("died_" + playerUuid, true);
    }

    public boolean consumeDied(UUID playerUuid) {
        boolean died = hasDied(playerUuid);
        if (died) setFlag("died_" + playerUuid, false);
        return died;
    }

    // --- Serialization ---
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("version", VERSION);
        json.addProperty("worldId", worldId.toString());
        json.addProperty("totalInteractionCount", totalInteractionCount);
        json.addProperty("activePersonality", activePersonality);

        JsonObject playersJson = new JsonObject();
        for (Map.Entry<UUID, KnownPlayer> entry : knownPlayers.entrySet()) {
            JsonObject pj = new JsonObject();
            pj.addProperty("name", entry.getValue().name);
            pj.addProperty("interactionCount", entry.getValue().interactionCount);
            pj.addProperty("firstEncounter", entry.getValue().firstEncounter);
            playersJson.add(entry.getKey().toString(), pj);
        }
        json.add("knownPlayers", playersJson);

        JsonArray buildsArray = new JsonArray();
        for (BuildLog log : buildLog) {
            JsonObject bj = new JsonObject();
            bj.addProperty("buildId", log.buildId);
            bj.addProperty("type", log.type);
            bj.addProperty("x", log.x);
            bj.addProperty("y", log.y);
            bj.addProperty("z", log.z);
            bj.addProperty("completed", log.completed);
            bj.addProperty("timestamp", log.timestamp);
            buildsArray.add(bj);
        }
        json.add("buildLog", buildsArray);

        JsonObject flagsJson = new JsonObject();
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            flagsJson.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("flags", flagsJson);

        JsonObject semanticJson = new JsonObject();
        JsonArray factsArray = new JsonArray();
        for (String f : semanticSummary.facts) factsArray.add(f);
        semanticJson.add("facts", factsArray);
        json.add("semantic", semanticJson);

        if (trustScores != null && !trustScores.isEmpty()) {
            JsonObject tsJson = new JsonObject();
            for (Map.Entry<String, Object> e : trustScores.entrySet()) {
                if (e.getValue() instanceof Number) tsJson.addProperty(e.getKey(), (Number) e.getValue());
                else if (e.getValue() instanceof String) tsJson.addProperty(e.getKey(), (String) e.getValue());
                else if (e.getValue() instanceof Boolean) tsJson.addProperty(e.getKey(), (Boolean) e.getValue());
            }
            json.add("trustScores", tsJson);
        }

        return json;
    }

    public static WorldMemory fromJson(String jsonStr, UUID worldId) {
        try {
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            WorldMemory memory = new WorldMemory(worldId);

            if (json.has("version")) {
                memory.version = json.get("version").getAsInt();
            }

            if (memory.version < VERSION) {
                memory.trustScores.clear();
                memory.conversations.clear();
                memory.semanticSummary.facts.clear();
                memory.version = VERSION;
            }

            if (json.has("totalInteractionCount"))
                memory.totalInteractionCount = json.get("totalInteractionCount").getAsInt();

            if (json.has("activePersonality"))
                memory.activePersonality = json.get("activePersonality").getAsString();

            if (json.has("knownPlayers")) {
                JsonObject playersJson = json.getAsJsonObject("knownPlayers");
                for (Map.Entry<String, JsonElement> entry : playersJson.entrySet()) {
                    UUID uuid = UUID.fromString(entry.getKey());
                    JsonObject pj = entry.getValue().getAsJsonObject();
                    KnownPlayer p = new KnownPlayer(pj.get("name").getAsString());
                    p.interactionCount = pj.get("interactionCount").getAsInt();
                    if (pj.has("firstEncounter")) p.firstEncounter = pj.get("firstEncounter").getAsString();
                    memory.knownPlayers.put(uuid, p);
                }
            }

            if (json.has("flags")) {
                JsonObject flagsJson = json.getAsJsonObject("flags");
                for (Map.Entry<String, JsonElement> entry : flagsJson.entrySet())
                    memory.flags.put(entry.getKey(), entry.getValue().getAsBoolean());
            }

            if (json.has("semantic")) {
                JsonObject semanticJson = json.getAsJsonObject("semantic");
                if (semanticJson.has("facts")) {
                    JsonArray factsArray = semanticJson.getAsJsonArray("facts");
                    for (JsonElement el : factsArray)
                        memory.semanticSummary.facts.add(el.getAsString());
                }
            }

            if (json.has("trustScores")) {
                JsonObject tsJson = json.getAsJsonObject("trustScores");
                Map<String, Object> ts = new HashMap<>();
                for (Map.Entry<String, JsonElement> e : tsJson.entrySet()) {
                    JsonElement val = e.getValue();
                    if (val.isJsonPrimitive()) {
                        JsonPrimitive prim = val.getAsJsonPrimitive();
                        if (prim.isNumber()) ts.put(e.getKey(), prim.getAsInt());
                        else if (prim.isBoolean()) ts.put(e.getKey(), prim.getAsBoolean());
                        else ts.put(e.getKey(), prim.getAsString());
                    }
                }
                memory.trustScores = ts;
            }

            return memory;
        } catch (Exception e) {
            return new WorldMemory(worldId);
        }
    }

    // --- Inner classes ---
    public static class KnownPlayer {
        public String name;
        public int interactionCount;
        public String firstEncounter;

        public KnownPlayer(String name) {
            this.name = name;
            this.interactionCount = 0;
            this.firstEncounter = java.time.Instant.now().toString();
        }
    }

    public static class BuildLog {
        public String buildId, type;
        public int x, y, z;
        public boolean completed;
        public String timestamp;

        public BuildLog(String buildId, String type, int x, int y, int z) {
            this.buildId = buildId;
            this.type = type;
            this.x = x; this.y = y; this.z = z;
            this.completed = false;
            this.timestamp = java.time.Instant.now().toString();
        }
    }

    public static class SemanticSummary {
        public final List<String> facts = new ArrayList<>();
    }
}
