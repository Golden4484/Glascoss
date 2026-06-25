package com.glascoss.ai;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.*;
import java.util.*;

public class RecipeManager {
    private static final Path RECIPES_DIR = FabricLoader.getInstance().getConfigDir().resolve("glascoss").resolve("recipes");
    private static final Map<String, NbtRecipe> recipes = new LinkedHashMap<>();
    private static boolean loaded = false;

    public static class NbtRecipe {
        public final String name;
        public final String item;
        public final int count;
        public final String nbt;

        public NbtRecipe(String name, String item, int count, String nbt) {
            this.name = name;
            this.item = item;
            this.count = count;
            this.nbt = nbt;
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", name);
            obj.addProperty("item", item);
            obj.addProperty("count", count);
            obj.addProperty("nbt", nbt);
            return obj;
        }

        public static NbtRecipe fromJson(JsonObject obj) {
            return new NbtRecipe(
                obj.get("name").getAsString(),
                obj.get("item").getAsString(),
                obj.get("count").getAsInt(),
                obj.has("nbt") ? obj.get("nbt").getAsString() : ""
            );
        }
    }

    public static void load() {
        if (loaded) return;
        try {
            Files.createDirectories(RECIPES_DIR);
            if (Files.exists(RECIPES_DIR)) {
                try (var stream = Files.list(RECIPES_DIR)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                        try {
                            String json = Files.readString(p);
                            NbtRecipe recipe = NbtRecipe.fromJson(JsonParser.parseString(json).getAsJsonObject());
                            recipes.put(recipe.name, recipe);
                        } catch (Exception ignored) {}
                    });
                }
            }
        } catch (Exception ignored) {}
        loaded = true;
    }

    public static void saveRecipe(NbtRecipe recipe) {
        recipes.put(recipe.name, recipe);
        try {
            Files.createDirectories(RECIPES_DIR);
            Files.writeString(RECIPES_DIR.resolve(recipe.name + ".json"),
                new GsonBuilder().setPrettyPrinting().create().toJson(recipe.toJson()));
        } catch (Exception ignored) {}
    }

    public static NbtRecipe getRecipe(String name) {
        return recipes.get(name);
    }

    public static List<NbtRecipe> getAllRecipes() {
        return new ArrayList<>(recipes.values());
    }

    public static boolean deleteRecipe(String name) {
        NbtRecipe removed = recipes.remove(name);
        if (removed != null) {
            try {
                Files.deleteIfExists(RECIPES_DIR.resolve(name + ".json"));
            } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    public static void reload() {
        recipes.clear();
        loaded = false;
        load();
    }
}
