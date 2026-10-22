package com.febreze.autosprintplus.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Mouse hooks for Minecraft 1.21.9+, which pass a MouseButtonEvent.
 * The legacy variant (src/variants/mouse-legacy) exposes the same hooks for 1.21 - 1.21.8.
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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return onPress(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return onDrag(event.x(), event.y(), event.button()) || super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return onRelease(event.x(), event.y(), event.button()) || super.mouseReleased(event);
    }
}
