// Copyright 2022 - 2022, Caeruleus Draconis and Taiterio
// SPDX-License-Identifier: Apache-2.0

package caeruleusTait.world.preview.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class WGLabel extends AbstractWidget {
    private Font font;
    private Component component;
    private int color;

    private TextAlignment alignment;
    private int textWidth;
    private int startX;
    private int startY;

    public WGLabel(Font _font, int _x, int _y, int _width, int _height, TextAlignment _alignment, Component _component, int _color) {
        super(_x, _y, _width, _height, _component);
        font = _font;
        component = _component;
        color = _color;
        alignment = _alignment;

        update();
    }

    public void update() {
        textWidth = font.width(component.getVisualOrderText());

        startY = getY() + (height / 2) - (font.lineHeight / 2);
        startX = switch (alignment) {
            case LEFT -> getX();
            case CENTER -> (getX() + (width / 2)) - (textWidth / 2);
            case RIGHT -> (getX() + width) - textWidth;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Do nothing...
        return false;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        update();
    }

    @Override
    public void setY(int i) {
        super.setY(i);
        update();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        update();
    }

    public void setText(Component _component) {
        component = _component;
        update();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.drawString(font, component, startX, startY, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    public enum TextAlignment {
        LEFT, CENTER, RIGHT
    }
}
