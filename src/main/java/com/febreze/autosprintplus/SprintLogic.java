package com.febreze.autosprintplus;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Sprint logic intentionally avoids compile-time references to Minecraft's
 * client-player classes. This keeps the mod compatible with older/newer
 * Fabric API 26.2 patch releases without requiring Fabric's injected player
 * interfaces on the compiler classpath.
 */
public final class SprintLogic {
    private SprintLogic() {}

    /**
     * Accept Object on purpose: referencing Minecraft directly from this class
     * can make javac resolve Fabric-injected interfaces that are absent from
     * some Fabric API 0.153.x compile classpaths.
     */
    public static void tick(Object client) {
        if (client == null) return;

        ModConfig config = ConfigManager.getConfig();
        config.ensureValid();

        if (!config.autoSprintEnabled) return;

        Object player = readField(client, "player");
        if (player == null) return;

        // Auto Sprint is always forward-only by design.
        Object options = readField(client, "options");
        Object keyUp = options == null ? null : readField(options, "keyUp");
        if (!invokeBoolean(keyUp, "isDown")) return;

        if (config.disableInWater && invokeBoolean(player, "isSwimming")) return;
        if (config.disableWhileFlying && isFlying(player)) return;

        invokeSetSprinting(player, true);
    }

    private static Object readField(Object target, String name) {
        if (target == null) return null;

        try {
            Field field = findField(target.getClass(), name);
            return field.get(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static boolean isFlying(Object player) {
        try {
            Method getAbilities = findMethod(player.getClass(), "getAbilities");
            Object abilities = getAbilities.invoke(player);
            if (abilities == null) return false;

            Field flying = findField(abilities.getClass(), "flying");
            return flying.getBoolean(abilities);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static boolean invokeBoolean(Object target, String methodName) {
        if (target == null) return false;

        try {
            Method method = findMethod(target.getClass(), methodName);
            Object value = method.invoke(target);
            return value instanceof Boolean b && b;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static void invokeSetSprinting(Object player, boolean value) {
        try {
            Method method = findMethod(
                    player.getClass(),
                    "setSprinting",
                    boolean.class
            );
            method.invoke(player, value);
        } catch (ReflectiveOperationException ignored) {
            // Gracefully fail if a future 26.x version changes the method.
        }
    }

    private static Method findMethod(
            Class<?> type,
            String name,
            Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        Class<?> current = type;

        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }

        // Public/inherited interface method fallback.
        Method method = type.getMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    private static Field findField(
            Class<?> type,
            String name
    ) throws NoSuchFieldException {
        Class<?> current = type;

        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchFieldException(name);
    }
}
