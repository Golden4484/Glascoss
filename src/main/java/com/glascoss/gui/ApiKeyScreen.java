package com.glascoss.gui;

import com.glascoss.ai.GeminiClient;
import com.glascoss.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ApiKeyScreen extends Screen {
    private TextFieldWidget apiKeyField;
    private final Screen parent;

    public ApiKeyScreen(Screen parent) {
        super(Text.translatable("glascoss.gui.api_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;

        apiKeyField = new TextFieldWidget(textRenderer, cx - 120, 80, 240, 20, Text.translatable("glascoss.gui.api_key_label"));
        apiKeyField.setMaxLength(256);
        apiKeyField.setText(ModConfig.getApiKey());
        addDrawableChild(apiKeyField);

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.save"),
                button -> save()
        ).dimensions(cx - 120, 110, 115, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.back"),
                button -> client.setScreen(parent)
        ).dimensions(cx + 5, 110, 115, 20).build());
    }

    private void save() {
        String key = apiKeyField.getText().trim();
        ModConfig.setApiKey(key);
        GeminiClient.getInstance().setApiKey(key);
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("glascoss.gui.glascoss"), cx, 30, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.translatable("glascoss.gui.api_key_label"), cx, 65, 0xAAAAAA);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.current_model", ModConfig.getModel()), cx, 145, 0x777777);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() { client.setScreen(parent); }
    @Override
    public boolean shouldPause() { return true; }
}
