package caeruleusTait.world.preview.client.gui.widgets.lists;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbstractSelectionListHolder<E extends BaseObjectSelectionList.Entry<E>, T extends BaseObjectSelectionList<E>> extends AbstractWidget {
    public final T theList;

    public AbstractSelectionListHolder(T theList, int x, int y, int width, int height, Component component) {
        super(x, y, width, height, component);
        this.theList = theList;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        theList.render(guiGraphics, i, j, f);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        theList.updateNarration(narrationElementOutput);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.visible) {
            theList.mouseMoved(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.visible && theList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.visible && theList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return this.visible && theList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return this.visible && theList.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.visible && theList.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return this.visible && theList.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return this.visible && theList.charTyped(codePoint, modifiers);
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (this.visible) {
            return theList.nextFocusPath(focusNavigationEvent);
        }
        return null;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && theList.isMouseOver(mouseX, mouseY);
    }

    @Nullable
    @Override
    public ComponentPath getCurrentFocusPath() {
        return theList.getCurrentFocusPath();
    }

    @Override
    public boolean isFocused() {
        return theList.isFocused();
    }

    @Override
    public void setFocused(boolean bl) {
        theList.setFocused(bl);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return theList.getRectangle();
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        theList.setWidth(width);
    }

    public void setHeight(int height) {
        this.height = height;
        theList.setHeight(height);
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        theList.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        theList.setY(y);
    }
}
