package com.febreze.autosprintplus.compat;

import net.minecraft.resources.Identifier;

public final class Ids {
    private Ids() {}

    public static Class<?> type() {
        return Identifier.class;
    }

    public static Object of(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}
