package com.febreze.autosprintplus.keybind;

import com.febreze.autosprintplus.AutoSprintPlusMod;
import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.gui.ConfigScreen;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class ModKeybinds {
    private ModKeybinds() {}

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(AutoSprintPlusMod.MOD_ID, "main")
    );

    public static KeyMapping openGuiKey;
    public static KeyMapping toggleSprintKey;

    public static void register() {
        openGuiKey = KeyMappingHelper.registerKeyMapping(
                createKeyMapping(
                        "key.auto-sprint-plus.open_gui",
                        InputConstants.KEY_Y
                )
        );

        toggleSprintKey = KeyMappingHelper.registerKeyMapping(
                createKeyMapping(
                        "key.auto-sprint-plus.toggle_sprint",
                        InputConstants.UNKNOWN.getValue()
                )
        );
    }

    /**
     * Creates a keyboard key mapping without directly referencing
     * InputConstants.Type.KEYSYM or Type.KEYBOARD.
     *
     * Minecraft 26.2 uses:
     *     InputConstants.Type.KEYSYM
     *
     * Minecraft 26.3 uses:
     *     InputConstants.Type.KEYBOARD
     *
     * Reflection keeps the main source compatible with both versions.
     */
    private static KeyMapping createKeyMapping(String translationKey, int keyCode) {
        try {
            Class<InputConstants.Type> typeClass = InputConstants.Type.class;

            InputConstants.Type keyboardType;

            try {
                // Minecraft 26.3+
                keyboardType = Enum.valueOf(typeClass, "KEYBOARD");
            } catch (IllegalArgumentException ignored) {
                // Minecraft 26.2 and older
                keyboardType = Enum.valueOf(typeClass, "KEYSYM");
            }

            Constructor<KeyMapping> constructor = KeyMapping.class.getConstructor(
                    String.class,
                    typeClass,
                    int.class,
                    KeyMapping.Category.class
            );

            return constructor.newInstance(
                    translationKey,
                    keyboardType,
                    keyCode,
                    CATEGORY
            );

        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Auto Sprint+ could not create a keyboard key mapping for this Minecraft version.",
                    e
            );
        }
    }

    public static void handle(Minecraft client) {
        while (openGuiKey.consumeClick()) {
            client.gui.setScreen(new ConfigScreen(client.gui.screen()));
        }

        while (toggleSprintKey.consumeClick()) {
            ConfigManager.getConfig().autoSprintEnabled =
                    !ConfigManager.getConfig().autoSprintEnabled;

            ConfigManager.save();
        }
    }
}
