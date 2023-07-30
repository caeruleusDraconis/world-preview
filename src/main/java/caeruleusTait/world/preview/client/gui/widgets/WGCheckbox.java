// Copyright 2022 - 2022, Caeruleus Draconis and Taiterio
// SPDX-License-Identifier: Apache-2.0

package caeruleusTait.world.preview.client.gui.widgets;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;

public class WGCheckbox extends Checkbox {
    public interface OnPress {
        void onPress(WGCheckbox checkbox);
    }

    private final OnPress cb;

    public WGCheckbox(int x, int y, int width, int height, Component component, OnPress onPress, boolean selected) {
        super(x, y, width, height, component, selected);
        cb = onPress;
    }

    public WGCheckbox(int x, int y, int width, int height, Component component, OnPress onPress, boolean selected, boolean showLabel) {
        super(x, y, width, height, component, selected, showLabel);
        cb = onPress;
    }

    @Override
    public void onPress() {
        super.onPress();
        if (cb != null) {
            cb.onPress(this);
        }
    }

    public void setSelected(boolean isSelected) {
        if (isSelected != selected()) {
            super.onPress();
        }
    }
}
