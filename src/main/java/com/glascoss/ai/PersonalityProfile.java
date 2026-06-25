package com.glascoss.ai;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.*;
import java.util.*;

public class PersonalityProfile {
    private String id;
    private String name;
    private String tone;
    private String promptTemplate;

    public PersonalityProfile(String id, String name, String tone, String promptTemplate) {
        this.id = id;
        this.name = name;
        this.tone = tone;
        this.promptTemplate = promptTemplate;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTone() { return tone; }
    public String getPromptTemplate() { return promptTemplate; }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name);
        obj.addProperty("tone", tone);
        obj.addProperty("promptTemplate", promptTemplate);
        return obj;
    }

    public static PersonalityProfile fromJson(JsonObject obj) {
        return new PersonalityProfile(
                obj.get("id").getAsString(),
                obj.get("name").getAsString(),
                obj.get("tone").getAsString(),
                obj.get("promptTemplate").getAsString()
        );
    }
}
