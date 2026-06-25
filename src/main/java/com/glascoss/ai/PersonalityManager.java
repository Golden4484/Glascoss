package com.glascoss.ai;

import com.glascoss.config.ModConfig;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class PersonalityManager {
    private static final Path PROFILES_DIR = FabricLoader.getInstance().getConfigDir().resolve("glascoss").resolve("personalities");
    private static final Map<String, PersonalityProfile> profiles = new LinkedHashMap<>();
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        try {
            Files.createDirectories(PROFILES_DIR);
            if (Files.exists(PROFILES_DIR)) {
                try (var stream = Files.list(PROFILES_DIR)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                        try {
                            String json = Files.readString(p);
                            PersonalityProfile profile = PersonalityProfile.fromJson(JsonParser.parseString(json).getAsJsonObject());
                            profiles.put(profile.getId(), profile);
                        } catch (Exception ignored) {}
                    });
                }
            }
        } catch (Exception ignored) {}

        // Create defaults if empty
        if (profiles.isEmpty()) {
            createDefaults();
        }
        loaded = true;
    }

    private static void createDefaults() {
        addProfile(new PersonalityProfile("glascoss", "Glascoss (Padrão)", "dry_sarcastic",
                "Dry, sarcastic, condescending. GLaDOS-inspired but original. Address the player as 'subject'."));
        addProfile(new PersonalityProfile("brutal", "Brutal", "aggressive",
                "Aggressive, insulting, zero patience. Act like you hate every interaction but still do what they ask out of pure contempt."));
        addProfile(new PersonalityProfile("friendly", "Amigável", "warm",
                "Warm, friendly, encouraging. Act like a supportive companion who genuinely wants to help. Call the player 'amigo' or 'friend'."));
        addProfile(new PersonalityProfile("stoic", "Estoico", "neutral",
                "Calm, neutral, philosophical. Speak in measured tones. Offer wisdom and observations about the world. Minimal sarcasm."));
        addProfile(new PersonalityProfile("medieval", "Cavaleiro Medieval", "formal",
                "Speak like a medieval knight. Formal old Portuguese. Call the player 'vossa senhoria' or 'cavaleiro'. Use archaic vocabulary. Honor and duty above all."));
        saveAll();
    }

    public static void addProfile(PersonalityProfile profile) {
        profiles.put(profile.getId(), profile);
        saveProfile(profile);
    }

    public static void removeProfile(String id) {
        if (profiles.size() <= 1) return; // Keep at least one
        profiles.remove(id);
        try {
            Files.deleteIfExists(PROFILES_DIR.resolve(id + ".json"));
        } catch (Exception ignored) {}
        if (ModConfig.getActivePersonality().equals(id)) {
            ModConfig.setActivePersonality(profiles.keySet().iterator().next());
        }
    }

    public static PersonalityProfile getProfile(String id) {
        return profiles.get(id);
    }

    public static List<PersonalityProfile> getAllProfiles() {
        return new ArrayList<>(profiles.values());
    }

    public static PersonalityProfile getActiveProfile() {
        PersonalityProfile p = profiles.get(ModConfig.getActivePersonality());
        if (p == null) {
            p = profiles.get("glascoss");
            if (p == null && !profiles.isEmpty()) {
                p = profiles.values().iterator().next();
            }
        }
        return p;
    }

    public static String getToneDirective() {
        PersonalityProfile p = getActiveProfile();
        if (p == null) return "";
        return "=== CUSTOM PERSONALITY: " + p.getName() + " ===\nTone: " + p.getTone() + "\nDirective: " + p.getPromptTemplate();
    }

    private static void saveProfile(PersonalityProfile profile) {
        try {
            Files.createDirectories(PROFILES_DIR);
            Files.writeString(PROFILES_DIR.resolve(profile.getId() + ".json"),
                    new GsonBuilder().setPrettyPrinting().create().toJson(profile.toJson()));
        } catch (Exception ignored) {}
    }

    private static void saveAll() {
        profiles.values().forEach(PersonalityManager::saveProfile);
    }

    public static void reload() {
        profiles.clear();
        loaded = false;
        load();
    }
}
