package com.febreze.autosprintplus.gui;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;
import com.febreze.autosprintplus.hud.HudColor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Auto Sprint+ main configuration screen.
 *
 * The layout intentionally follows the vanilla Minecraft Options style:
 * translucent world backdrop, centered title, two-column option buttons,
 * vanilla button widgets, and a simple scrollable content area.
 */
public final class ConfigScreen extends Screen {
    private static final int CONTENT_TOP = 58;
    private static final int CONTENT_BOTTOM = 42;
    private static final int ROW_H = 24;
    private static final int ROW_GAP = 26;
    private static final int COLUMN_GAP = 8;
    private static final int CONTENT_W = 520;
    private static final int SCROLLBAR_W = 6;

    private final Screen parent;
    private final List<ButtonEntry> entries = new ArrayList<>();

    private double scrollOffset;
    private int contentHeight;
    private boolean draggingScrollbar;
    private double scrollbarGrabOffset;
    private ButtonWidget doneButton;

    private int roundedOffset = -1;
    private int opacityOffset = -1;
    private boolean draggingRounded;
    private boolean draggingOpacity;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Auto Sprint+"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        entries.clear();
        roundedOffset = -1;
        opacityOffset = -1;
        draggingScrollbar = false;
        draggingRounded = false;
        draggingOpacity = false;

        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();

        int left = contentLeft();
        int colW = (contentWidth() - COLUMN_GAP) / 2;
        int row = 0;

        row = addSection("Auto Sprint", row);
        row = addTogglePair(left, colW, row,
                "Auto Sprint", () -> c.autoSprintEnabled,
                value -> c.autoSprintEnabled = value,
                "Disable While Swimming", () -> c.disableInWater,
                value -> c.disableInWater = value);
        row = addToggleRow(left, colW, row,
                "Disable While Flying", () -> c.disableWhileFlying,
                value -> c.disableWhileFlying = value);

        row += 8;
        row = addSection("HUD", row);
        row = addTogglePair(left, colW, row,
                "HUD Enabled", () -> c.hudEnabled,
                value -> c.hudEnabled = value,
                "Background", () -> c.hudBackground,
                value -> c.hudBackground = value);

        row = addCyclePair(left, colW, row,
                "Background Color", () -> c.backgroundColor.getDisplayName(),
                () -> c.backgroundColor = c.backgroundColor.next(),
                "Border Color", () -> c.borderColor.getDisplayName(),
                () -> c.borderColor = c.borderColor.next());

        row = addTogglePair(left, colW, row,
                "Border", () -> c.borderEnabled,
                value -> c.borderEnabled = value,
                "Text Shadow", () -> c.textShadow,
                value -> c.textShadow = value);

        row = addCyclePair(left, colW, row,
                "Border Width", () -> c.borderWidth + " px",
                () -> c.borderWidth = c.borderWidth >= 4 ? 0 : c.borderWidth + 1,
                "Text Color", () -> c.textColor.getDisplayName(),
                () -> c.textColor = c.textColor.next());

        roundedOffset = row;
        row += 48;
        opacityOffset = row;
        row += 48;

        row = addButtonPair(left, colW, row,
                "Edit HUD", b -> client.setScreen(new HudEditorScreen(this)),
                "Reset HUD", b -> {
                    c.resetHud();
                    ConfigManager.save();
                    updateSliderWidgets();
                });

        row += 8;
        row = addSection("Reset", row);
        addButton(left, contentWidth(), 0, row, "Reset All Settings", b -> {
            ConfigManager.reset();
            scrollOffset = 0;
            this.init();
        });

        contentHeight = row + 28;

        doneButton = ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(left, height - 28, CONTENT_W, 20)
                .build();
        addDrawableChild(doneButton);

