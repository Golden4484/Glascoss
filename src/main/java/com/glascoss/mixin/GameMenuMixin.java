package com.glascoss.mixin;

import com.glascoss.gui.InGameScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void glascossAddButton(CallbackInfo ci) {
        GameMenuScreen screen = (GameMenuScreen) (Object) this;
        MinecraftClient client = screen.client;

        java.nio.file.Path worldPath = client.getServer() != null
                ? client.getServer().getSavePath(WorldSavePath.ROOT)
                : null;

        String worldName = worldPath != null
                ? worldPath.getFileName().toString()
                : "Desconhecido";

        java.nio.file.Path wp = worldPath;
        screen.addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.glascoss"),
                button -> client.setScreen(new InGameScreen(screen, worldName, wp))
        ).dimensions(screen.width - 108, 4, 104, 20).build());
    }
}
