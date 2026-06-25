package com.glascoss.gui;

import com.glascoss.memory.MemoryManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.List;

public class WorldHistoryScreen extends Screen {
    private final Screen parent;
    private List<MemoryManager.WorldSaveInfo> worlds;
    private int scrollOffset;
    private static final int PER_PAGE = 10;

    public WorldHistoryScreen(Screen parent) {
        super(Text.translatable("glascoss.gui.world_history_title"));
        this.parent = parent;
        this.scrollOffset = 0;
        this.worlds = MemoryManager.scanWorldSaves();
    }

    @Override
    protected void init() {
        int cx = width / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.refresh"),
                button -> {
                    worlds = MemoryManager.scanWorldSaves();
                    scrollOffset = 0;
                    init();
                }
        ).dimensions(cx - 100, 5, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.back"),
                button -> client.setScreen(parent)
        ).dimensions(cx - 100, height - 30, 200, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        super.render(ctx, mouseX, mouseY, delta);

        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.world_history_title"), cx, 30, 0xFFFFFF);

        int y = 55;
        int start = scrollOffset * PER_PAGE;
        int count = 0;

        for (int i = start; i < worlds.size() && count < PER_PAGE; i++) {
            MemoryManager.WorldSaveInfo w = worlds.get(i);
            String info = "§e" + w.displayName + " §7- " + w.conversationCount + " conversas";
            ctx.drawTextWithShadow(textRenderer, Text.literal(info), 15, y, 0xAAAAAA);

            int fi = i;
            drawButton(ctx, cx + 100, y, Text.translatable("glascoss.gui.manage").getString(), () -> {
                client.setScreen(new WorldDataScreen(this, w.path, w.displayName));
            });
            y += 16;
            count++;
        }

        if (worlds.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("glascoss.gui.no_worlds"), cx, height / 2, 0x777777);
        }

        int totalPages = Math.max(1, (worlds.size() + PER_PAGE - 1) / PER_PAGE);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.page", scrollOffset + 1, totalPages), cx, height - 50, 0x777777);
    }

    private void drawButton(DrawContext ctx, int x, int y, String text, Runnable action) {
        int w = textRenderer.getWidth(text) + 6;
        ctx.fill(x, y - 1, x + w, y + 10, 0x44FFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.literal(text), x + 3, y, 0x55FFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int y = 55;
        int start = scrollOffset * PER_PAGE;
        int count = 0;
        for (int i = start; i < worlds.size() && count < PER_PAGE; i++) {
            String text = Text.translatable("glascoss.gui.manage").getString();
            int w = textRenderer.getWidth(text) + 6;
            int bx = width / 2 + 100;
            int by = y;
            if (mouseX >= bx && mouseX <= bx + w && mouseY >= by && mouseY <= by + 10) {
                client.setScreen(new WorldDataScreen(this, worlds.get(i).path, worlds.get(i).displayName));
                return true;
            }
            y += 16;
            count++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int totalPages = Math.max(1, (worlds.size() + PER_PAGE - 1) / PER_PAGE);
        if (amount < 0) scrollOffset = Math.min(totalPages - 1, scrollOffset + 1);
        else if (amount > 0) scrollOffset = Math.max(0, scrollOffset - 1);
        return true;
    }

    @Override
    public void close() { client.setScreen(parent); }
    @Override
    public boolean shouldPause() { return true; }
}