        clampScroll();
        updateLayout();
    }

    private int contentWidth() {
        // Keep the vanilla-style two-column layout usable at high GUI scales,
        // where the scaled screen can be much narrower than 520 px.
        return Math.min(CONTENT_W, Math.max(280, width - 32));
    }

    private int contentLeft() {
        return (width - contentWidth()) / 2;
    }

    private int addSection(String title, int cursor) {
        entries.add(ButtonEntry.section(title, cursor));
        return cursor + 30;
    }

    private int addTogglePair(int left, int colW, int row,
                              String leftLabel, Supplier<Boolean> leftGetter, Consumer<Boolean> leftSetter,
                              String rightLabel, Supplier<Boolean> rightGetter, Consumer<Boolean> rightSetter) {
        addToggle(left, colW, 0, row, leftLabel, leftGetter, leftSetter);
        addToggle(left + colW + COLUMN_GAP, colW, 1, row, rightLabel, rightGetter, rightSetter);
        return row + ROW_GAP;
    }

    private int addToggleRow(int left, int colW, int row,
                             String label, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        addToggle(left, colW, 0, row, label, getter, setter);
        return row + ROW_GAP;
    }

    private int addCyclePair(int left, int colW, int row,
                             String leftLabel, Supplier<String> leftGetter, Runnable leftAction,
                             String rightLabel, Supplier<String> rightGetter, Runnable rightAction) {
        addCycle(left, colW, 0, row, leftLabel, leftGetter, leftAction);
        addCycle(left + colW + COLUMN_GAP, colW, 1, row, rightLabel, rightGetter, rightAction);
        return row + ROW_GAP;
    }

    private int addButtonPair(int left, int colW, int row,
                              String leftLabel, ButtonWidget.PressAction leftAction,
                              String rightLabel, ButtonWidget.PressAction rightAction) {
        addButton(left, colW, 0, row, leftLabel, leftAction);
        addButton(left + colW + COLUMN_GAP, colW, 1, row, rightLabel, rightAction);
        return row + ROW_GAP;
    }

    private int addToggle(int x, int w, int column, int cursor, String label,
                          Supplier<Boolean> getter, Consumer<Boolean> setter) {
        ButtonWidget button = ButtonWidget.builder(
                optionText(label, getter.get()),
                b -> {
                    boolean next = !getter.get();
                    setter.accept(next);
                    ConfigManager.save();
                    b.setMessage(optionText(label, next));
                }
        ).dimensions(x, 0, w, ROW_H).build();

        entries.add(ButtonEntry.widget(button, cursor, column));
        addDrawableChild(button);
        return cursor + ROW_GAP;
    }

    private int addCycle(int x, int w, int column, int cursor, String label,
                         Supplier<String> getter, Runnable action) {
        ButtonWidget button = ButtonWidget.builder(
                optionText(label, getter.get()),
                b -> {
                    action.run();
                    ConfigManager.save();
                    b.setMessage(optionText(label, getter.get()));
                }
        ).dimensions(x, 0, w, ROW_H).build();

        entries.add(ButtonEntry.widget(button, cursor, column));
        addDrawableChild(button);
        return cursor + ROW_GAP;
    }

    private int addButton(int x, int w, int column, int cursor, String label, ButtonWidget.PressAction action) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(label), action)
                .dimensions(x, 0, w, ROW_H)
                .build();

        entries.add(ButtonEntry.widget(button, cursor, column));
        addDrawableChild(button);
        return cursor + ROW_GAP;
    }

    private Text optionText(String label, boolean value) {
        return Text.literal(label + ": " + (value ? "ON" : "OFF"));
    }

    private Text optionText(String label, String value) {
        return Text.literal(label + ": " + value);
    }

    private void updateLayout() {
        int left = contentLeft();
        int colW = (contentWidth() - COLUMN_GAP) / 2;
        int visibleTop = CONTENT_TOP;
        int visibleBottom = height - CONTENT_BOTTOM;

        for (ButtonEntry entry : entries) {
            if (entry.button == null) continue;

            int x = entry.column == 1 ? left + colW + COLUMN_GAP : left;
            int w = entry.width == CONTENT_W ? contentWidth() : colW;
            int y = visibleTop + entry.offset - (int) scrollOffset;
            boolean visible = y + ROW_H >= visibleTop && y <= visibleBottom;

            entry.button.setX(x);
            entry.button.setY(y);
            entry.button.setWidth(w);
            entry.button.visible = visible;
            entry.button.active = visible;
        }

        if (doneButton != null) {
            doneButton.setX(left);
            doneButton.setY(height - 28);
            doneButton.setWidth(contentWidth());
        }
    }

    private void updateSliderWidgets() {
        // Slider values are drawn live from the current config, so there is
        // no widget state to synchronize here. Kept as a separate method so
        // reset operations have one clean update hook.
        updateLayout();
    }

    private void clampScroll() {
        int visibleHeight = Math.max(1, height - CONTENT_TOP - CONTENT_BOTTOM);
        double max = Math.max(0.0, contentHeight - visibleHeight);
        scrollOffset = Math.max(0.0, Math.min(max, scrollOffset));
    }

    private int scrollbarTrackHeight() {
        return Math.max(1, height - CONTENT_TOP - CONTENT_BOTTOM);
    }

    private int scrollbarThumbHeight(int trackH) {
        return Math.max(28, (int) ((trackH * (double) trackH) / Math.max(trackH, contentHeight)));
    }

    private int scrollbarThumbY(int trackH, int thumbH) {
        int usable = Math.max(1, trackH - thumbH);
        int maxScroll = Math.max(1, contentHeight - trackH);
        return CONTENT_TOP + (int) (usable * (scrollOffset / maxScroll));
    }

    private boolean insideContent(double mouseX, double mouseY) {
        int left = contentLeft();
        return mouseX >= left && mouseX <= left + contentWidth()
                && mouseY >= CONTENT_TOP && mouseY <= height - CONTENT_BOTTOM;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (insideContent(mouseX, mouseY)) {
            scrollOffset -= scrollY * 24.0;
            clampScroll();
            updateLayout();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int trackH = scrollbarTrackHeight();
            int trackX = contentLeft() + contentWidth() + 8;
            if (contentHeight > trackH && mouseX >= trackX - 5 && mouseX <= trackX + SCROLLBAR_W + 5
                    && mouseY >= CONTENT_TOP && mouseY <= CONTENT_TOP + trackH) {
                int thumbH = scrollbarThumbHeight(trackH);
                int thumbY = scrollbarThumbY(trackH, thumbH);
                if (mouseY >= thumbY && mouseY <= thumbY + thumbH) {
                    draggingScrollbar = true;
                    scrollbarGrabOffset = mouseY - thumbY;
                    return true;
                }
            }

            double rounded = sliderHit(mouseX, mouseY, roundedOffset);
            if (rounded >= 0.0) {
                setRoundedFromSlider(rounded);
                draggingRounded = true;
                return true;
            }

            double opacity = sliderHit(mouseX, mouseY, opacityOffset);
            if (opacity >= 0.0) {
                setOpacityFromSlider(opacity);
                draggingOpacity = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            int trackH = scrollbarTrackHeight();
            int thumbH = scrollbarThumbHeight(trackH);
            double usable = Math.max(1.0, trackH - thumbH);
            double top = Math.max(0.0, Math.min(usable,
                    mouseY - CONTENT_TOP - scrollbarGrabOffset));
            double max = Math.max(0.0, contentHeight - trackH);
            scrollOffset = max * (top / usable);
            updateLayout();
            return true;
        }

        if (draggingRounded) {
            double value = sliderHit(mouseX, mouseY, roundedOffset);
            if (value >= 0.0) setRoundedFromSlider(value);
            return true;
        }

        if (draggingOpacity) {
            double value = sliderHit(mouseX, mouseY, opacityOffset);
            if (value >= 0.0) setOpacityFromSlider(value);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean wasDragging = draggingScrollbar || draggingRounded || draggingOpacity;
            draggingScrollbar = false;
            draggingRounded = false;
            draggingOpacity = false;
            if (wasDragging) {
                ConfigManager.save();
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private double sliderHit(double mouseX, double mouseY, int contentOffset) {
        if (contentOffset < 0) return -1.0;
        int x = contentLeft() + 20;
        int w = contentWidth() - 40;
        int y = CONTENT_TOP + contentOffset + 17 - (int) scrollOffset;
        if (mouseX < x - 6 || mouseX > x + w + 6 || mouseY < y - 9 || mouseY > y + 11) return -1.0;
        return Math.max(0.0, Math.min(1.0, (mouseX - x) / (double) w));
    }

    private void setRoundedFromSlider(double value) {
        ModConfig c = ConfigManager.getConfig();
        c.roundedCorners = (int) Math.round(value * 12.0);
        ConfigManager.save();
    }

    private void setOpacityFromSlider(double value) {
        ModConfig c = ConfigManager.getConfig();
        c.hudOpacity = (float) (0.10 + value * 0.90);
        ConfigManager.save();
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        // Render vanilla background exactly once. Screen.render() also calls
        // renderBackground(), so renderBackground() is disabled below to
        // prevent it from being drawn over our crisp GUI text and controls.
        renderBackground(graphics, mouseX, mouseY, delta);

        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();

        // Vanilla-style transparent backdrop: keep the world/menu behind it visible.
        graphics.fill(0, 0, width, height, 0x55000000);

        drawCenteredCrisp(graphics, title, width / 2, 15, 0xFFFFFFFF);
        drawCenteredCrisp(graphics, Text.literal("Auto Sprint+ Settings"), width / 2, 29, 0xFFAAAAAA);

        int left = contentLeft();
        int trackH = scrollbarTrackHeight();

        // Section headings and separators, in the visual style of Minecraft Options.
        for (ButtonEntry entry : entries) {
            if (!entry.section) continue;
            int y = CONTENT_TOP + entry.offset - (int) scrollOffset;
            if (y >= CONTENT_TOP - 12 && y <= height - CONTENT_BOTTOM) {
                drawCenteredCrisp(graphics, Text.literal(entry.sectionTitle), width / 2, y, 0xFFBFBFBF);
                graphics.fill(left, y + 16, left + contentWidth(), y + 17, 0x55555555);
            }
        }

        // Live slider values: no reopening required after dragging.
        int roundedY = CONTENT_TOP + roundedOffset - (int) scrollOffset;
        if (roundedY + 40 >= CONTENT_TOP && roundedY <= height - CONTENT_BOTTOM) {
            drawSlider(graphics, left + 20, roundedY + 17, CONTENT_W - 40,
                    c.roundedCorners / 12.0, "Rounded Corners", c.roundedCorners + " px");
        }

        int opacityY = CONTENT_TOP + opacityOffset - (int) scrollOffset;
        if (opacityY + 40 >= CONTENT_TOP && opacityY <= height - CONTENT_BOTTOM) {
            double normalized = (c.hudOpacity - 0.10) / 0.90;
            drawSlider(graphics, left + 20, opacityY + 17, CONTENT_W - 40,
                    Math.max(0.0, Math.min(1.0, normalized)), "Opacity",
                    Math.round(c.hudOpacity * 100) + "%");
        }

        // Vanilla-style scrollbar.
        if (contentHeight > trackH) {
            int x = left + contentWidth() + 8;
            graphics.fill(x, CONTENT_TOP, x + SCROLLBAR_W, CONTENT_TOP + trackH, 0xFF202020);
            int thumbH = scrollbarThumbHeight(trackH);
            int thumbY = scrollbarThumbY(trackH, thumbH);
            graphics.fill(x, thumbY, x + SCROLLBAR_W, thumbY + thumbH, 0xFFC0C0C0);
        }

        // Footer separator; the footer itself remains transparent.
        graphics.fill(left, height - CONTENT_BOTTOM, left + contentWidth(), height - CONTENT_BOTTOM + 1, 0x55555555);

        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawCenteredCrisp(DrawContext graphics, Text text, int centerX, int y, int color) {
        int x = centerX - textRenderer.getWidth(text) / 2;
        graphics.drawText(textRenderer, text, x, y, color, false);
    }

    private void drawSlider(DrawContext graphics, int x, int y, int w,
                            double value, String label, String valueText) {
        graphics.drawText(textRenderer, label, x, y - 16, 0xFFFFFFFF, false);
        int lineY = y + 2;
        graphics.fill(x, lineY, x + w, lineY + 3, 0xFF555555);
        graphics.fill(x, lineY, x + (int) (w * value), lineY + 3, 0xFFAAAAAA);
        int knobX = x + (int) (w * value);
        graphics.fill(knobX - 2, lineY - 3, knobX + 3, lineY + 8, 0xFFFFFFFF);
        int valueWidth = textRenderer.getWidth(valueText);
        graphics.drawText(textRenderer, valueText, x + w - valueWidth, y - 16, 0xFFBFBFBF, false);
    }

    /**
     * Background is rendered explicitly at the start of render().
     * Keeping this empty prevents Screen.render() from drawing the blurred
     * background a second time over our custom GUI.
     */
    @Override
    public void renderBackground(DrawContext graphics, int mouseX, int mouseY, float delta) {
        // Intentionally empty.
    }

    @Override
    public void close() {
        ConfigManager.save();
        client.setScreen(parent);
    }

    private static final class ButtonEntry {
        final ButtonWidget button;
        final int offset;
        final int column;
        final int width;
        final boolean section;
        final String sectionTitle;

        private ButtonEntry(ButtonWidget button, int offset, int column, int width,
                            boolean section, String sectionTitle) {
            this.button = button;
            this.offset = offset;
            this.column = column;
            this.width = width;
            this.section = section;
            this.sectionTitle = sectionTitle;
        }

        static ButtonEntry widget(ButtonWidget button, int offset, int column) {
            return new ButtonEntry(button, offset, column, -1, false, "");
        }

        static ButtonEntry section(String title, int offset) {
            return new ButtonEntry(null, offset, 0, 0, true, title);
        }
    }
}
