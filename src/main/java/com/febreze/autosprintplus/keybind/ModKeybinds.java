package com.febreze.autosprintplus.keybind;

import com.febreze.autosprintplus.AutoSprintPlusMod;
import com.febreze.autosprintplus.compat.Keys;
import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.gui.ConfigScreen;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public final class ModKeybinds {
    private static final String CATEGORY_KEY = "key.category.auto-sprint-plus.main";

    private static KeyMapping openGuiKey;
    private static KeyMapping toggleSprintKey;

    private ModKeybinds() {}

    public static void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(Keys.create(
                "key.auto-sprint-plus.open_gui", GLFW.GLFW_KEY_Y, CATEGORY_KEY, AutoSprintPlusMod.MOD_ID));

        toggleSprintKey = KeyBindingHelper.registerKeyBinding(Keys.create(
                "key.auto-sprint-plus.toggle_sprint", GLFW.GLFW_KEY_UNKNOWN, CATEGORY_KEY, AutoSprintPlusMod.MOD_ID));
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
