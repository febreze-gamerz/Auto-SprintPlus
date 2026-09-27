package com.febreze.autosprintplus.hud;

public enum HudColor {
    WHITE("White", 0xFFFFFFFF),
    CYAN("Cyan", 0xFF22DDFF),
    BLUE("Blue", 0xFF5599FF),
    GREEN("Green", 0xFF22CC66),
    RED("Red", 0xFFFF5555),
    ORANGE("Orange", 0xFFFFAA22),
    PURPLE("Purple", 0xFFAA66FF),
    GRAY("Gray", 0xFFB0B0B0),
    BLACK("Black", 0xFF111111);

    private final String displayName;
    private final int color;

    HudColor(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public HudColor next() {
        HudColor[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
