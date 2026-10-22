package com.febreze.autosprintplus.compat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class Keys {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath("auto-sprint-plus", "main"));

    private Keys() {}

    public static KeyMapping create(
            String translationKey, int keyCode,
            String categoryKey, String modId) {
        return new KeyMapping(
                translationKey,
                InputConstants.Type.KEYSYM,
                keyCode,
                CATEGORY
        );
    }
}
