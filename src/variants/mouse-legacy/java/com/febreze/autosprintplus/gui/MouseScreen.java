package com.febreze.autosprintplus.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Mouse hooks for Minecraft 1.21 - 1.21.8, which use raw (x, y, button) signatures.
 * The modern variant (src/variants/mouse-modern) exposes the same hooks for 1.21.9+.
 */
public abstract class MouseScreen extends Screen {
    protected MouseScreen(Component title) {
        super(title);
    }

    protected boolean onPress(double mouseX, double mouseY, int button) {
        return false;
    }

    protected boolean onDrag(double mouseX, double mouseY, int button) {
        return false;
    }

    protected boolean onRelease(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return onPress(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return onDrag(mouseX, mouseY, button) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return onRelease(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }
}
