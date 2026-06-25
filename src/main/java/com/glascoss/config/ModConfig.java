package com.glascoss.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.*;

public class ModConfig {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("glascoss.json");

    private static String apiKey = "";
    private static String model = "gemini-flash-lite-latest";
    private static boolean showCommands = false;
    private static boolean disableOnOnline = false;
    private static String activePersonality = "default";

    public static void load() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (Files.exists(CONFIG_FILE)) {
                JsonObject obj = JsonParser.parseString(Files.readString(CONFIG_FILE)).getAsJsonObject();
                if (obj.has("apiKey")) apiKey = obj.get("apiKey").getAsString();
                if (obj.has("model")) model = obj.get("model").getAsString();
                if (obj.has("showCommands")) showCommands = obj.get("showCommands").getAsBoolean();
                if (obj.has("disableOnOnline")) disableOnOnline = obj.get("disableOnOnline").getAsBoolean();
                if (obj.has("activePersonality")) activePersonality = obj.get("activePersonality").getAsString();
            }
        } catch (Exception e) {
        }
        save();
    }

    public static void save() {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("apiKey", apiKey);
            obj.addProperty("model", model);
            obj.addProperty("showCommands", showCommands);
            obj.addProperty("disableOnOnline", disableOnOnline);
            obj.addProperty("activePersonality", activePersonality);
            Files.createDirectories(CONFIG_DIR);
            Files.writeString(CONFIG_FILE, new GsonBuilder().setPrettyPrinting().create().toJson(obj));
        } catch (Exception e) {
        }
    }

    public static String getApiKey() { return apiKey; }
    public static void setApiKey(String key) { apiKey = key; save(); }

    public static String getModel() { return model; }
    public static void setModel(String m) { model = m; save(); }

    public static boolean isShowCommands() { return showCommands; }
    public static void setShowCommands(boolean v) { showCommands = v; save(); }

    public static boolean isDisableOnOnline() { return disableOnOnline; }
    public static void setDisableOnOnline(boolean v) { disableOnOnline = v; save(); }

    public static String getActivePersonality() { return activePersonality; }
    public static void setActivePersonality(String v) { activePersonality = v; save(); }
}
