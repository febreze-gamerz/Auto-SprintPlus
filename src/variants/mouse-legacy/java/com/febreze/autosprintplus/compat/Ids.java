package com.febreze.autosprintplus.compat;

import net.minecraft.resources.ResourceLocation;

public final class Ids {
    private Ids() {}

    public static Class<?> type() {
        return ResourceLocation.class;
    }

    public static Object of(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
