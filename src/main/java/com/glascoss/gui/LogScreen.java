package com.glascoss.gui;

import com.glascoss.config.ConfigScreen;
import com.glascoss.memory.ActionEntry;
import com.glascoss.memory.MemoryView;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LogScreen extends Screen {
    private final Screen parent;
    private int currentTab = 0;
    private int scrollOffset = 0;
    private static final int ENTRIES_PER_PAGE = 20;
    private static final String[] TAB_KEYS = {"glascoss.gui.chat_tab", "glascoss.gui.actions_tab"};
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm")
            .withZone(ZoneId.systemDefault());

    public LogScreen(Screen parent) {
        super(Text.translatable("glascoss.gui.glascoss"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;

        for (int i = 0; i < TAB_KEYS.length; i++) {
            int tabIdx = i;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(currentTab == i ? "§l" : "").append(Text.translatable(TAB_KEYS[i])),
                    btn -> { currentTab = tabIdx; scrollOffset = 0; init(); }
            ).dimensions(cx - 155 + i * 105, 5, 100, 20).build());
        }

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.back"),
                btn -> client.setScreen(new ConfigScreen(parent))
        ).dimensions(cx + 55, 5, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.clear"),
                btn -> { clearTab(); scrollOffset = 0; }
        ).dimensions(cx + 160, 5, 60, 20).build());

        int totalEntries = getEntries().size();
        int maxScroll = Math.max(0, (totalEntries + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE - 1);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);

        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.log_title", Text.translatable(TAB_KEYS[currentTab])), cx, 30, 0xFFFFFF);

        List<?> entries = getEntries();
        int startIdx = scrollOffset * ENTRIES_PER_PAGE;
        int y = 55;
        int count = 0;

        for (int i = startIdx; i < entries.size() && count < ENTRIES_PER_PAGE; i++) {
            int color;
            String text;

            switch (currentTab) {
                case 0: {
                    MemoryView.ChatEntry ce = (MemoryView.ChatEntry) entries.get(i);
                    color = ce.getSpeaker().equals("player") ? 0xAAAAAA : 0x55FFFF;
                    text = "§7[" + formatTime(ce.getTimestamp()) + "] §" +
                            (ce.getSpeaker().equals("player") ? "7<" + ce.getPlayerName() + ">" : "bGlascoss") +
                            " §f" + truncate(ce.getMessage(), 100);
                    break;
                }
                case 1: {
                    ActionEntry ae = (ActionEntry) entries.get(i);
                    color = ae.isSabotaged() ? 0xFF5555 : 0x55FF55;
                    text = "§7[" + formatTime(ae.getTimestamp()) + "] §" +
                            (ae.isSabotaged() ? "c" : "a") + ae.getToolName() +
                            " §7(" + truncate(ae.getArgs(), 40) + ") §f" + truncate(ae.getResult(), 60);
                    break;
                }
                default:
                    color = 0xFFFFFF;
                    text = "";
            }

            ctx.drawTextWithShadow(textRenderer, Text.literal(text), 15, y, color);
            y += 12;
            count++;
        }

        if (entries.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("glascoss.gui.no_entries"), cx, height / 2, 0x777777);
        }

        int totalPages = Math.max(1, (entries.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.page", scrollOffset + 1, totalPages), cx, height - 20, 0x777777);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int totalEntries = getEntries().size();
        int maxScroll = Math.max(0, (totalEntries + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE - 1);
        if (amount < 0) scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        else if (amount > 0) scrollOffset = Math.max(0, scrollOffset - 1);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 264) {
            int maxScroll = Math.max(0, (getEntries().size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE - 1);
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            return true;
        }
        if (keyCode == 265) {
            scrollOffset = Math.max(0, scrollOffset - 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private List<?> getEntries() {
        switch (currentTab) {
            case 0: return MemoryView.getChatLog();
            case 1: return MemoryView.getActionLog();
            default: return List.of();
        }
    }

    private void clearTab() {
        switch (currentTab) {
            case 0: MemoryView.clearChat(); break;
            case 1: MemoryView.clearActions(); break;
        }
    }

    private String formatTime(long epochMillis) {
        return FMT.format(Instant.ofEpochMilli(epochMillis));
    }

    private String truncate(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "..." : (s == null ? "" : s);
    }

    @Override
    public void close() {
        client.setScreen(new ConfigScreen(parent));
    }

    @Override
    public boolean shouldPause() {
        return true;
    }
}
