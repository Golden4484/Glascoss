package com.glascoss.mixin;

import com.glascoss.config.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    private static final Identifier LOGO = new Identifier("glascoss", "textures/icon.png");

    @Inject(method = "init", at = @At("TAIL"))
    private void glascossAddButton(CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        MinecraftClient client = screen.client;

        screen.addDrawableChild(new ButtonWidget(4, 4, 100, 20, Text.literal(""), button -> {
            client.setScreen(new ConfigScreen(screen));
        }, supplier -> Text.translatable("glascoss.gui.glascoss")) {
            @Override
            public void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
                int x = getX();
                int y = getY();
                int w = getWidth();
                int h = getHeight();

                int bgColor = isHovered() ? 0x33FFFFFF : 0x1FFFFFFF;
                ctx.fill(x, y, x + w, y + h, bgColor);

                ctx.drawTexture(LOGO, x + 2, y + 1, 18, 18, 0, 0, 500, 500, 500, 500);

                ctx.drawText(client.textRenderer, Text.translatable("glascoss.gui.glascoss"), x + 22, y + 6, 0xFFFFFF, true);
            }
        });
    }
}
