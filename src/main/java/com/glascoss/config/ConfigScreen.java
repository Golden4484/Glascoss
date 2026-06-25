package com.glascoss.config;

import com.glascoss.ai.GeminiClient;
import com.glascoss.ai.PersonalityEngine;
import com.glascoss.ai.PersonalityManager;
import com.glascoss.ai.PersonalityProfile;
import com.glascoss.gui.LogScreen;
import com.glascoss.memory.MemoryManager;
import com.glascoss.memory.WorldMemory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ConfigScreen extends Screen {
    private TextFieldWidget apiKeyField;
    private TextFieldWidget modelField;
    private final Screen parent;
    private int currentTab = 0;
    private int personalityScroll = 0;
    private boolean creatingPersonality = false;
    private TextFieldWidget newNameField;
    private TextFieldWidget newDirectiveField;
    private int selectedToneIndex = 0;
    private static final String[] TONES = {"dry_sarcastic", "aggressive", "warm", "neutral", "formal", "custom"};
    private static final String[] TONE_KEYS = {
            "glascoss.gui.tone.dry_sarcastic",
            "glascoss.gui.tone.aggressive",
            "glascoss.gui.tone.warm",
            "glascoss.gui.tone.neutral",
            "glascoss.gui.tone.formal",
            "glascoss.gui.tone.custom"
    };
    private static final String[] TABS = {"glascoss.gui.tab_general", "glascoss.gui.tab_personality"};

    public ConfigScreen(Screen parent) {
        super(Text.translatable("glascoss.gui.glascoss"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        int cx = width / 2;

        for (int i = 0; i < TABS.length; i++) {
            int tabIdx = i;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(currentTab == i ? "> " : "  ").append(Text.translatable(TABS[i])),
                    btn -> { currentTab = tabIdx; init(); }
            ).dimensions(cx - 120 + i * 125, 5, 120, 20).build());
        }

        if (currentTab == 0) initGeneralTab(cx);
        else initPersonalityTab(cx);
    }

    private void initGeneralTab(int cx) {
        int y = 40;

        apiKeyField = new TextFieldWidget(textRenderer, cx - 120, y, 240, 20, Text.translatable("glascoss.gui.api_key"));
        apiKeyField.setMaxLength(256);
        apiKeyField.setText(ModConfig.getApiKey());
        addDrawableChild(apiKeyField);

        y += 25;
        modelField = new TextFieldWidget(textRenderer, cx - 120, y, 240, 20, Text.translatable("glascoss.gui.model"));
        modelField.setMaxLength(64);
        modelField.setText(ModConfig.getModel());
        addDrawableChild(modelField);

        y += 25;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.save"),
                button -> save()
        ).dimensions(cx - 120, y, 115, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.back"),
                button -> client.setScreen(parent)
        ).dimensions(cx + 5, y, 115, 20).build());

        y += 30;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable(ModConfig.isShowCommands() ? "glascoss.gui.show_commands" : "glascoss.gui.hide_commands"),
                button -> {
                    ModConfig.setShowCommands(!ModConfig.isShowCommands());
                    init();
                }
        ).dimensions(cx - 120, y, 240, 20).build());

        y += 25;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable(ModConfig.isDisableOnOnline() ? "glascoss.gui.disable_online" : "glascoss.gui.enable_online"),
                button -> {
                    ModConfig.setDisableOnOnline(!ModConfig.isDisableOnOnline());
                    init();
                }
        ).dimensions(cx - 120, y, 240, 20).build());

        y += 25;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.action_log"),
                button -> client.setScreen(new LogScreen(this))
        ).dimensions(cx - 120, y, 240, 20).build());

        y += 25;
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.reset_memory"),
                button -> {
                    if (client.world != null && client.getServer() != null) {
                        var overworld = client.getServer().getOverworld();
                        String wid = com.glascoss.memory.MemoryManager.getWorldId(overworld);
                        PersonalityEngine.resetWorld(wid);
                        java.nio.file.Path savePath = overworld.getServer().getSavePath(net.minecraft.util.WorldSavePath.ROOT);
                        java.nio.file.Path memDir = savePath.resolve("glascoss");
                        try { java.nio.file.Files.walk(memDir).sorted(java.util.Comparator.reverseOrder()).forEach(p -> { try { java.nio.file.Files.deleteIfExists(p); } catch (Exception ignored) {} }); } catch (Exception ignored) {}
                        client.getServer().getPlayerManager().broadcast(Text.translatable("glascoss.chat.memory_reset"), false);
                    }
                }
        ).dimensions(cx - 120, y, 240, 20).build());

        y += 25;
        boolean configured = !ModConfig.getApiKey().isEmpty();
        String statusKey = configured ? "glascoss.gui.api_configured" : "glascoss.gui.api_not_configured";
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.api_status", Text.translatable(statusKey)),
                button -> {}
        ).dimensions(cx - 120, y, 240, 20).build());
    }

    private void initPersonalityTab(int cx) {
        int y = 40;
        PersonalityProfile active = PersonalityManager.getActiveProfile();
        String activeName = active != null ? active.getName() : Text.translatable("glascoss.gui.api_not_configured").getString();

        addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.active_personality", activeName),
                button -> {}
        ).dimensions(cx - 120, y, 240, 20).build());

        y += 26;

        if (!creatingPersonality) {
            addDrawableChild(ButtonWidget.builder(
                    Text.translatable("glascoss.gui.create_personality"),
                    button -> {
                        creatingPersonality = true;
                        init();
                    }
            ).dimensions(cx - 120, y, 240, 20).build());
            y += 25;
        }

        if (creatingPersonality) {
            newNameField = new TextFieldWidget(textRenderer, cx - 120, y, 240, 20, Text.translatable("glascoss.gui.personality_name"));
            newNameField.setMaxLength(32);
            newNameField.setPlaceholder(Text.translatable("glascoss.gui.personality_name_placeholder"));
            addDrawableChild(newNameField);

            y += 25;
            addDrawableChild(ButtonWidget.builder(
                    Text.translatable("glascoss.gui.personality_tone", Text.translatable(TONE_KEYS[selectedToneIndex])),
                    button -> {
                        selectedToneIndex = (selectedToneIndex + 1) % TONES.length;
                        init();
                    }
            ).dimensions(cx - 120, y, 240, 20).build());

            y += 25;
            newDirectiveField = new TextFieldWidget(textRenderer, cx - 120, y, 240, 40, Text.translatable("glascoss.gui.personality_directive"));
            newDirectiveField.setMaxLength(512);
            newDirectiveField.setPlaceholder(Text.translatable("glascoss.gui.personality_directive_placeholder"));
            addDrawableChild(newDirectiveField);

            y += 45;
            addDrawableChild(ButtonWidget.builder(
                    Text.translatable("glascoss.gui.save"),
                    button -> saveNewPersonality()
            ).dimensions(cx - 120, y, 115, 20).build());

            addDrawableChild(ButtonWidget.builder(
                    Text.translatable("glascoss.gui.cancel"),
                    button -> {
                        creatingPersonality = false;
                        init();
                    }
            ).dimensions(cx + 5, y, 115, 20).build());

            y += 30;
        }

        List<PersonalityProfile> all = PersonalityManager.getAllProfiles();
        int visibleCount = Math.min(10, all.size());

        y += 8;
        int sepY = y;
        addDrawable((Drawable) (ctx, mx, my, delta) ->
                ctx.fill(cx - 120, sepY, cx + 120, sepY + 1, 0x44FFFFFF));
        y += 10;

        for (int i = personalityScroll; i < all.size() && i < personalityScroll + visibleCount; i++) {
            PersonalityProfile p = all.get(i);
            boolean isActive = p.getId().equals(ModConfig.getActivePersonality());
            String label = (isActive ? "> " : "  ") + p.getName();

            int row = i - personalityScroll;
            int by = y + row * 24;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal(label),
                    button -> {
                        ModConfig.setActivePersonality(p.getId());
                        resetWorldConversations();
                        init();
                    }
            ).dimensions(cx - 120, by, 240, 20).build());

            if (!p.getId().equals("glascoss")) {
                addDrawableChild(ButtonWidget.builder(
                        Text.translatable("glascoss.gui.delete_personality"),
                        button -> {
                            PersonalityManager.removeProfile(p.getId());
                            init();
                        }
                ).dimensions(cx + 125, by, 20, 20).build());
            }
        }

        if (all.size() > visibleCount) {
            int totalPages = (all.size() + visibleCount - 1) / visibleCount;
            int curPage = personalityScroll / visibleCount + 1;
            addDrawableChild(ButtonWidget.builder(
                    Text.translatable("glascoss.gui.scroll", curPage, totalPages),
                    button -> {}
            ).dimensions(cx - 120, y + visibleCount * 24 + 5, 240, 20).build());
        }
    }

    private void saveNewPersonality() {
        String name = newNameField.getText().trim();
        if (name.isEmpty()) return;
        String tone = TONES[selectedToneIndex];
        String directive = newDirectiveField.getText().trim();
        if (directive.isEmpty()) directive = "Custom personality: " + name;
        String id = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        PersonalityManager.addProfile(new PersonalityProfile(id, name, tone, directive));
        creatingPersonality = false;
        ModConfig.setActivePersonality(id);
        resetWorldConversations();
        init();
    }

    private void resetWorldConversations() {
        if (client.world != null && client.getServer() != null) {
            var overworld = client.getServer().getOverworld();
            WorldMemory wm = MemoryManager.getWorldMemory(overworld);
            wm.resetConversations();
            wm.setActivePersonality(ModConfig.getActivePersonality());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (currentTab == 1) {
            List<PersonalityProfile> all = PersonalityManager.getAllProfiles();
            int maxScroll = Math.max(0, all.size() - Math.min(10, all.size()));
            if (amount < 0) personalityScroll = Math.min(maxScroll, personalityScroll + 1);
            else if (amount > 0) personalityScroll = Math.max(0, personalityScroll - 1);
            init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    private void save() {
        String key = apiKeyField.getText().trim();
        String model = modelField.getText().trim();
        ModConfig.setApiKey(key);
        if (!model.isEmpty()) {
            ModConfig.setModel(model);
            GeminiClient.getInstance().setModel(model);
        }
        GeminiClient.getInstance().setApiKey(key);
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.translatable("glascoss.gui.config_screen", Text.translatable(TABS[currentTab])), cx, 25, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() { client.setScreen(parent); }
    @Override
    public boolean shouldPause() { return true; }
}
