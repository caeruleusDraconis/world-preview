// Copyright 2022 - 2022, Caeruleus Draconis and Taiterio
// SPDX-License-Identifier: Apache-2.0

package caeruleusTait.world.preview.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WGCheckbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox");

    public interface OnPress {
        void onPress(WGCheckbox checkbox);
    }

    private final OnPress cb;
    private final boolean showLabel;
    private boolean selected;

    public WGCheckbox(int x, int y, int width, int height, Component component, OnPress onPress, boolean selected) {
        this(x, y, width, height, component, onPress, selected, true);
    }

    public WGCheckbox(int x, int y, int width, int height, Component component, OnPress onPress, boolean selected, boolean showLabel) {
        super(x, y, width, height, component);
        this.cb = onPress;
        this.selected = selected;
        this.showLabel = showLabel;
    }

    @Override
    public void onPress() {
        selected = !selected;
        if (cb != null) {
            cb.onPress(this);
        }
    }

    public boolean selected() {
        return selected;
    }

    public void setSelected(boolean isSelected) {
        selected = isSelected;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        RenderSystem.enableDepthTest();
        ResourceLocation sprite;
        if (selected) {
            sprite = isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            sprite = isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        int boxSize = Math.min(17, getHeight());
        guiGraphics.blitSprite(sprite, getX(), getY() + (getHeight() - boxSize) / 2, boxSize, boxSize);
        if (showLabel) {
            guiGraphics.drawString(font, getMessage(), getX() + boxSize + 4, getY() + (getHeight() - 8) / 2, 0xE0E0E0);
        }
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage());
    }
}
