package com.febreze.autosprintplus;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Auto Sprint is forward-only: it sprints while the forward key is held,
 * unless the player is swimming (optional) or flying (optional).
 */
public final class SprintLogic {
    private SprintLogic() {}

    public static void tick(Minecraft client) {
        if (client == null) return;

        ModConfig config = ConfigManager.getConfig();
        config.ensureValid();

        if (!config.autoSprintEnabled) return;

        LocalPlayer player = client.player;
        if (player == null) return;

        if (!client.options.keyUp.isDown()) return;
        if (config.disableInWater && player.isSwimming()) return;
        if (config.disableWhileFlying && player.getAbilities().flying) return;

        player.setSprinting(true);
    }
}
