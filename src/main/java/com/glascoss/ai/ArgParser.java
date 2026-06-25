package com.glascoss.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class ArgParser {

    public static Map<String, String> parse(String argsStr) {
        Map<String, String> result = new HashMap<>();
        if (argsStr == null || argsStr.isBlank()) return result;

        String trimmed = argsStr.trim();

        if (trimmed.startsWith("{")) {
            return parseJson(trimmed);
        }

        return parseKeyValue(trimmed);
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> result = new HashMap<>();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            for (String key : obj.keySet()) {
                result.put(key, obj.get(key).getAsString());
            }
        } catch (Exception ignored) {}
        return result;
    }

    private static Map<String, String> parseKeyValue(String args) {
        Map<String, String> result = new HashMap<>();
        String[] parts = splitArgs(args);
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            int eqIdx = part.indexOf('=');
            if (eqIdx > 0) {
                String key = part.substring(0, eqIdx).trim();
                String val = part.substring(eqIdx + 1).trim();
                result.put(key, val);
            }
        }
        return result;
    }

    private static String[] splitArgs(String args) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (char c : args.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') depth++;
            else if (c == ')' || c == '}' || c == ']') depth--;
            else if (c == ',' && depth == 0) {
                parts.add(current.toString());
                current = new StringBuilder();
                continue;
            }
            current.append(c);
        }
        if (current.length() > 0) parts.add(current.toString());
        return parts.toArray(new String[0]);
    }

    public static int getInt(Map<String, String> args, String key, int defaultValue) {
        if (args.containsKey(key)) {
            try { return Integer.parseInt(args.get(key).trim()); } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static String getString(Map<String, String> args, String key, String defaultValue) {
        return args.getOrDefault(key, defaultValue);
    }

    public static int resolveCoord(String raw, int playerPos) {
        if (raw == null || raw.isBlank()) return playerPos;
        raw = raw.trim();
        if (raw.equals("~")) return playerPos;
        if (raw.startsWith("~")) {
            String offset = raw.substring(1);
            if (offset.isEmpty()) return playerPos;
            try { return playerPos + Integer.parseInt(offset); } catch (NumberFormatException e) { return playerPos; }
        }
        try { return Integer.parseInt(raw); } catch (NumberFormatException e) { return playerPos; }
    }
}
