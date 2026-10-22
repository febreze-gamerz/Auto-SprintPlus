package com.febreze.autosprintplus.compat;

import net.minecraft.client.gui.GuiGraphics;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Registers a HUD renderer with whichever Fabric HUD API is present:
 * HudElementRegistry (newer 1.21.x builds) or the older HudRenderCallback.
 * Both are reached reflectively, so the same source compiles across versions.
 */
public final class HudHook {
    private static final String MOD_ID = "auto-sprint-plus";
    private static final String ELEMENT_REGISTRY = "net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry";
    private static final String VANILLA_ELEMENTS = "net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements";
    private static final String RENDER_CALLBACK = "net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback";

    private HudHook() {}

    @FunctionalInterface
    public interface Renderer {
        void render(GuiGraphics graphics);
    }

    public static void register(String id, Renderer renderer) {
        Class<?> registry;
        try {
            registry = Class.forName(ELEMENT_REGISTRY);
        } catch (ClassNotFoundException ignored) {
            registerCallback(renderer);
            return;
        }
        try {
            registerElement(registry, id, renderer);
        } catch (ReflectiveOperationException | RuntimeException e) {
            // The newer API class exists but is not usable here (e.g. an older Fabric API); use the callback instead.
            registerCallback(renderer);
        }
    }

    private static void registerElement(Class<?> registry, String id, Renderer renderer)
            throws ReflectiveOperationException {
        Object hotbar = Class.forName(VANILLA_ELEMENTS).getField("HOTBAR").get(null);

        Method attach = null;
        for (Method m : registry.getMethods()) {
            if (m.getName().equals("attachElementAfter") && m.getParameterCount() == 3) {
                attach = m;
                break;
            }
        }
        if (attach == null) {
            throw new NoSuchMethodException("HudElementRegistry.attachElementAfter");
        }

        Class<?> elementType = attach.getParameterTypes()[2];
        attach.invoke(null, hotbar, Ids.of(MOD_ID, id), proxy(elementType, renderer));
    }

    private static void registerCallback(Renderer renderer) {
        try {
            Class<?> callback = Class.forName(RENDER_CALLBACK);
            Object event = callback.getField("EVENT").get(null);

            Class<?> eventClass = Class.forName("net.fabricmc.fabric.api.event.Event");
            Method register = eventClass.getMethod("register", Object.class);
            register.invoke(event, proxy(callback, renderer));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Auto Sprint+ could not register its HUD callback", e);
        }
    }

    private static Object proxy(Class<?> iface, Renderer renderer) {
        return Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[]{iface}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "AutoSprintPlusHud";
                default:
                    break;
            }
            if (method.getDeclaringClass() == Object.class) return null;
            renderer.render((GuiGraphics) args[0]);
            return null;
        });
    }
}
