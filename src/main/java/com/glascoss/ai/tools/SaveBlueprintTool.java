package com.glascoss.ai.tools;

import com.glascoss.ai.ArgParser;
import com.glascoss.ai.BlueprintManager;
import com.glascoss.ai.BlueprintManager.Blueprint;
import com.glascoss.ai.BlueprintManager.Step;
import com.glascoss.ai.CommandFilter;
import com.google.gson.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;

public class SaveBlueprintTool implements Tool {
    @Override
    public String getName() { return "save_blueprint"; }

    @Override
    public String getDescription() { return "Save a construction blueprint. Parameters: name (string), steps (JSON array of step objects). Each step: {type:fill/place, x1,y1,z1,x2,y2,z2, x,y,z, block:string}"; }

    @Override
    public String execute(String argsStr, ServerWorld world, PlayerEntity player) {
        try {
            Map<String, String> args = ArgParser.parse(argsStr);
            String name = ArgParser.getString(args, "name", "").trim().replaceAll("[^a-zA-Z0-9_-]", "");
            String stepsStr = ArgParser.getString(args, "steps", "");

            if (name.isBlank()) return "Blueprint name is required.";
            if (stepsStr.isBlank()) return "Steps are required.";

            JsonArray arr;
            try {
                arr = JsonParser.parseString(stepsStr).getAsJsonArray();
            } catch (Exception e) {
                return "Invalid steps JSON: " + e.getMessage();
            }

            List<Step> steps = new ArrayList<>();
            for (var elem : arr) {
                steps.add(Step.fromJson(elem.getAsJsonObject()));
            }

            BlueprintManager.saveBlueprint(new Blueprint(name, steps));
            return "Saved blueprint '" + name + "' with " + steps.size() + " steps. Use build_blueprint(name=" + name + ", ...) to build it.";
        } catch (Exception e) {
            return "save_blueprint error: " + e.getMessage();
        }
    }
}
