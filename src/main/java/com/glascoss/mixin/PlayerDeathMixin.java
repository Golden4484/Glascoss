package com.glascoss.mixin;

import com.glascoss.memory.MemoryManager;
import com.glascoss.memory.WorldMemory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerDeathMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (!player.getServerWorld().isClient()) {
            WorldMemory memory = MemoryManager.getWorldMemory(player.getServerWorld());
            memory.setDied(player.getUuid());
        }
    }
}
