package com.glascoss.memory;

import java.util.ArrayList;
import java.util.List;

public class ConversationHistory {
    private static final int MAX_TOKENS = 1500;
    private static final int MAX_RECENT_ENTRIES = 15;

    private final java.util.UUID playerUuid;
    private final List<ConversationEntry> entries;
    private String summary;
    private int totalTokens;

    public ConversationHistory(java.util.UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.entries = new ArrayList<>();
        this.summary = "";
        this.totalTokens = 0;
    }

    public void addEntry(ConversationEntry entry) {
        entries.add(entry);
        totalTokens += estimateTokens(entry.getMessage());

        if (totalTokens > MAX_TOKENS && entries.size() > MAX_RECENT_ENTRIES) {
            summarize();
        }
    }

    public List<ConversationEntry> getRecentEntries(int count) {
        int take = Math.min(count, MAX_RECENT_ENTRIES);
        if (entries.size() <= take) {
            return new ArrayList<>(entries);
        }
        return new ArrayList<>(entries.subList(entries.size() - take, entries.size()));
    }

    public List<ConversationEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }

    public String getSummary() {
        return summary;
    }

    public boolean hasSummary() {
        return !summary.isBlank();
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public java.util.UUID getPlayerUuid() {
        return playerUuid;
    }

    private void summarize() {
        int keepCount = Math.min(MAX_RECENT_ENTRIES, entries.size());
        int summarizeCount = entries.size() - keepCount;

        if (summarizeCount <= 0) return;

        List<ConversationEntry> oldEntries = new ArrayList<>(entries.subList(0, summarizeCount));

        StringBuilder sb = new StringBuilder();
        if (!summary.isBlank()) {
            sb.append(summary).append(" ");
        }

        int summarizedTokens = 0;
        for (ConversationEntry entry : oldEntries) {
            String line = entry.getSpeaker() + ": " + truncate(entry.getMessage(), 60) + "; ";
            int lineTokens = estimateTokens(line);
            if (summarizedTokens + lineTokens > 800) {
                sb.append("...");
                break;
            }
            sb.append(line);
            summarizedTokens += lineTokens;
        }

        summary = sb.toString();

        entries.subList(0, summarizeCount).clear();
        totalTokens = 0;
        for (ConversationEntry e : entries) {
            totalTokens += estimateTokens(e.getMessage());
        }
    }

    private int estimateTokens(String text) {
        if (text == null) return 0;
        int tokens = text.length() / 4;
        int toolMarkers = countOccurrences(text, "[TOOL:") * 8;
        return tokens + toolMarkers;
    }

    private int countOccurrences(String text, String sub) {
        int count = 0, idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    private String truncate(String text, int maxLen) {
        return text != null && text.length() > maxLen
                ? text.substring(0, maxLen) + "..."
                : (text == null ? "" : text);
    }
}
