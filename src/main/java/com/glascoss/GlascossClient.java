package com.glascoss;

import com.glascoss.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;

public class GlascossClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModNetworking.registerClient();
    }
}
