package com.glascoss.memory;

import java.util.ArrayList;
import java.util.List;

public class MemoryView {
    private static final List<ChatEntry> chatLog = new ArrayList<>();
    private static final List<ActionEntry> actionLog = new ArrayList<>();

    public static synchronized void addChat(String playerName, String message, String speaker) {
        chatLog.add(new ChatEntry(playerName, message, speaker));
        if (chatLog.size() > 1000) chatLog.remove(0);
    }

    public static synchronized void addAction(ActionEntry entry) {
        actionLog.add(entry);
        if (actionLog.size() > 500) actionLog.remove(0);
    }

    public static synchronized List<ChatEntry> getChatLog() {
        return new ArrayList<>(chatLog);
    }

    public static synchronized List<ActionEntry> getActionLog() {
        return new ArrayList<>(actionLog);
    }

    public static synchronized void addChatEntry(String message) {
        chatLog.add(new ChatEntry("", message, ""));
        if (chatLog.size() > 1000) chatLog.remove(0);
    }

    public static synchronized void addActionEntry(String description) {
        actionLog.add(new ActionEntry("system", description, "action"));
        if (actionLog.size() > 500) actionLog.remove(0);
    }

    public static synchronized void clearChat() { chatLog.clear(); }
    public static synchronized void clearActions() { actionLog.clear(); }

    public static class ChatEntry {
        private final String playerName;
        private final String message;
        private final String speaker;
        private final long timestamp;

        public ChatEntry(String playerName, String message, String speaker) {
            this.playerName = playerName;
            this.message = message;
            this.speaker = speaker;
            this.timestamp = System.currentTimeMillis();
        }

        public String getPlayerName() { return playerName; }
        public String getMessage() { return message; }
        public String getSpeaker() { return speaker; }
        public long getTimestamp() { return timestamp; }
    }
}
