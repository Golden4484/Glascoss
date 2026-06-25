package com.glascoss.memory;

import com.glascoss.ai.PersonalityEngine;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryManager {
    private static final Map<String, WorldMemory> worldMemories = new ConcurrentHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void register() {}

    public static WorldMemory getWorldMemory(ServerWorld world) {
        String worldId = world.getRegistryKey().getValue().toString();
        return worldMemories.computeIfAbsent(worldId, id -> loadWorldMemory(world, id));
    }

    public static String getWorldId(ServerWorld world) {
        return world.getRegistryKey().getValue().toString();
    }

    private static WorldMemory loadWorldMemory(ServerWorld world, String worldId) {
        Path memoryDir = getMemoryDir(world);
        Path worldMemoryFile = memoryDir.resolve("world_memory.json");

        UUID uuid = UUID.nameUUIDFromBytes(worldId.getBytes());
        WorldMemory wm;
        if (Files.exists(worldMemoryFile)) {
            try {
                String json = Files.readString(worldMemoryFile);
                wm = WorldMemory.fromJson(json, uuid);
            } catch (Exception e) {
                wm = new WorldMemory(uuid);
            }
        } else {
            wm = new WorldMemory(uuid);
        }

        Path conversationsDir = memoryDir.resolve("conversations");
        if (Files.isDirectory(conversationsDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(conversationsDir, "*.json")) {
                for (Path convFile : stream) {
                    String fileName = convFile.getFileName().toString().replace(".json", "");
                    try {
                        UUID playerUuid = UUID.fromString(fileName);
                        String json = Files.readString(convFile);
                        JsonObject obj = GSON.fromJson(json, JsonObject.class);
                        ConversationHistory history = new ConversationHistory(playerUuid);
                        if (obj.has("entries")) {
                            JsonArray entries = obj.getAsJsonArray("entries");
                            for (JsonElement element : entries) {
                                JsonObject entryObj = element.getAsJsonObject();
                                history.addEntry(new ConversationEntry(
                                        entryObj.get("speaker").getAsString(),
                                        entryObj.get("message").getAsString(),
                                        entryObj.has("emotion") ? entryObj.get("emotion").getAsString() : "neutral"
                                ));
                            }
                        }
                        wm.setConversation(playerUuid, history);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }

        PersonalityEngine.deserializeTrustScores(worldId, wm.getTrustScores());

        return wm;
    }

    public static void saveWorldMemory(ServerWorld world, WorldMemory memory) {
        Path memoryDir = getMemoryDir(world);
        String worldId = getWorldId(world);
        try {
            Files.createDirectories(memoryDir);

            memory.setTrustScores(PersonalityEngine.serializeTrustScores(worldId));

            Path worldMemoryFile = memoryDir.resolve("world_memory.json");
            Files.writeString(worldMemoryFile, GSON.toJson(memory.toJson()));

            Path conversationsDir = memoryDir.resolve("conversations");
            Files.createDirectories(conversationsDir);
            for (Map.Entry<UUID, ConversationHistory> entry : memory.getAllConversations().entrySet()) {
                Path convFile = conversationsDir.resolve(entry.getKey().toString() + ".json");
                Files.writeString(convFile, GSON.toJson(serializeConversation(entry.getValue())));
            }
        } catch (Exception e) {
            com.glascoss.GlascossMod.LOGGER.error("Failed to save Glascoss memory", e);
        }
    }

    public static void saveAll(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            String worldId = world.getRegistryKey().getValue().toString();
            WorldMemory memory = worldMemories.get(worldId);
            if (memory != null) {
                saveWorldMemory(world, memory);
            }
        }
    }

    public static Path getMemoryDir(ServerWorld world) {
        return world.getServer().getSavePath(WorldSavePath.ROOT).resolve("glascoss");
    }

    // --- Filesystem utilities (for screens WITHOUT an active server) ---

    public static Path getSavesDir() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("saves");
    }

    public static Path getWorldMemoryFile(Path worldFolder) {
        return worldFolder.resolve("glascoss").resolve("world_memory.json");
    }

    public static Path getConversationsDir(Path worldFolder) {
        return worldFolder.resolve("glascoss").resolve("conversations");
    }

    public static WorldMemory loadWorldMemoryFromFile(Path worldFolder) {
        Path memFile = getWorldMemoryFile(worldFolder);
        if (!Files.exists(memFile)) return null;
        try {
            String json = Files.readString(memFile);
            UUID uuid = UUID.nameUUIDFromBytes(worldFolder.getFileName().toString().getBytes());
            return WorldMemory.fromJson(json, uuid);
        } catch (Exception e) {
            return null;
        }
    }

    public static ConversationHistory loadConversationFromFile(Path worldFolder, UUID playerUuid) {
        Path convFile = getConversationsDir(worldFolder).resolve(playerUuid.toString() + ".json");
        if (!Files.exists(convFile)) return null;
        try {
            String json = Files.readString(convFile);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            ConversationHistory history = new ConversationHistory(playerUuid);
            if (obj.has("summary")) history = new ConversationHistory(playerUuid);
            if (obj.has("entries")) {
                JsonArray entries = obj.getAsJsonArray("entries");
                for (JsonElement element : entries) {
                    JsonObject entryObj = element.getAsJsonObject();
                    history.addEntry(new ConversationEntry(
                            entryObj.get("speaker").getAsString(),
                            entryObj.get("message").getAsString(),
                            entryObj.has("emotion") ? entryObj.get("emotion").getAsString() : "neutral"
                    ));
                }
            }
            return history;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getWorldDisplayName(Path worldFolder) {
        Path levelDat = worldFolder.resolve("level.dat");
        if (!Files.exists(levelDat)) return worldFolder.getFileName().toString();
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(levelDat.toFile()));
            dis.readInt();
            dis.readByte();
            dis.readByte();
            byte[] nameBytes = new byte[dis.readShort()];
            dis.readFully(nameBytes);
            dis.close();
            String nbtString = new String(nameBytes, "US-ASCII");
            int idx = nbtString.indexOf("LevelName");
            if (idx != -1) {
                int start = nbtString.indexOf('"', idx + 10) + 1;
                int end = nbtString.indexOf('"', start);
                if (start > 0 && end > start) return nbtString.substring(start, end);
            }
        } catch (Exception ignored) {}
        return worldFolder.getFileName().toString();
    }

    public static List<WorldSaveInfo> scanWorldSaves() {
        List<WorldSaveInfo> worlds = new ArrayList<>();
        Path savesDir = getSavesDir();
        if (!Files.isDirectory(savesDir)) return worlds;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(savesDir)) {
            for (Path folder : stream) {
                if (!Files.isDirectory(folder)) continue;
                Path glascossDir = folder.resolve("glascoss");
                Path memFile = glascossDir.resolve("world_memory.json");
                if (!Files.exists(memFile)) continue;

                WorldSaveInfo info = new WorldSaveInfo();
                info.folderName = folder.getFileName().toString();
                info.displayName = getWorldDisplayName(folder);
                info.path = folder;

                try {
                    String json = Files.readString(memFile);
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                    if (obj.has("knownPlayers")) info.conversationCount = obj.getAsJsonObject("knownPlayers").size();
                    if (obj.has("totalInteractionCount")) info.totalInteractions = obj.get("totalInteractionCount").getAsInt();

                    Path convDir = getConversationsDir(folder);
                    if (Files.isDirectory(convDir)) {
                        int convFiles = 0;
                        try (DirectoryStream<Path> convStream = Files.newDirectoryStream(convDir, "*.json")) {
                            for (Path ignored : convStream) convFiles++;
                        }
                        info.conversationCount = convFiles;
                    }
                } catch (Exception ignored) {}

                worlds.add(info);
            }
        } catch (Exception e) {
            com.glascoss.GlascossMod.LOGGER.error("Failed to scan world saves", e);
        }

        worlds.sort((a, b) -> b.folderName.compareTo(a.folderName));
        return worlds;
    }

    public static boolean deleteWorldMemory(Path worldFolder) {
        Path glascossDir = worldFolder.resolve("glascoss");
        if (!Files.exists(glascossDir)) return false;
        try {
            Files.walk(glascossDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // --- Serialization ---

    private static JsonObject serializeConversation(ConversationHistory history) {
        JsonObject json = new JsonObject();
        json.addProperty("playerUuid", history.getPlayerUuid().toString());
        json.addProperty("summary", history.getSummary());
        json.addProperty("totalTokens", history.getTotalTokens());

        JsonArray entriesArray = new JsonArray();
        for (ConversationEntry entry : history.getAllEntries()) {
            JsonObject entryObj = new JsonObject();
            entryObj.addProperty("speaker", entry.getSpeaker());
            entryObj.addProperty("message", entry.getMessage());
            entryObj.addProperty("timestamp", entry.getTimestamp());
            entryObj.addProperty("emotion", entry.getEmotion());
            entriesArray.add(entryObj);
        }
        json.add("entries", entriesArray);
        return json;
    }

    public static class WorldSaveInfo {
        public String folderName;
        public String displayName;
        public Path path;
        public int conversationCount;
        public int totalInteractions;
    }
}
