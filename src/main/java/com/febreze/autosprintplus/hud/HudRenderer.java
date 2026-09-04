package com.febreze.autosprintplus.hud;

import com.febreze.autosprintplus.AutoSprintPlusMod;
import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class HudRenderer {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(AutoSprintPlusMod.MOD_ID, "hud");
    private HudRenderer() {}

    public static void register() {
        HudElementRegistry.addLast(ID, HudRenderer::extract);
    }

    public static void extract(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();
        if (!c.hudEnabled || client.options.hideGui) return;

        int screenW = client.getWindow().getGuiScaledWidth();
        int screenH = client.getWindow().getGuiScaledHeight();
        float scale = clampScale(c.hudScale);
        float width = 190.0f * scale;
        float height = 26.0f * scale;

        float x = resolveX(c, screenW, width);
        float y = resolveY(c, screenH, height);

        c.hudX = x;
        c.hudY = y;

        renderHudBox(graphics, x, y, scale, false, false);
    }

    public static float resolveX(ModConfig c, int screenW, float hudWidth) {
        if (c.hudNormalizedX < 0.0f) {
            float maxX = Math.max(0.0f, screenW - hudWidth);
            c.hudNormalizedX = maxX <= 0.0f ? 0.0f : clamp01(c.hudX / maxX);
        }
        float maxX = Math.max(0.0f, screenW - hudWidth);
        return clamp(c.hudNormalizedX * maxX, 0.0f, maxX);
    }

    public static float resolveY(ModConfig c, int screenH, float hudHeight) {
        if (c.hudNormalizedY < 0.0f) {
            float maxY = Math.max(0.0f, screenH - hudHeight);
            c.hudNormalizedY = maxY <= 0.0f ? 0.0f : clamp01(c.hudY / maxY);
        }
        float maxY = Math.max(0.0f, screenH - hudHeight);
        return clamp(c.hudNormalizedY * maxY, 0.0f, maxY);
    }

    public static void setPositionFromPixels(ModConfig c, float x, float y, int screenW, int screenH) {
        float scale = clampScale(c.hudScale);
        float width = 190.0f * scale;
        float height = 26.0f * scale;
        float maxX = Math.max(0.0f, screenW - width);
        float maxY = Math.max(0.0f, screenH - height);
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

    public static void renderHudBox(GuiGraphicsExtractor graphics, float x, float y, float scale, boolean selected) {
        renderHudBox(graphics, x, y, scale, selected, false);
    }

    public static void renderHudBox(GuiGraphicsExtractor graphics, float x, float y, float scale, boolean selected, boolean forceBackground) {
        Minecraft client = Minecraft.getInstance();
        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();

        float safeScale = clampScale(scale);
        int sx = Math.round(x);
        int sy = Math.round(y);
        int width = Math.round(190 * safeScale);
        int height = Math.round(26 * safeScale);

        int textColor = c.textColor.getColor();
        float textScale = Math.max(0.5f, Math.min(2.0f, c.textScale));
        int bgAlpha = Math.max(0, Math.min(255, Math.round(c.hudOpacity * 255.0f)));
        int bg = (bgAlpha << 24) | (c.backgroundColor.getColor() & 0x00FFFFFF);

        if (c.hudBackground || forceBackground) {
            drawRoundedRect(graphics, sx, sy, width, height, c.roundedCorners, bg);
        }

        if (c.borderEnabled) {
            int borderColor = c.borderColor.getColor();
            int borderWidth = Math.max(1, c.borderWidth);
            for (int i = 0; i < borderWidth; i++) {
                drawRoundedOutline(graphics, sx + i, sy + i, width - i * 2, height - i * 2,
                        Math.max(0, c.roundedCorners - i), selected ? 0xFFFFFFFF : borderColor);
            }
        }

        String sprintText = c.autoSprintEnabled ? "Sprint: Toggled" : "Sprint: Vanilla";
        int textWidth = Math.round(client.font.width(Component.literal(sprintText)) * textScale);
        int lineHeight = Math.round(client.font.lineHeight * textScale);
        int textX = sx + Math.max(4, (width - textWidth) / 2);
        int textY = sy + Math.max(2, (height - lineHeight) / 2);

        graphics.pose().pushMatrix();
        graphics.pose().translate(textX, textY);
        graphics.pose().scale(textScale, textScale);
        graphics.text(client.font, sprintText, 0, 0, textColor, c.textShadow);
        graphics.pose().popMatrix();
    }

    private static void drawRoundedRect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
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

    private static void drawRoundedOutline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
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
            graphics.fill(x + inset, y + row, x + inset + 1, y + row + 1, color);
            graphics.fill(x + width - inset - 1, y + row, x + width - inset, y + row + 1, color);
            graphics.fill(x + inset, y + height - row - 1, x + inset + 1, y + height - row, color);
            graphics.fill(x + width - inset - 1, y + height - row - 1, x + width - inset, y + height - row, color);
        }
    }

    private static int cornerInset(int radius, int row) {
        double dy = radius - row - 0.5;
        double dx = Math.sqrt(Math.max(0.0, radius * radius - dy * dy));
        return Math.max(0, radius - (int) Math.ceil(dx));
    }

    private static float clampScale(float value) {
        return clamp(value, 0.5f, 2.0f);
    }

    private static float clamp01(float value) {
        return clamp(value, 0.0f, 1.0f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
