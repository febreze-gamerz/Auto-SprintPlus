package com.febreze.autosprintplus.gui;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;
import com.febreze.autosprintplus.hud.HudRenderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Minimal Minecraft-style HUD editor.
 * The background remains translucent so the world can stay visible behind the editor.
 */
public final class HudEditorScreen extends Screen {
    private static final int PANEL_W = 330;
    private static final int PANEL_H = 255;

    private final Screen parent;
    private boolean draggingHud;
    private double dragOffsetX;
    private double dragOffsetY;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Auto Sprint+ - HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();
        HudRenderer.migrateAbsolutePosition(c, this.width, this.height);

        int panelW = Math.min(PANEL_W, Math.max(280, width - 20));
        int panelH = Math.min(PANEL_H, Math.max(220, height - 20));
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;
        int buttonW = Math.max(120, (panelW - 42) / 2);
        int left = panelX + 14;
        int right = panelX + panelW - 14 - buttonW;

        addRenderableWidget(toggle("Grid", c.gridEnabled, v -> c.gridEnabled = v, left, panelY + 48, buttonW));
        addRenderableWidget(cycleGridButton(c, right, panelY + 48, buttonW));

        addRenderableWidget(toggle("Snap to Grid", c.snapPosition, v -> c.snapPosition = v, left, panelY + 74, buttonW));
        addRenderableWidget(toggle("Center Snap", c.centerSnap, v -> c.centerSnap = v, right, panelY + 74, buttonW));

        addRenderableWidget(toggle("Edge Snap", c.edgeSnap, v -> c.edgeSnap = v, left, panelY + 100, buttonW));
        addRenderableWidget(toggle("Snap Scale", c.snapScale, v -> c.snapScale = v, right, panelY + 100, buttonW));

        addRenderableWidget(Button.builder(Component.literal("Scale -"), b -> changeScale(-0.1f))
                .bounds(left, panelY + 132, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Scale +"), b -> changeScale(0.1f))
                .bounds(right, panelY + 132, buttonW, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Text Scale -"), b -> changeTextScale(-0.1f))
                .bounds(left, panelY + 158, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Text Scale +"), b -> changeTextScale(0.1f))
                .bounds(right, panelY + 158, buttonW, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Reset HUD"), b -> {
            c.resetHud();
            ConfigManager.save();
        }).bounds(left, panelY + 190, buttonW, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(right, panelY + 190, buttonW, 20).build());
    }

    private Button cycleGridButton(ModConfig c, int x, int y, int width) {
        return Button.builder(Component.literal("Grid Size: " + c.gridSize + " px"), b -> {
            int[] sizes = {5, 10, 15, 20, 25, 50};
            int index = 0;
            for (int i = 0; i < sizes.length; i++) {
                if (sizes[i] == c.gridSize) {
                    index = i;
                    break;
                }
            }
            c.gridSize = sizes[(index + 1) % sizes.length];
            b.setMessage(Component.literal("Grid Size: " + c.gridSize + " px"));
            ConfigManager.save();
        }).bounds(x, y, width, 20).build();
    }

    private Button toggle(String name, boolean value, ToggleHandler handler, int x, int y, int width) {
        return Button.builder(toggleText(name, value), b -> {
            boolean next = !b.getMessage().getString().endsWith("ON");
            handler.change(next);
            b.setMessage(toggleText(name, next));
            ConfigManager.save();
        }).bounds(x, y, width, 20).build();
    }

    private Component toggleText(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "ON" : "OFF"));
    }

    private void changeScale(float delta) {
        ModConfig c = ConfigManager.getConfig();
        float next = Math.max(0.5f, Math.min(2.0f, c.hudScale + delta));
        if (c.snapScale) next = Math.round(next * 10.0f) / 10.0f;
        c.hudScale = next;
        ConfigManager.save();
    }

