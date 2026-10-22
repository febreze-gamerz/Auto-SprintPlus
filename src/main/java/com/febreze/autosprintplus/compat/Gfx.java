package com.febreze.autosprintplus.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Scaled text for the pose-stack APIs used by Minecraft 1.21.x.
 *
 * Minecraft's own methods must be looked up by their runtime names. In a development
 * environment those are Mojang names; in the game Fabric Loader has remapped them to
 * intermediary names (method_NNNNN). Each lookup tries the intermediary name first,
 * then the Mojang name. JOML methods (pushMatrix, translate, scale on Matrix3x2fStack)
 * are not obfuscated, so their Mojang names are the runtime names.
 *
 * If the transform methods cannot be found, text is drawn unscaled at its correct
 * position, and a warning is printed once. It is never drawn at the origin of an
 * untransformed pose.
 */
public final class Gfx {
    // GuiGraphics.pose()  (intermediary: DrawContext.getMatrices)
    private static final String[] POSE_GETTER = {"method_51448", "pose"};
    // PoseStack / MatrixStack push, pop, translate, scale; Matrix3x2fStack pushMatrix/popMatrix/translate/scale
    private static final String[] PUSH = {"method_22903", "pushPose", "pushMatrix"};
    private static final String[] POP = {"method_22909", "popPose", "popMatrix"};
    private static final String[] TRANSLATE = {"method_22904", "translate"};
    private static final String[] SCALE = {"method_22905", "scale"};

    private static Method poseGetter;
    private static Class<?> cachedClass;
    private static Transform cached;
    private static boolean warned;

    private Gfx() {}

    /** Draws text with its top-left corner at (x, y), scaled by {@code scale} around that corner. */
    public static void drawScaledText(GuiGraphics graphics, Font font, String text,
                                      float x, float y, float scale, int color, boolean shadow) {
        Object pose = pose(graphics);
        Transform t = pose == null ? null : resolve(pose.getClass());

        if (t == null) {
            warnOnce();
            graphics.drawString(font, text, Math.round(x), Math.round(y), color, shadow);
            return;
        }

        invoke(t.push, pose);
        invoke(t.translate, pose, x, y, t.translate3 ? 0.0 : Double.NaN);
        invoke(t.scale, pose, scale, scale, t.scale3 ? 1.0 : Double.NaN);
        graphics.drawString(font, text, 0, 0, color, shadow);
        invoke(t.pop, pose);
    }

    private static synchronized Object pose(GuiGraphics graphics) {
        try {
            if (poseGetter == null) {
                poseGetter = findNoArg(GuiGraphics.class, POSE_GETTER);
                if (poseGetter == null || poseGetter.getReturnType() == void.class) return null;
            }
            return poseGetter.invoke(graphics);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Auto Sprint+ could not read the GUI pose stack", e);
        }
    }

    private static synchronized Transform resolve(Class<?> poseClass) {
        if (poseClass == cachedClass) return cached;

        Transform t = new Transform();
        t.push = findNoArg(poseClass, PUSH);
        t.pop = findNoArg(poseClass, POP);

        t.translate = find(poseClass, TRANSLATE, 2);
        t.translate3 = false;
        if (t.translate == null) {
            t.translate = find(poseClass, TRANSLATE, 3);
            t.translate3 = true;
        }

        t.scale = find(poseClass, SCALE, 2);
        t.scale3 = false;
        if (t.scale == null) {
            t.scale = find(poseClass, SCALE, 3);
            t.scale3 = true;
        }

        if (t.push == null || t.pop == null || t.translate == null || t.scale == null) {
            t = null;
        }

        cachedClass = poseClass;
        cached = t;
        return t;
    }

    private static void warnOnce() {
        if (warned) return;
        warned = true;
        System.err.println("[Auto-SprintPlus] Could not find the GUI transform methods; "
                + "HUD text will not scale. Please report this with your Minecraft version.");
    }

    /** Public zero-argument method with one of the given names. */
    private static Method findNoArg(Class<?> owner, String[] names) {
        for (Method m : owner.getMethods()) {
            if (m.getParameterCount() == 0 && matches(m.getName(), names)) return m;
        }
        return null;
    }

    /** Public method with one of the given names and arity whose parameters are all float or double. */
    private static Method find(Class<?> owner, String[] names, int arity) {
        for (Method m : owner.getMethods()) {
            if (m.getParameterCount() != arity || !matches(m.getName(), names)) continue;
            boolean numeric = true;
            for (Class<?> p : m.getParameterTypes()) {
                if (p != float.class && p != double.class) {
                    numeric = false;
                    break;
                }
            }
            if (numeric) return m;
        }
        return null;
    }

    /**
     * Calls {@code m} with the given numeric arguments, converted to the parameter types.
     * A NaN argument marks an optional trailing parameter the method does not take.
     */
    private static void invoke(Method m, Object target, double... args) {
        Class<?>[] types = m.getParameterTypes();
        Object[] values = new Object[types.length];
        int next = 0;
        for (double arg : args) {
            if (Double.isNaN(arg)) continue;
            values[next] = types[next] == float.class ? (Object) (float) arg : (Object) arg;
            next++;
        }
        try {
            m.invoke(target, values);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Auto Sprint+ pose call failed: " + m, e);
        }
    }

    private static boolean matches(String name, String[] names) {
        for (String candidate : names) {
            if (candidate.equals(name)) return true;
        }
        return false;
    }

    private static final class Transform {
        Method push;
        Method pop;
        Method translate;
        Method scale;
        boolean translate3;
        boolean scale3;
    }
}
