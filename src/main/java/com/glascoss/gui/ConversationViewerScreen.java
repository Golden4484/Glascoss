package com.glascoss.gui;

import com.glascoss.memory.ConversationEntry;
import com.glascoss.memory.ConversationHistory;
import com.glascoss.memory.MemoryManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConversationViewerScreen extends Screen {
    private final Screen parent;
    private final Path worldPath;
    private List<MessageEntry> messages;
    private int scrollOffset;
    private static final int PER_PAGE = 15;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm")
            .withZone(ZoneId.systemDefault());

    private static class MessageEntry {
        final ConversationEntry entry;
        final int index;
        final UUID playerUuid;

        MessageEntry(ConversationEntry entry, int index, UUID playerUuid) {
            this.entry = entry;
            this.index = index;
            this.playerUuid = playerUuid;
        }
    }

    public ConversationViewerScreen(Screen parent, Path worldPath, UUID playerUuid) {
        super(Text.translatable("glascoss.gui.conversations_title"));
        this.parent = parent;
        this.worldPath = worldPath;
        this.messages = new ArrayList<>();
        this.scrollOffset = 0;

        if (worldPath != null) {
            loadConversations();
        }
    }

    private void loadConversations() {
        messages.clear();
        Path convDir = MemoryManager.getConversationsDir(worldPath);
        if (convDir == null || !convDir.toFile().exists()) return;

        java.io.File[] files = convDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (java.io.File f : files) {
            String name = f.getName().replace(".json", "");
            try {
                UUID uuid = UUID.fromString(name);
                ConversationHistory history = MemoryManager.loadConversationFromFile(worldPath, uuid);
                if (history == null) continue;
                List<ConversationEntry> entries = history.getAllEntries();
                for (int i = 0; i < entries.size(); i++) {
                    messages.add(new MessageEntry(entries.get(i), i, uuid));
                }
            } catch (Exception ignored) {}
        }

        messages.sort((a, b) -> Long.compare(a.entry.getTimestamp(), b.entry.getTimestamp()));
    }

    @Override
    protected void init() {
        int cx = width / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.delete_all"),
                button -> {
                    if (worldPath != null) deleteAll();
                }
        ).dimensions(cx - 150, 5, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.delete_ai"),
                button -> deleteBySpeaker("glascoss")
        ).dimensions(cx - 50, 5, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.delete_player"),
                button -> deleteBySpeaker("player")
        ).dimensions(cx + 50, 5, 95, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.back"),
                button -> client.setScreen(parent)
        ).dimensions(cx + 150, 5, 60, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);

        int y = 35;
        int start = scrollOffset * PER_PAGE;
        int count = 0;

        for (int i = start; i < messages.size() && count < PER_PAGE; i++) {
            MessageEntry me = messages.get(i);
            String speakerKey = me.entry.getSpeaker().equals("player")
                    ? "glascoss.gui.speaker_player" : "glascoss.gui.speaker_glascoss";
            int color = me.entry.getSpeaker().equals("player") ? 0xAAAAAA : 0x55FFFF;
            String time = FMT.format(Instant.ofEpochMilli(me.entry.getTimestamp()));
            String text = "§7[" + time + "] §" + (me.entry.getSpeaker().equals("player") ? "7" : "b")
                    + Text.translatable(speakerKey).getString() + ": §f" + truncate(me.entry.getMessage(), 70);
            ctx.drawTextWithShadow(textRenderer, Text.literal(text), 10, y, color);

            int fi = i;
            drawDeleteButton(ctx, width - 20, y, () -> deleteMessage(fi));
            y += 11;
            count++;
        }

        if (messages.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("glascoss.gui.no_conversations"), width / 2, height / 2, 0x777777);
        }

        int totalPages = Math.max(1, (messages.size() + PER_PAGE - 1) / PER_PAGE);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.page", scrollOffset + 1, totalPages), width / 2, height - 15, 0x777777);
    }

    private void drawDeleteButton(DrawContext ctx, int x, int y, Runnable action) {
        String text = "§8[X]";
        int w = textRenderer.getWidth(text);
        ctx.drawTextWithShadow(textRenderer, Text.literal(text), x - w, y, 0xFF5555);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int y = 35;
        int start = scrollOffset * PER_PAGE;
        int count = 0;
        for (int i = start; i < messages.size() && count < PER_PAGE; i++) {
            String text = "§8[X]";
            int w = textRenderer.getWidth(text);
            int bx = width - 20 - w;
            int by = y;
            if (mouseX >= bx && mouseX <= bx + w && mouseY >= by && mouseY <= by + 11) {
                deleteMessage(i);
                return true;
            }
            y += 11;
            count++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int totalPages = Math.max(1, (messages.size() + PER_PAGE - 1) / PER_PAGE);
        if (amount < 0) scrollOffset = Math.min(totalPages - 1, scrollOffset + 1);
        else if (amount > 0) scrollOffset = Math.max(0, scrollOffset - 1);
        return true;
    }

    private void deleteMessage(int index) {
        if (index < 0 || index >= messages.size()) return;
        MessageEntry me = messages.remove(index);
        saveConversations();
    }

    private void deleteBySpeaker(String speaker) {
        messages.removeIf(me -> me.entry.getSpeaker().equals(speaker));
        saveConversations();
    }

    private void deleteAll() {
        messages.clear();
        saveConversations();
        MemoryManager.deleteWorldMemory(worldPath);
    }

    private void saveConversations() {
        if (worldPath == null) return;
        Path convDir = MemoryManager.getConversationsDir(worldPath);
        if (convDir == null) return;

        try {
            java.nio.file.Files.createDirectories(convDir);
            java.io.File[] files = convDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (java.io.File f : files) f.delete();
            }

            java.util.Map<UUID, List<ConversationEntry>> grouped = new java.util.HashMap<>();
            for (MessageEntry me : messages) {
                grouped.computeIfAbsent(me.playerUuid, k -> new ArrayList<>()).add(me.entry);
            }

            for (java.util.Map.Entry<UUID, List<ConversationEntry>> e : grouped.entrySet()) {
                ConversationHistory history = new ConversationHistory(e.getKey());
                for (ConversationEntry ce : e.getValue()) {
                    history.addEntry(ce);
                }
                java.nio.file.Path convFile = convDir.resolve(e.getKey().toString() + ".json");
                String json = com.google.gson.JsonParser.parseString(
                        new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(
                                MemoryManager.loadConversationFromFile(worldPath, e.getKey()) != null
                                        ? serializeHistory(history) : serializeHistory(history)
                        )).toString();
                java.nio.file.Files.writeString(convFile, json);
            }
        } catch (Exception ignored) {}

        loadConversations();
    }

    private com.google.gson.JsonObject serializeHistory(ConversationHistory history) {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        json.addProperty("playerUuid", history.getPlayerUuid().toString());
        json.addProperty("summary", history.getSummary());
        json.addProperty("totalTokens", history.getTotalTokens());

        com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
        for (ConversationEntry e : history.getAllEntries()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("speaker", e.getSpeaker());
            obj.addProperty("message", e.getMessage());
            obj.addProperty("timestamp", e.getTimestamp());
            obj.addProperty("emotion", e.getEmotion());
            arr.add(obj);
        }
        json.add("entries", arr);
        return json;
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "..." : (s == null ? "" : s);
    }

    @Override
    public void close() { client.setScreen(parent); }
    @Override
    public boolean shouldPause() { return true; }
}
