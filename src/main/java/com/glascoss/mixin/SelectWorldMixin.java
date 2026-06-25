package com.glascoss.mixin;

import com.glascoss.gui.WorldHistoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
public class SelectWorldMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void glascossAddButton(CallbackInfo ci) {
        SelectWorldScreen screen = (SelectWorldScreen) (Object) this;
        MinecraftClient client = screen.client;

        screen.addDrawableChild(ButtonWidget.builder(
                Text.translatable("glascoss.gui.history"),
                button -> client.setScreen(new WorldHistoryScreen(screen))
        ).dimensions(4, 4, 80, 20).build());
    }
}