    private void changeTextScale(float delta) {
        ModConfig c = ConfigManager.getConfig();
        float next = Math.max(0.5f, Math.min(2.0f, c.textScale + delta));
        next = Math.round(next * 10.0f) / 10.0f;
        c.textScale = next;
        ConfigManager.save();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();

        // Transparent/translucent editor background: the vanilla screen background/world remains visible.
        graphics.fill(0, 0, width, height, 0x55000000);

        drawGrid(graphics, c);
        drawCenterGuides(graphics, c);

        float hudScale = c.hudScale;
        float hudW = 190.0f * hudScale;
        float hudH = 26.0f * hudScale;
        float hudX = HudRenderer.resolveX(c, width, hudW);
        float hudY = HudRenderer.resolveY(c, height, hudH);

        // Show the real background/stroke settings while editing.
        HudRenderer.renderHudBox(graphics, hudX, hudY, hudScale, false, true);

        int panelW = Math.min(PANEL_W, Math.max(280, width - 20));
        int panelH = Math.min(PANEL_H, Math.max(220, height - 20));
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        // Classic Minecraft options-style panel.
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF02A2A2A);
        graphics.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + panelH - 2, 0xE0181818);
        graphics.outline(panelX, panelY, panelW, panelH, 0xFF808080);

        graphics.centeredText(font, Component.literal("HUD Editor"), width / 2, panelY + 12, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Drag the HUD to move it"), width / 2, panelY + 28, 0xFFB0B0B0);
        graphics.text(font, "HUD: " + String.format("%.1fx", c.hudScale) + "   Text: " + String.format("%.1fx", c.textScale), panelX + 15, panelY + panelH - 20, 0xFFB0B0B0, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawGrid(GuiGraphicsExtractor graphics, ModConfig c) {
        if (!c.gridEnabled) return;
        int step = Math.max(2, c.gridSize);
        int minX = 0;
        int maxX = width;
        int minY = 0;
        int maxY = height;
        for (int x = minX; x < maxX; x += step) {
            graphics.fill(x, minY, x + 1, maxY, 0x20FFFFFF);
        }
        for (int y = minY; y < maxY; y += step) {
            graphics.fill(minX, y, maxX, y + 1, 0x20FFFFFF);
        }
    }

    private void drawCenterGuides(GuiGraphicsExtractor graphics, ModConfig c) {
        if (!c.centerSnap) return;
        graphics.fill(width / 2, 0, width / 2 + 1, height, 0x5533AAFF);
        graphics.fill(0, height / 2, width, height / 2 + 1, 0x5533AAFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            ModConfig c = ConfigManager.getConfig();
            float w = 190.0f * c.hudScale;
            float h = 26.0f * c.hudScale;
            float x = HudRenderer.resolveX(c, width, w);
            float y = HudRenderer.resolveY(c, height, h);

            if (event.x() >= x && event.x() <= x + w && event.y() >= y && event.y() <= y + h) {
                draggingHud = true;
                dragOffsetX = event.x() - x;
                dragOffsetY = event.y() - y;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingHud && event.button() == 0) {
            ModConfig c = ConfigManager.getConfig();
            float w = 190.0f * c.hudScale;
            float h = 26.0f * c.hudScale;
            float x = (float) (event.x() - dragOffsetX);
            float y = (float) (event.y() - dragOffsetY);

            // Snap-to-grid works independently from whether the grid is currently visible.
            if (c.snapPosition) {
                int g = Math.max(1, c.gridSize);
                x = Math.round(x / g) * g;
                y = Math.round(y / g) * g;
            }

            if (c.centerSnap) {
                if (Math.abs((x + w / 2f) - width / 2f) <= 8) x = width / 2f - w / 2f;
                if (Math.abs((y + h / 2f) - height / 2f) <= 8) y = height / 2f - h / 2f;
            }

            if (c.edgeSnap) {
                if (Math.abs(x) <= 8) x = 0;
                if (Math.abs(y) <= 8) y = 0;
                if (Math.abs((x + w) - width) <= 8) x = width - w;
                if (Math.abs((y + h) - height) <= 8) y = height - h;
            }

            // Allow true edge/corner placement: there is NO editor border margin.
            x = Math.max(0, Math.min(width - w, x));
            y = Math.max(0, Math.min(height - h, y));

            HudRenderer.setPositionFromPixels(c, x, y, width, height);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            boolean wasDragging = draggingHud;
            draggingHud = false;
            if (wasDragging) {
                ConfigManager.save();
                return true;
            }
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        minecraft.setScreen(parent);
    }

    @FunctionalInterface
    private interface ToggleHandler {
        void change(boolean value);
    }
}
