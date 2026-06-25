package com.glascoss.gui;

import com.glascoss.config.ConfigScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class InGameScreen extends Screen {
    private final Screen parent;
    private final String worldName;
    private final Path worldPath;

    public InGameScreen(Screen parent, String worldName, Path worldPath) {
        super(Text.translatable("glascoss.gui.ingame_title", worldName));
        this.parent = parent;
        this.worldName = worldName;
        this.worldPath = worldPath;
    }

    @Override
    protected void init() {
        int cx = width / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.conversation_history"),
                button -> client.setScreen(new ConversationViewerScreen(this, worldPath, null))
        ).dimensions(cx - 100, 60, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.settings"),
                button -> client.setScreen(new ConfigScreen(this))
        ).dimensions(cx - 100, 90, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.back"),
                button -> {
                    if (parent instanceof net.minecraft.client.gui.screen.GameMenuScreen) {
                        client.setScreen(null);
                        client.setScreen(parent);
                    } else {
                        client.setScreen(parent);
                    }
                }
        ).dimensions(cx - 100, 160, 200, 20).build());
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.glascoss"), cx, 15, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.world_label", worldName), cx, 35, 0xAAAAAA);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() { client.setScreen(parent); }
    @Override
    public boolean shouldPause() { return true; }
}
