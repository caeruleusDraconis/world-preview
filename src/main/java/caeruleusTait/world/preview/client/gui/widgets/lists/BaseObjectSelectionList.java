package caeruleusTait.world.preview.client.gui.widgets.lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;

import java.util.Collection;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;

public abstract class BaseObjectSelectionList<E extends BaseObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    public BaseObjectSelectionList(Minecraft minecraft, int width, int height, int x, int y, int itemHeight) {
        super(minecraft, width, height, x, y, itemHeight);
    }

    public void setHeight(int height) {
        this.height = height;
        this.y1 = y0 + this.height;
    }

    public void setWidth(int width) {
        this.width = width;
        this.x1 = x0 + this.width;
    }

    public void setTopPos(int top) {
        this.y0 = top;
        this.y1 = top + this.height;
    }

    @Override
    public void setLeftPos(int x0) {
        this.x0 = x0;
        this.x1 = x0 + this.width;
    }

    @Override
    public int getRowLeft() {
        return this.x0;
    }

    @Override
    public int getRowRight() {
        return this.x1 - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 6;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int rowTop, int rowWidth, int innerHeight, int boxBorderColor, int boxInnerColor) {
        int left = this.getRowLeft();
        int right = this.getRowRight();
        guiGraphics.fill(left, rowTop - 2, right, rowTop + innerHeight + 2, boxBorderColor);
        guiGraphics.fill(left + 1, rowTop - 1, right - 1, rowTop + innerHeight + 1, boxInnerColor);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isMouseOver(mouseX, mouseY)) {
            E e = getEntryAtPosition(mouseX, mouseY);
            if (e != null && e.tooltip() != null && minecraft.screen != null) {
                minecraft.screen.setTooltipForNextRenderPass(e.tooltip(), DefaultTooltipPositioner.INSTANCE, this.isFocused());
            }
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Make public
     */
    @Override
    public void replaceEntries(Collection<E> entryList) {
        super.replaceEntries(entryList);
    }

    public abstract static class Entry<E extends Entry<E>> extends ObjectSelectionList.Entry<E> {
        public Tooltip tooltip() {
            return null;
        }
    }
}
