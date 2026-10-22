package com.febreze.autosprintplus.hud;

import com.febreze.autosprintplus.AutoSprintPlusMod;
import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Draws the Auto Sprint+ HUD box.
 *
 * All sizing comes from {@link #size(ModConfig)}. The box is never smaller than its
 * base size and always grows to contain the text. The text is scaled by
 * hudScale * textScale, so scaling the HUD scales its text with it, and the text is
 * centred inside the box.
 */
public final class HudRenderer {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(AutoSprintPlusMod.MOD_ID, "hud");

    private static final float BASE_W = 190.0f;
    private static final float BASE_H = 26.0f;
    private static final float PAD_X = 8.0f;
    private static final float PAD_Y = 4.0f;

    private HudRenderer() {}

    public static void register() {
        HudElementRegistry.addLast(ID, HudRenderer::extract);
    }

    public static String sprintText(ModConfig c) {
        return c.autoSprintEnabled ? "Sprint: Toggled" : "Sprint: Vanilla";
    }

    /** Effective text scale: the HUD scale multiplied by the text-only scale. */
    public static float textScale(ModConfig c) {
        return c.hudScale * c.textScale;
    }

    /** Full box size in GUI pixels, as {width, height}. */
    public static float[] size(ModConfig c) {
        c.ensureValid();
        Font font = Minecraft.getInstance().font;
        float ts = textScale(c);
        float textW = font.width(Component.literal(sprintText(c))) * ts;
        float textH = font.lineHeight * ts;

        float w = Math.max(BASE_W * c.hudScale, textW + 2.0f * PAD_X * c.hudScale);
        float h = Math.max(BASE_H * c.hudScale, textH + 2.0f * PAD_Y * c.hudScale);
        return new float[]{w, h};
    }

    public static void extract(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();
        if (!c.hudEnabled || client.gui.hud.isHidden()) return;

        int screenW = client.getWindow().getGuiScaledWidth();
        int screenH = client.getWindow().getGuiScaledHeight();
        float[] sz = size(c);

        float x = resolveX(c, screenW, sz[0]);
        float y = resolveY(c, screenH, sz[1]);
        c.hudX = x;
        c.hudY = y;

        renderHudBox(graphics, x, y, c, false, false);
    }

    public static float resolveX(ModConfig c, int screenW, float hudWidth) {
        float maxX = Math.max(0.0f, screenW - hudWidth);
        if (c.hudNormalizedX < 0.0f) {
            c.hudNormalizedX = maxX <= 0.0f ? 0.0f : clamp01(c.hudX / maxX);
        }
        return clamp(c.hudNormalizedX * maxX, 0.0f, maxX);
    }

    public static float resolveY(ModConfig c, int screenH, float hudHeight) {
        float maxY = Math.max(0.0f, screenH - hudHeight);
        if (c.hudNormalizedY < 0.0f) {
            c.hudNormalizedY = maxY <= 0.0f ? 0.0f : clamp01(c.hudY / maxY);
        }
        return clamp(c.hudNormalizedY * maxY, 0.0f, maxY);
    }

    /** Stores a pixel position as normalised coordinates, clamped so the whole box stays on screen. */
    public static void setPositionFromPixels(ModConfig c, float x, float y, int screenW, int screenH) {
        float[] sz = size(c);
        float maxX = Math.max(0.0f, screenW - sz[0]);
        float maxY = Math.max(0.0f, screenH - sz[1]);
        c.hudX = clamp(x, 0.0f, maxX);
        c.hudY = clamp(y, 0.0f, maxY);
        c.hudNormalizedX = maxX <= 0.0f ? 0.0f : c.hudX / maxX;
        c.hudNormalizedY = maxY <= 0.0f ? 0.0f : c.hudY / maxY;
    }

    public static void migrateAbsolutePosition(ModConfig c, int screenW, int screenH) {
        if (c.hudNormalizedX < 0.0f || c.hudNormalizedY < 0.0f) {
            setPositionFromPixels(c, c.hudX, c.hudY, screenW, screenH);
        }
    }

    public static void renderHudBox(GuiGraphicsExtractor graphics, float x, float y, ModConfig c,
                                    boolean selected, boolean forceBackground) {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        c.ensureValid();

        float[] sz = size(c);
        float w = sz[0];
        float h = sz[1];
        float scale = c.hudScale;

        int sx = Math.round(x);
        int sy = Math.round(y);
        int boxW = Math.round(w);
        int boxH = Math.round(h);
        int radius = Math.round(c.roundedCorners * scale);

        if (c.hudBackground || forceBackground) {
            int alpha = Math.max(0, Math.min(255, Math.round(c.hudOpacity * 255.0f)));
            int bg = (alpha << 24) | (c.backgroundColor.getColor() & 0x00FFFFFF);
            drawRoundedRect(graphics, sx, sy, boxW, boxH, radius, bg);
        }

        if (c.borderEnabled) {
            int borderColor = selected ? 0xFFFFFFFF : c.borderColor.getColor();
            int borderPx = Math.max(1, Math.round(c.borderWidth * scale));
            for (int i = 0; i < borderPx; i++) {
                drawRoundedOutline(graphics, sx + i, sy + i, boxW - i * 2, boxH - i * 2,
                        Math.max(0, radius - i), borderColor);
            }
        }

        String text = sprintText(c);
        float ts = textScale(c);
        float textW = font.width(Component.literal(text)) * ts;
        float textH = font.lineHeight * ts;
        float textX = x + (w - textW) / 2.0f;
        float textY = y + (h - textH) / 2.0f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(textX, textY);
        graphics.pose().scale(ts, ts);
        graphics.text(font, text, 0, 0, c.textColor.getColor(), c.textShadow);
        graphics.pose().popMatrix();
    }

    private static void drawRoundedRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                        int radius, int color) {
        radius = Math.min(radius, Math.min(width, height) / 2);
        if (radius <= 0) {
            graphics.fill(x, y, x + width, y + height, color);
            return;
        }
        for (int row = 0; row < radius; row++) {
            int inset = cornerInset(radius, row);
            graphics.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
            graphics.fill(x + inset, y + height - row - 1, x + width - inset, y + height - row, color);
        }
        graphics.fill(x, y + radius, x + width, y + height - radius, color);
    }

    /**
     * One-pixel rounded outline. Each corner row is filled from its arc position to the
     * previous row's position, so consecutive rows always meet and there are no gaps.
     */
    private static void drawRoundedOutline(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                           int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.min(radius, Math.min(width, height) / 2);
        if (r <= 0) {
            graphics.fill(x, y, x + width, y + 1, color);
            graphics.fill(x, y + height - 1, x + width, y + height, color);
            graphics.fill(x, y, x + 1, y + height, color);
            graphics.fill(x + width - 1, y, x + width, y + height, color);
            return;
        }

        graphics.fill(x + r, y, x + width - r, y + 1, color);
        graphics.fill(x + r, y + height - 1, x + width - r, y + height, color);
        graphics.fill(x, y + r, x + 1, y + height - r, color);
        graphics.fill(x + width - 1, y + r, x + width, y + height - r, color);

        for (int row = 0; row < r; row++) {
            int inset = cornerInset(r, row);
            // At least one pixel wide, so rows where the arc is vertical still reach the straight edge.
            int end = Math.max(row == 0 ? r : cornerInset(r, row - 1), inset + 1);

            graphics.fill(x + inset, y + row, x + end, y + row + 1, color);
            graphics.fill(x + width - end, y + row, x + width - inset, y + row + 1, color);
            graphics.fill(x + inset, y + height - row - 1, x + end, y + height - row, color);
            graphics.fill(x + width - end, y + height - row - 1, x + width - inset, y + height - row, color);
        }
    }

    private static int cornerInset(int radius, int row) {
        double dy = radius - row - 0.5;
        double dx = Math.sqrt(Math.max(0.0, radius * radius - dy * dy));
        return Math.max(0, radius - (int) Math.ceil(dx));
    }

    private static float clamp01(float value) {
        return clamp(value, 0.0f, 1.0f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
