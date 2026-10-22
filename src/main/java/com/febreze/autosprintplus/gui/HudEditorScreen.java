package com.febreze.autosprintplus.gui;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;
import com.febreze.autosprintplus.hud.HudRenderer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * HUD editor. Drag the HUD box to move it; the panel controls grid, snapping and scale.
 *
 * The drag widget is the same mechanism the first working version used. It records the
 * grab offset (mouse minus HUD corner) when the press arrives, and places the HUD from
 * absolute mouse positions while dragging, so the grabbed point stays under the cursor.
 * If the press did not reach the widget, the offset is taken from the first drag event,
 * so the HUD never jumps to its corner.
 */
public final class HudEditorScreen extends Screen {
    private static final int PANEL_W = 330;
    private static final int PANEL_H = 255;
    private static final int[] GRID_SIZES = {5, 10, 15, 20, 25, 50};

    private final Screen parent;
    private HudDragWidget hudDragWidget;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Auto Sprint+ - HUD Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();
        HudRenderer.migrateAbsolutePosition(c, this.width, this.height);

        int[] p = panelRect();
        int panelX = p[0], panelY = p[1], panelW = p[2];
        int buttonW = Math.max(120, (panelW - 42) / 2);
        int left = panelX + 14;
        int right = panelX + panelW - 14 - buttonW;

        addRenderableWidget(toggle("Grid",
                () -> ConfigManager.getConfig().gridEnabled,
                v -> ConfigManager.getConfig().gridEnabled = v,
                left, panelY + 48, buttonW));
        addRenderableWidget(Button.builder(gridSizeText(), b -> {
                    ModConfig cfg = ConfigManager.getConfig();
                    int index = 0;
                    for (int i = 0; i < GRID_SIZES.length; i++) {
                        if (GRID_SIZES[i] == cfg.gridSize) {
                            index = i;
                            break;
                        }
                    }
                    cfg.gridSize = GRID_SIZES[(index + 1) % GRID_SIZES.length];
                    b.setMessage(gridSizeText());
                    ConfigManager.save();
                }).bounds(right, panelY + 48, buttonW, 20).build());

        addRenderableWidget(toggle("Snap to Grid",
                () -> ConfigManager.getConfig().snapPosition,
                v -> ConfigManager.getConfig().snapPosition = v,
                left, panelY + 74, buttonW));
        addRenderableWidget(toggle("Center Snap",
                () -> ConfigManager.getConfig().centerSnap,
                v -> ConfigManager.getConfig().centerSnap = v,
                right, panelY + 74, buttonW));

        addRenderableWidget(toggle("Edge Snap",
                () -> ConfigManager.getConfig().edgeSnap,
                v -> ConfigManager.getConfig().edgeSnap = v,
                left, panelY + 100, buttonW));
        addRenderableWidget(toggle("Snap Scale",
                () -> ConfigManager.getConfig().snapScale,
                v -> ConfigManager.getConfig().snapScale = v,
                right, panelY + 100, buttonW));

