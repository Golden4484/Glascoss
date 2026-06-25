package com.glascoss.memory;

import java.util.UUID;

public class ConversationEntry {
    private final String speaker;
    private final String message;
    private final long timestamp;
    private final String emotion;

    public ConversationEntry(String speaker, String message) {
        this(speaker, message, "neutral");
    }

    public ConversationEntry(String speaker, String message, String emotion) {
        this.speaker = speaker;
        this.message = message;
        this.emotion = emotion;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSpeaker() { return speaker; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getEmotion() { return emotion; }
}
