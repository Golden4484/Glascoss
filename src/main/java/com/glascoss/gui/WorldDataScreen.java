package com.glascoss.gui;

import com.glascoss.memory.MemoryManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class WorldDataScreen extends Screen {
    private final Screen parent;
    private final Path worldPath;
    private final String worldName;
    private MemoryManager.WorldSaveInfo info;

    public WorldDataScreen(Screen parent, Path worldPath, String worldName) {
        super(Text.translatable("glascoss.gui.data_title", worldName));
        this.parent = parent;
        this.worldPath = worldPath;
        this.worldName = worldName;
        refreshInfo();
    }

    private void refreshInfo() {
        java.util.List<MemoryManager.WorldSaveInfo> worlds = MemoryManager.scanWorldSaves();
        for (MemoryManager.WorldSaveInfo w : worlds) {
            if (w.path.equals(worldPath)) {
                this.info = w;
                return;
            }
        }
        this.info = null;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        refreshInfo();

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.view_conversations"),
                button -> client.setScreen(new ConversationViewerScreen(this, worldPath, null))
        ).dimensions(cx - 100, 70, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.delete_all"),
                button -> {
                    if (MemoryManager.deleteWorldMemory(worldPath)) {
                        client.setScreen(parent);
                    }
                }
        ).dimensions(cx - 100, 140, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.back"),
                button -> client.setScreen(parent)
        ).dimensions(cx - 100, 170, 200, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(worldName), cx, 15, 0xFFFFFF);

        if (info != null) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§7Conversas: §f" + info.conversationCount
                            + "  §7Interações: §f" + info.totalInteractions),
                    cx, 40, 0xAAAAAA);
        }

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.folder_label", worldPath.getFileName().toString()), cx, 55, 0x555555);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() { client.setScreen(parent); }
    @Override
    public boolean shouldPause() { return true; }
}