        addRenderableWidget(Button.builder(Component.literal("HUD Scale -"), b -> changeScale(-0.1f))
                .bounds(left, panelY + 132, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("HUD Scale +"), b -> changeScale(0.1f))
                .bounds(right, panelY + 132, buttonW, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Text Scale -"), b -> changeTextScale(-0.1f))
                .bounds(left, panelY + 158, buttonW, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Text Scale +"), b -> changeTextScale(0.1f))
                .bounds(right, panelY + 158, buttonW, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Reset HUD"), b -> {
            ConfigManager.getConfig().resetHud();
            ConfigManager.save();
            updateHudDragWidgetBounds();
        }).bounds(left, panelY + 190, buttonW, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(right, panelY + 190, buttonW, 20).build());

        // Added last, so the panel buttons get the first chance at every click.
        hudDragWidget = new HudDragWidget();
        addRenderableWidget(hudDragWidget);
        updateHudDragWidgetBounds();
    }

    private Button toggle(String name, Supplier<Boolean> getter, Consumer<Boolean> setter,
                          int x, int y, int w) {
        return Button.builder(toggleText(name, getter.get()), b -> {
            boolean next = !getter.get();
            setter.accept(next);
            b.setMessage(toggleText(name, next));
            ConfigManager.save();
        }).bounds(x, y, w, 20).build();
    }

    private static Component toggleText(String name, boolean value) {
        return Component.literal(name + ": " + (value ? "ON" : "OFF"));
    }

    private static Component gridSizeText() {
        return Component.literal("Grid Size: " + ConfigManager.getConfig().gridSize + " px");
    }

    private void changeScale(float delta) {
        ModConfig c = ConfigManager.getConfig();
        float next = Math.max(0.5f, Math.min(2.0f, c.hudScale + delta));
        if (c.snapScale) next = Math.round(next * 10.0f) / 10.0f;
        c.hudScale = next;
        ConfigManager.save();
        updateHudDragWidgetBounds();
    }

    private void changeTextScale(float delta) {
        ModConfig c = ConfigManager.getConfig();
        float next = Math.max(0.5f, Math.min(2.0f, c.textScale + delta));
        c.textScale = Math.round(next * 10.0f) / 10.0f;
        ConfigManager.save();
        updateHudDragWidgetBounds();
    }

    /** Panel rectangle as {x, y, width, height}. */
    private int[] panelRect() {
        int panelW = Math.min(PANEL_W, Math.max(280, width - 20));
        int panelH = Math.min(PANEL_H, Math.max(220, height - 20));
        return new int[]{(width - panelW) / 2, (height - panelH) / 2, panelW, panelH};
    }

    private void updateHudDragWidgetBounds() {
        if (hudDragWidget == null) return;

        ModConfig c = ConfigManager.getConfig();
        float[] sz = HudRenderer.size(c);
        float x = HudRenderer.resolveX(c, width, sz[0]);
        float y = HudRenderer.resolveY(c, height, sz[1]);
        hudDragWidget.setRectangle(Math.round(sz[0]), Math.round(sz[1]), Math.round(x), Math.round(y));
    }

    /** Moves the HUD so its top-left corner is at (x, y), applying snapping and keeping it on screen. */
    private void moveHudTo(float x, float y) {
        ModConfig c = ConfigManager.getConfig();
        float[] sz = HudRenderer.size(c);
        float w = sz[0];
        float h = sz[1];

        if (c.snapPosition) {
            int g = Math.max(1, c.gridSize);
            x = Math.round(x / (float) g) * g;
            y = Math.round(y / (float) g) * g;
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

        HudRenderer.setPositionFromPixels(c, x, y, width, height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();

        // Translucent backdrop: the world stays visible behind the editor.
        graphics.fill(0, 0, width, height, 0x55000000);

        drawGrid(graphics, c);
        drawCenterGuides(graphics, c);

        float[] sz = HudRenderer.size(c);
        float hudX = HudRenderer.resolveX(c, width, sz[0]);
        float hudY = HudRenderer.resolveY(c, height, sz[1]);

        // Draw the real background and border while editing.
        HudRenderer.renderHudBox(graphics, hudX, hudY, c, false, true);

        int[] p = panelRect();
        int panelX = p[0], panelY = p[1], panelW = p[2], panelH = p[3];

        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF02A2A2A);
        graphics.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + panelH - 2, 0xE0181818);
        graphics.outline(panelX, panelY, panelW, panelH, 0xFF808080);

        updateHudDragWidgetBounds();

        graphics.centeredText(font, Component.literal("HUD Editor"), width / 2, panelY + 12, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Drag the HUD box to move it"), width / 2, panelY + 28, 0xFFB0B0B0);
        graphics.text(font, "HUD: " + String.format("%.1fx", c.hudScale) + "   Text: " + String.format("%.1fx", c.textScale),
                panelX + 15, panelY + panelH - 20, 0xFFB0B0B0, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void drawGrid(GuiGraphicsExtractor graphics, ModConfig c) {
        if (!c.gridEnabled) return;
        int step = Math.max(2, c.gridSize);
        for (int x = 0; x < width; x += step) {
            graphics.fill(x, 0, x + 1, height, 0x20FFFFFF);
        }
        for (int y = 0; y < height; y += step) {
            graphics.fill(0, y, width, y + 1, 0x20FFFFFF);
        }
    }

    private void drawCenterGuides(GuiGraphicsExtractor graphics, ModConfig c) {
        if (!c.centerSnap) return;
        graphics.fill(width / 2, 0, width / 2 + 1, height, 0x5533AAFF);
        graphics.fill(0, height / 2, width, height / 2 + 1, 0x5533AAFF);
    }

    private final class HudDragWidget extends AbstractWidget {
        /** Grab offset: mouse position minus the HUD's top-left corner, for the current press. */
        private double offsetX;
        private double offsetY;
        /** False until the offset has been measured for this press. */
        private boolean offsetSet;

        private HudDragWidget() {
            super(0, 0, 1, 1, Component.literal("Move HUD"));
            this.active = true;
            this.visible = true;
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() != 0) return;

            offsetX = event.x() - getX();
            offsetY = event.y() - getY();
            offsetSet = true;
        }

        @Override
        protected void onDrag(MouseButtonEvent event, double dx, double dy) {
            if (!offsetSet) {
                // The press did not reach onClick(); measure the grab offset from this drag event.
                offsetX = event.x() - getX();
                offsetY = event.y() - getY();
                offsetSet = true;
            }
            moveHudTo((float) (event.x() - offsetX), (float) (event.y() - offsetY));
            updateHudDragWidgetBounds();
        }

        @Override
        public void onRelease(MouseButtonEvent event) {
            offsetSet = false;
            ConfigManager.save();
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            // Invisible: the HUD box itself is the visual drag target.
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        minecraft.gui.setScreen(parent);
    }
}
