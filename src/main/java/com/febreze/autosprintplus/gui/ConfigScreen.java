package com.febreze.autosprintplus.gui;

import com.febreze.autosprintplus.config.ConfigManager;
import com.febreze.autosprintplus.config.ModConfig;
import com.febreze.autosprintplus.hud.HudColor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

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
    private static final int SCROLLBAR_W = 4;

    private final Screen parent;
    private final List<ButtonEntry> entries = new ArrayList<>();

    private double scrollOffset;
    private int contentHeight;
    private boolean draggingScrollbar;
    private double scrollbarGrabOffset;
    private Button doneButton;

    private int roundedOffset = -1;
    private int opacityOffset = -1;
    private ConfigSlider roundedSlider;
    private ConfigSlider opacitySlider;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Auto Sprint+"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        entries.clear();
        roundedOffset = -1;
        opacityOffset = -1;
        roundedSlider = null;
        opacitySlider = null;
        draggingScrollbar = false;

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
        roundedSlider = addSlider(left, row, "Rounded Corners",
                () -> ConfigManager.getConfig().roundedCorners / 12.0,
                value -> {
                    ModConfig config = ConfigManager.getConfig();
                    config.roundedCorners = (int) Math.round(value * 12.0);
                },
                value -> Math.round(value * 12.0) + " px");
        row += 48;

        opacityOffset = row;
        opacitySlider = addSlider(left, row, "Opacity",
                () -> {
                    float opacity = ConfigManager.getConfig().hudOpacity;
                    return Math.max(0.0, Math.min(1.0, (opacity - 0.10) / 0.90));
                },
                value -> ConfigManager.getConfig().hudOpacity = (float) (0.10 + value * 0.90),
                value -> Math.round((0.10 + value * 0.90) * 100.0) + "%");
        row += 48;

        row = addButtonPair(left, colW, row,
                "Edit HUD", b -> minecraft.gui.setScreen(new HudEditorScreen(this)),
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

        doneButton = Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(left, height - 28, CONTENT_W, 20)
                .build();
        addRenderableWidget(doneButton);

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
                              String leftLabel, Button.OnPress leftAction,
                              String rightLabel, Button.OnPress rightAction) {
        addButton(left, colW, 0, row, leftLabel, leftAction);
        addButton(left + colW + COLUMN_GAP, colW, 1, row, rightLabel, rightAction);
        return row + ROW_GAP;
    }

    private int addToggle(int x, int w, int column, int cursor, String label,
                          Supplier<Boolean> getter, Consumer<Boolean> setter) {
        Button button = Button.builder(
                optionText(label, getter.get()),
                b -> {
                    boolean next = !getter.get();
                    setter.accept(next);
                    ConfigManager.save();
                    b.setMessage(optionText(label, next));
                }
        ).bounds(x, 0, w, ROW_H).build();

        entries.add(ButtonEntry.widget(button, cursor, column));
        addRenderableWidget(button);
        return cursor + ROW_GAP;
    }

    private int addCycle(int x, int w, int column, int cursor, String label,
                         Supplier<String> getter, Runnable action) {
        Button button = Button.builder(
                optionText(label, getter.get()),
                b -> {
                    action.run();
                    ConfigManager.save();
                    b.setMessage(optionText(label, getter.get()));
                }
        ).bounds(x, 0, w, ROW_H).build();

        entries.add(ButtonEntry.widget(button, cursor, column));
        addRenderableWidget(button);
        return cursor + ROW_GAP;
    }

    private int addButton(int x, int w, int column, int cursor, String label, Button.OnPress action) {
        Button button = Button.builder(Component.literal(label), action)
                .bounds(x, 0, w, ROW_H)
                .build();

        entries.add(ButtonEntry.widget(button, cursor, column));
        addRenderableWidget(button);
        return cursor + ROW_GAP;
    }

    private ConfigSlider addSlider(int x, int cursor, String label,
                                    Supplier<Double> getter, Consumer<Double> setter,
                                    java.util.function.Function<Double, String> valueFormatter) {
        ConfigSlider slider = new ConfigSlider(
                x + 20, 0, Math.max(80, contentWidth() - 40), 20,
                label, getter.get(), setter, valueFormatter);
        entries.add(ButtonEntry.widget(slider, cursor, 0, contentWidth()));
        addRenderableWidget(slider);
        return slider;
    }

    private Component optionText(String label, boolean value) {
        return Component.literal(label + ": " + (value ? "ON" : "OFF"));
    }

    private Component optionText(String label, String value) {
        return Component.literal(label + ": " + value);
    }

    private void updateLayout() {
        int left = contentLeft();
        int colW = (contentWidth() - COLUMN_GAP) / 2;
        int visibleTop = CONTENT_TOP;
        int visibleBottom = height - CONTENT_BOTTOM;

        for (ButtonEntry entry : entries) {
            if (entry.widget == null) continue;

            int x = entry.column == 1 ? left + colW + COLUMN_GAP : left;
            int w = entry.width == CONTENT_W ? contentWidth() : (entry.width > 0 ? entry.width : colW);
            if (entry.widget instanceof ConfigSlider) {
                x = left + 20;
                w = Math.max(80, contentWidth() - 40);
            }
            int y = visibleTop + entry.offset - (int) scrollOffset;
            boolean visible = y + ROW_H >= visibleTop && y <= visibleBottom;

            entry.widget.setX(x);
            entry.widget.setY(y);
            entry.widget.setWidth(w);
            entry.widget.visible = visible;
            entry.widget.active = visible;
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
        if (roundedSlider != null) roundedSlider.refreshFromConfig();
        if (opacitySlider != null) opacitySlider.refreshFromConfig();
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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            int trackH = scrollbarTrackHeight();
            int trackX = contentLeft() + contentWidth() + 8;
            if (contentHeight > trackH && event.x() >= trackX - 5 && event.x() <= trackX + SCROLLBAR_W + 5
                    && event.y() >= CONTENT_TOP && event.y() <= CONTENT_TOP + trackH) {
                int thumbH = scrollbarThumbHeight(trackH);
                int thumbY = scrollbarThumbY(trackH, thumbH);
                if (event.y() >= thumbY && event.y() <= thumbY + thumbH) {
                    draggingScrollbar = true;
                    scrollbarGrabOffset = event.y() - thumbY;
                    return true;
                }
            }

        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollbar) {
            int trackH = scrollbarTrackHeight();
            int thumbH = scrollbarThumbHeight(trackH);
            double usable = Math.max(1.0, trackH - thumbH);
            double top = Math.max(0.0, Math.min(usable,
                    event.y() - CONTENT_TOP - scrollbarGrabOffset));
            double max = Math.max(0.0, contentHeight - trackH);
            scrollOffset = max * (top / usable);
            updateLayout();
            return true;
        }


        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            boolean wasDragging = draggingScrollbar;
            draggingScrollbar = false;
            if (wasDragging) {
                ConfigManager.save();
                return true;
            }
        }
        return super.mouseReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        ModConfig c = ConfigManager.getConfig();
        c.ensureValid();

        // Vanilla-style transparent backdrop: keep the world/menu behind it visible.
        graphics.fill(0, 0, width, height, 0x55000000);

        graphics.centeredText(font, title, width / 2, 15, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Auto Sprint+ Settings"), width / 2, 29, 0xFFAAAAAA);

        int left = contentLeft();
        int trackH = scrollbarTrackHeight();

        // Section headings and separators, in the visual style of Minecraft Options.
        for (ButtonEntry entry : entries) {
            if (!entry.section) continue;
            int y = CONTENT_TOP + entry.offset - (int) scrollOffset;
            if (y >= CONTENT_TOP - 12 && y <= height - CONTENT_BOTTOM) {
                graphics.centeredText(font, Component.literal(entry.sectionTitle), width / 2, y, 0xFFBFBFBF);
                graphics.fill(left, y + 16, left + contentWidth(), y + 17, 0x55555555);
            }
        }

        // Vanilla-style scrollbar.
        if (contentHeight > trackH) {
            int x = left + contentWidth() + 8;
            graphics.fill(x, CONTENT_TOP, x + SCROLLBAR_W, CONTENT_TOP + trackH, 0x55333333);
            int thumbH = scrollbarThumbHeight(trackH);
            int thumbY = scrollbarThumbY(trackH, thumbH);
            graphics.fill(x - 1, thumbY, x + SCROLLBAR_W + 1, thumbY + thumbH, 0xFFAAAAAA);
        }

        // Footer separator; the footer itself remains transparent.
        graphics.fill(left, height - CONTENT_BOTTOM, left + contentWidth(), height - CONTENT_BOTTOM + 1, 0x55555555);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        ConfigManager.save();
        minecraft.gui.setScreen(parent);
    }

    private static final class ButtonEntry {
        final AbstractWidget widget;
        final int offset;
        final int column;
        final int width;
        final boolean section;
        final String sectionTitle;

        private ButtonEntry(AbstractWidget widget, int offset, int column, int width,
                            boolean section, String sectionTitle) {
            this.widget = widget;
            this.offset = offset;
            this.column = column;
            this.width = width;
            this.section = section;
            this.sectionTitle = sectionTitle;
        }

        static ButtonEntry widget(AbstractWidget widget, int offset, int column) {
            return new ButtonEntry(widget, offset, column, -1, false, "");
        }

        static ButtonEntry widget(AbstractWidget widget, int offset, int column, int width) {
            return new ButtonEntry(widget, offset, column, width, false, "");
        }

        static ButtonEntry section(String title, int offset) {
            return new ButtonEntry(null, offset, 0, 0, true, title);
        }
    }

    /** Vanilla Minecraft slider widget with native slider rendering and drag handling. */
    private final class ConfigSlider extends AbstractSliderButton {
        private final String label;
        private final Consumer<Double> setter;
        private final java.util.function.Function<Double, String> valueFormatter;

        private ConfigSlider(int x, int y, int width, int height, String label, double initialValue,
                             Consumer<Double> setter,
                             java.util.function.Function<Double, String> valueFormatter) {
            super(x, y, width, height, Component.literal(label), initialValue);
            this.label = label;
            this.setter = setter;
            this.valueFormatter = valueFormatter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + valueFormatter.apply(value)));
        }

        @Override
        protected void applyValue() {
            setter.accept(value);
            ConfigManager.save();
            updateMessage();
        }

        private void refreshFromConfig() {
            value = Math.max(0.0, Math.min(1.0,
                    label.equals("Rounded Corners")
                            ? ConfigManager.getConfig().roundedCorners / 12.0
                            : (ConfigManager.getConfig().hudOpacity - 0.10) / 0.90));
            updateMessage();
        }
    }
}
