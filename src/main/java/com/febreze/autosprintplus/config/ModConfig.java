package com.febreze.autosprintplus.config;

import com.febreze.autosprintplus.hud.HudColor;

public class ModConfig {
    public boolean autoSprintEnabled = true;

    // Sprint is intentionally simple: ON behaves as toggled auto sprint;
    // OFF returns the player to normal Minecraft/vanilla behavior.
    // Legacy sprintMode is no longer used and may safely remain in old JSON files.

    public boolean disableInWater = false;
    public boolean disableWhileFlying = false;

    // HUD
    public boolean hudEnabled = true;
    public boolean hudBackground = true;
    public HudColor backgroundColor = HudColor.BLACK;
    public float hudOpacity = 0.85f;

    public boolean borderEnabled = true;
    public HudColor borderColor = HudColor.CYAN;
    public int borderWidth = 1;

    public HudColor textColor = HudColor.CYAN;
    public boolean textShadow = true;
    public int roundedCorners = 6;

    // Stored as normalized screen coordinates so GUI Scale changes do not move the HUD unexpectedly.
    public float hudX = 10.0f;
    public float hudY = 10.0f;
    public float hudNormalizedX = -1.0f;
    public float hudNormalizedY = -1.0f;
    public float hudScale = 1.0f;
    public float textScale = 1.0f;

    // HUD editor
    public boolean gridEnabled = true;
    public int gridSize = 10;
    public boolean snapPosition = true;
    public boolean snapScale = true;
    public boolean centerSnap = true;
    public boolean edgeSnap = true;

    public void ensureValid() {
        if (backgroundColor == null) backgroundColor = HudColor.BLACK;
        if (borderColor == null) borderColor = HudColor.CYAN;
        if (textColor == null) textColor = HudColor.CYAN;

        if (gridSize < 2 || gridSize > 100) gridSize = 10;
        if (hudScale < 0.5f || hudScale > 2.0f || Float.isNaN(hudScale)) hudScale = 1.0f;
        if (textScale < 0.5f || textScale > 2.0f || Float.isNaN(textScale)) textScale = 1.0f;
        if (hudOpacity < 0.0f || hudOpacity > 1.0f || Float.isNaN(hudOpacity)) hudOpacity = 0.85f;
        if (roundedCorners < 0 || roundedCorners > 12) roundedCorners = 6;
        if (borderWidth < 0 || borderWidth > 4) borderWidth = 1;
    }

    public void resetHud() {
        hudX = 10.0f;
        hudY = 10.0f;
        hudNormalizedX = -1.0f;
        hudNormalizedY = -1.0f;
        hudScale = 1.0f;
        textScale = 1.0f;
        ensureValid();
    }
}
