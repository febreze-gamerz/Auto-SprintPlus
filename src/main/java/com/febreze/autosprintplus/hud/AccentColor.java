package com.febreze.autosprintplus.hud;

public enum AccentColor {
    CYAN("Cyan", 0xFF22DDFF),
    BLUE("Blue", 0xFF5599FF),
    GREEN("Green", 0xFF22CC66),
    RED("Red", 0xFFFF5555),
    ORANGE("Orange", 0xFFFFAA22),
    PURPLE("Purple", 0xFFAA66FF);

    private final String displayName;
    private final int color;

    AccentColor(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public int getColor() { return color; }
    public AccentColor next() {
        AccentColor[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
