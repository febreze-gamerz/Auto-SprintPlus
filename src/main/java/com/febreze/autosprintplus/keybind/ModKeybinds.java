package com.febreze.autosprintplus.keybind;

import com.febreze.autosprintplus.AutoSprintPlusMod;
import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.gui.ConfigScreen;

import net.minecraft.client.util.InputUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public final class ModKeybinds {
    private ModKeybinds() {}

    private static final String CATEGORY = "key.categories.auto-sprint-plus";

    public static KeyBinding openGuiKey;
    public static KeyBinding toggleSprintKey;

    public static void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auto-sprint-plus.open_gui",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_Y,
                CATEGORY
        ));

        toggleSprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.auto-sprint-plus.toggle_sprint",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                CATEGORY
        ));
    }

    public static void handle(MinecraftClient client) {
        while (openGuiKey.wasPressed()) {
            client.setScreen(new ConfigScreen(client.currentScreen));
        }

        while (toggleSprintKey.wasPressed()) {
            ConfigManager.getConfig().autoSprintEnabled = !ConfigManager.getConfig().autoSprintEnabled;
            ConfigManager.save();
        }
    }
}
