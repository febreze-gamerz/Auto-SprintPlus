package com.febreze.autosprintplus;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class SprintLogic {
    private SprintLogic() {}

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        ModConfig config = ConfigManager.getConfig();
        config.ensureValid();

        // Auto Sprint disabled: leave vanilla Minecraft sprint behavior untouched.
        if (!config.autoSprintEnabled || !player.canSprint()) return;

        if (config.disableInWater && player.isSwimming()) return;
        if (config.disableWhileFlying && player.getAbilities().flying) return;

        // Auto Sprint is always forward-only by design.
        if (player.input.getMoveVector().y <= 0.0f) return;

        player.setSprinting(true);
    }
}
