package com.febreze.autosprintplus.gui;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;
import com.febreze.autosprintplus.hud.HudRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * HUD editor: drag the HUD box to move it, use the panel for grid, snapping and scale.
 * Hit-testing and drawing both use {@link HudRenderer#size(ModConfig)}, so the drag
 * area is exactly the box that is drawn.
 */
public final class HudEditorScreen extends MouseScreen {
    private static final int PANEL_W = 330;
    private static final int PANEL_H = 255;
    private static final int[] GRID_SIZES = {5, 10, 15, 20, 25, 50};

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
        HudRenderer.migrateAbsolutePosition(c, width, height);

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
        }).bounds(left, panelY + 190, buttonW, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(right, panelY + 190, buttonW, 20).build());
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
    }

    private void changeTextScale(float delta) {
        ModConfig c = ConfigManager.getConfig();
        float next = Math.max(0.5f, Math.min(2.0f, c.textScale + delta));
        c.textScale = Math.round(next * 10.0f) / 10.0f;
        ConfigManager.save();
    }

    /** Panel rectangle: {x, y, width, height}. */
    private int[] panelRect() {
        int panelW = Math.min(PANEL_W, Math.max(280, width - 20));
        int panelH = Math.min(PANEL_H, Math.max(220, height - 20));
        return new int[]{(width - panelW) / 2, (height - panelH) / 2, panelW, panelH};
    }

    private boolean insidePanel(double mouseX, double mouseY) {
        int[] p = panelRect();
        return mouseX >= p[0] && mouseX <= p[0] + p[2] && mouseY >= p[1] && mouseY <= p[1] + panelRect()[3];
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();

        graphics.fill(0, 0, width, height, 0x55000000);
        drawGrid(graphics, c);
        drawCenterGuides(graphics, c);

        float[] sz = HudRenderer.size(c);
        float hudX = HudRenderer.resolveX(c, width, sz[0]);
        float hudY = HudRenderer.resolveY(c, height, sz[1]);
        HudRenderer.renderHudBox(graphics, hudX, hudY, c, true, true);

        int[] p = panelRect();
        int panelX = p[0], panelY = p[1], panelW = p[2], panelH = p[3];
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF02A2A2A);
        graphics.fill(panelX + 2, panelY + 2, panelX + panelW - 2, panelY + panelH - 2, 0xE0181818);
        graphics.renderOutline(panelX, panelY, panelW, panelH, 0xFF808080);

        Font font = Minecraft.getInstance().font;
        drawCentered(graphics, font, "HUD Editor", width / 2, panelY + 12, 0xFFFFFFFF);
        drawCentered(graphics, font, "Drag the HUD box to move it", width / 2, panelY + 28, 0xFFB0B0B0);
        graphics.drawString(font,
                "HUD: " + String.format("%.1fx", c.hudScale) + "   Text: " + String.format("%.1fx", c.textScale),
                panelX + 15, panelY + panelH - 20, 0xFFB0B0B0, false);

        super.render(graphics, mouseX, mouseY, delta);
    }

    private static void drawCentered(GuiGraphics graphics, Font font, String text, int centerX, int y, int color) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    private void drawGrid(GuiGraphics graphics, ModConfig c) {
        if (!c.gridEnabled) return;
        int step = Math.max(2, c.gridSize);
        for (int x = 0; x < width; x += step) {
            graphics.fill(x, 0, x + 1, height, 0x20FFFFFF);
        }
        for (int y = 0; y < height; y += step) {
            graphics.fill(0, y, width, y + 1, 0x20FFFFFF);
        }
    }

    private void drawCenterGuides(GuiGraphics graphics, ModConfig c) {
        if (!c.centerSnap) return;
        graphics.fill(width / 2, 0, width / 2 + 1, height, 0x5533AAFF);
        graphics.fill(0, height / 2, width, height / 2 + 1, 0x5533AAFF);
    }

    private boolean insideHud(double mouseX, double mouseY) {
        ModConfig c = ConfigManager.getConfig();
        float[] sz = HudRenderer.size(c);
        float x = HudRenderer.resolveX(c, width, sz[0]);
        float y = HudRenderer.resolveY(c, height, sz[1]);
        return mouseX >= x && mouseX <= x + sz[0] && mouseY >= y && mouseY <= y + sz[1];
    }

    @Override
    protected boolean onPress(double mouseX, double mouseY, int button) {
        // Clicks on the panel go to its buttons, even when the HUD sits underneath it.
        if (button != 0 || insidePanel(mouseX, mouseY) || !insideHud(mouseX, mouseY)) return false;

        ModConfig c = ConfigManager.getConfig();
        float[] sz = HudRenderer.size(c);
        float x = HudRenderer.resolveX(c, width, sz[0]);
        float y = HudRenderer.resolveY(c, height, sz[1]);
        draggingHud = true;
        dragOffsetX = mouseX - x;
        dragOffsetY = mouseY - y;
        return true;
    }

    @Override
    protected boolean onDrag(double mouseX, double mouseY, int button) {
        if (!draggingHud || button != 0) return false;
        moveHudFromMouse(mouseX, mouseY);
        return true;
    }

    @Override
    protected boolean onRelease(double mouseX, double mouseY, int button) {
        if (button != 0 || !draggingHud) return false;
        draggingHud = false;
        ConfigManager.save();
        return true;
    }

    private void moveHudFromMouse(double mouseX, double mouseY) {
        ModConfig c = ConfigManager.getConfig();
        float[] sz = HudRenderer.size(c);
        float w = sz[0];
        float h = sz[1];
        float x = (float) (mouseX - dragOffsetX);
        float y = (float) (mouseY - dragOffsetY);

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
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Intentionally empty; the background is drawn in render().
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        Minecraft.getInstance().setScreen(parent);
    }
}
