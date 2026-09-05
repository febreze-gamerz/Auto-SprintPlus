package com.febreze.autosprintplus;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class SprintLogic {
    private SprintLogic() {}

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        ModConfig config = ConfigManager.getConfig();
        config.ensureValid();

        if (!config.autoSprintEnabled) return;
        // Match vanilla sprint prerequisites that are publicly accessible in 1.21.
        if (player.isSneaking() || player.getHungerManager().getFoodLevel() <= 6) return;
        if (config.disableInWater && player.isSwimming()) return;
        if (config.disableWhileFlying && player.getAbilities().flying) return;

        if (player.input.getMovementInput().y <= 0.0f) return;
        player.setSprinting(true);
    }
}
