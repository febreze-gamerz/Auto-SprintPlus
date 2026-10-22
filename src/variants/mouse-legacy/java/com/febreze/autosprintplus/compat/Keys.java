package com.febreze.autosprintplus.compat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class Keys {
    private Keys() {}

    public static KeyMapping create(
            String translationKey, int keyCode,
            String categoryKey, String modId) {
        return new KeyMapping(
                translationKey,
                InputConstants.Type.KEYSYM,
                keyCode,
                categoryKey
        );
    }
}
