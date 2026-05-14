package caeruleusTait.world.preview.client.gui.widgets.lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;

import java.util.Collection;

public abstract class BaseObjectSelectionList<E extends BaseObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    private boolean renderBackground = true;
    private boolean renderTopAndBottom = true;

    public BaseObjectSelectionList(Minecraft minecraft, int width, int height, int x, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        setLeftPos(x);
    }

    public void setHeight(int height) {
        super.setHeight(height);
    }

    public void setWidth(int width) {
        super.setWidth(width);
    }

    public void setTopPos(int top) {
        setY(top);
    }

    public void setLeftPos(int x0) {
        setX(x0);
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }

    @Override
    public int getRowRight() {
        return this.getRight() - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 6;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRight() - 6;
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int rowTop, int rowWidth, int innerHeight, int boxBorderColor, int boxInnerColor) {
        int left = this.getRowLeft();
        int right = this.getRowRight();
        guiGraphics.fill(left, rowTop - 2, right, rowTop + innerHeight + 2, boxBorderColor);
        guiGraphics.fill(left + 1, rowTop - 1, right - 1, rowTop + innerHeight + 1, boxInnerColor);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isMouseOver(mouseX, mouseY)) {
            E e = getEntryAtPosition(mouseX, mouseY);
            if (e != null && e.tooltip() != null && minecraft.screen != null) {
                minecraft.screen.setTooltipForNextRenderPass(e.tooltip(), DefaultTooltipPositioner.INSTANCE, this.isFocused());
            }
        }
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void setRenderBackground(boolean renderBackground) {
        this.renderBackground = renderBackground;
    }

    public void setRenderTopAndBottom(boolean renderTopAndBottom) {
        this.renderTopAndBottom = renderTopAndBottom;
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
        if (renderBackground) {
            super.renderListBackground(guiGraphics);
        }
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
        if (renderTopAndBottom) {
            super.renderListSeparators(guiGraphics);
        }
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
