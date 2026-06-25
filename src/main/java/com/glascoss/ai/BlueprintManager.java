package com.glascoss.ai;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.*;
import java.util.*;

public class BlueprintManager {
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("glascoss").resolve("blueprints");
    private static final Map<String, Blueprint> blueprints = new LinkedHashMap<>();
    private static boolean loaded = false;

    public static class Step {
        public String type; // "fill" or "place"
        public int x1, y1, z1, x2, y2, z2; // for fill
        public int x, y, z; // for place
        public String block; // e.g. "minecraft:stone" or "oak_door[facing=north,half=lower]"

        public static Step fromJson(JsonObject obj) {
            Step s = new Step();
            s.type = obj.get("type").getAsString();
            if (s.type.equals("fill")) {
                s.x1 = obj.get("x1").getAsInt();
                s.y1 = obj.get("y1").getAsInt();
                s.z1 = obj.get("z1").getAsInt();
                s.x2 = obj.get("x2").getAsInt();
                s.y2 = obj.get("y2").getAsInt();
                s.z2 = obj.get("z2").getAsInt();
            } else {
                s.x = obj.get("x").getAsInt();
                s.y = obj.get("y").getAsInt();
                s.z = obj.get("z").getAsInt();
            }
            s.block = obj.get("block").getAsString();
            return s;
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", type);
            if (type.equals("fill")) {
                obj.addProperty("x1", x1);
                obj.addProperty("y1", y1);
                obj.addProperty("z1", z1);
                obj.addProperty("x2", x2);
                obj.addProperty("y2", y2);
                obj.addProperty("z2", z2);
            } else {
                obj.addProperty("x", x);
                obj.addProperty("y", y);
                obj.addProperty("z", z);
            }
            obj.addProperty("block", block);
            return obj;
        }
    }

    public static class Blueprint {
        public final String name;
        public final List<Step> steps;

        public Blueprint(String name, List<Step> steps) {
            this.name = name;
            this.steps = steps;
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", name);
            JsonArray arr = new JsonArray();
            for (Step s : steps) arr.add(s.toJson());
            obj.add("steps", arr);
            return obj;
        }

        public static Blueprint fromJson(JsonObject obj) {
            String name = obj.get("name").getAsString();
            List<Step> steps = new ArrayList<>();
            JsonArray arr = obj.getAsJsonArray("steps");
            for (var e : arr) steps.add(Step.fromJson(e.getAsJsonObject()));
            return new Blueprint(name, steps);
        }
    }

    public static void load() {
        if (loaded) return;
        try {
            Files.createDirectories(DIR);
            if (Files.exists(DIR)) {
                try (var stream = Files.list(DIR)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                        try {
                            String json = Files.readString(p);
                            Blueprint bp = Blueprint.fromJson(JsonParser.parseString(json).getAsJsonObject());
                            blueprints.put(bp.name, bp);
                        } catch (Exception ignored) {}
                    });
                }
            }
        } catch (Exception ignored) {}
        loaded = true;
    }

    public static void saveBlueprint(Blueprint bp) {
        blueprints.put(bp.name, bp);
        try {
            Files.createDirectories(DIR);
            Files.writeString(DIR.resolve(bp.name + ".json"),
                new GsonBuilder().setPrettyPrinting().create().toJson(bp.toJson()));
        } catch (Exception ignored) {}
    }

    public static Blueprint getBlueprint(String name) {
        return blueprints.get(name);
    }

    public static List<Blueprint> getAllBlueprints() {
        return new ArrayList<>(blueprints.values());
    }

    public static boolean deleteBlueprint(String name) {
        Blueprint removed = blueprints.remove(name);
        if (removed != null) {
            try {
                Files.deleteIfExists(DIR.resolve(name + ".json"));
            } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    public static void reload() {
        blueprints.clear();
        loaded = false;
        load();
    }
}
