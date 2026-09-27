package com.febreze.autosprintplus;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.hud.HudRenderer;
import com.febreze.autosprintplus.keybind.ModKeybinds;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class AutoSprintPlusMod implements ClientModInitializer {
    public static final String MOD_ID = "auto-sprint-plus";

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ModKeybinds.register();
        HudRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModKeybinds.handle(client);
            SprintLogic.tick(client);
        });
    }
}
