package com.febreze.autosprintplus.keybind;

import com.febreze.autosprintplus.AutoSprintPlusMod;
import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.gui.ConfigScreen;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class ModKeybinds {
    private ModKeybinds() {}

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(AutoSprintPlusMod.MOD_ID, "main")
    );

    public static KeyMapping openGuiKey;
    public static KeyMapping toggleSprintKey;

    public static void register() {
        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.auto-sprint-plus.open_gui",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_Y,
                CATEGORY
        ));

        toggleSprintKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.auto-sprint-plus.toggle_sprint",
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                CATEGORY
        ));
    }

    public static void handle(Minecraft client) {
        while (openGuiKey.consumeClick()) {
            client.setScreen(new ConfigScreen(client.screen));
        }

        while (toggleSprintKey.consumeClick()) {
            ConfigManager.getConfig().autoSprintEnabled = !ConfigManager.getConfig().autoSprintEnabled;
            ConfigManager.save();
        }
    }
}
