package caeruleusTait.world.preview.client.gui.widgets;

import caeruleusTait.world.preview.client.WorldPreviewClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import org.joml.Matrix4f;

import java.awt.*;

public class ColorChooser extends AbstractWidget {

    public static final int INITIAL_SV_SQUARE_SIZE = 128;
    public static final int INITIAL_H_BAR_WIDTH = 16;
    public static final int SEPARATOR = 10;
    public static final int INITIAL_FINAL_COLOR_HEIGHT = 20;

    private int svSquareSize;
    private int hBarWidth;
    private int finalColorHeight;

    private float hue = 0f;
    private float saturation = 0f;
    private float value = 0f;

    private int argbColor = 0xFF000000;
    private int argbHueOnly = 0xFF000000;

    private ColorUpdater updater;

    public ColorChooser(int x, int y) {
        super(x, y, 10, 10, CommonComponents.EMPTY);
        svSquareSize = INITIAL_SV_SQUARE_SIZE;
        hBarWidth = INITIAL_H_BAR_WIDTH;
        finalColorHeight = INITIAL_FINAL_COLOR_HEIGHT;
        recalculateSize();
    }

    private void recalculateSize() {
        width = svSquareSize + SEPARATOR + hBarWidth;
        height = svSquareSize + SEPARATOR + finalColorHeight;
    }

    public void setSquareSize(int squareSize) {
        float scalor = (float) squareSize / (float) INITIAL_SV_SQUARE_SIZE;
        svSquareSize = squareSize;
        hBarWidth = (int)(INITIAL_H_BAR_WIDTH * scalor);
        finalColorHeight = (int)(INITIAL_FINAL_COLOR_HEIGHT * scalor);
        recalculateSize();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        // render background
        guiGraphics.fill(getX() - 2, getY() - 2, getX() + width + 2, getY() + height + 2, 0x77000000);

        RenderSystem.setShader(() -> WorldPreviewClient.HSV_SHADER);
        Matrix4f posMatrix = guiGraphics.pose().last().pose();

        // Render saturation value chooser
        int leftX = getX();
        int topY = getY();
        int rightX = leftX + svSquareSize;
        int botY = topY + svSquareSize;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(posMatrix, (float) leftX, (float) topY, 0).color(hue, 0f, 1f, 1f).endVertex();
        buffer.vertex(posMatrix, (float) leftX, (float) botY, 0).color(hue, 0f, 0f, 1f).endVertex();
        buffer.vertex(posMatrix, (float) rightX, (float) botY, 0).color(hue, 1f, 0f, 1f).endVertex();
        buffer.vertex(posMatrix, (float) rightX, (float) topY, 0).color(hue, 1f, 1f, 1f).endVertex();

        Tesselator.getInstance().end();

        // Render saturation value indicator
        int satX = leftX + Math.round(saturation * svSquareSize);
        int valY = topY + Math.round((1f - value) * svSquareSize);
        guiGraphics.fill(satX - 4, valY - 4, satX + 4, valY + 4, value > .3 ? 0xFF000000 : 0xFFFFFFFF);
        guiGraphics.fill(satX - 3, valY - 3, satX + 3, valY + 3, argbColor);

        // Render Hue chooser
        RenderSystem.setShader(() -> WorldPreviewClient.HSV_SHADER);
        leftX = rightX + SEPARATOR;
        rightX = leftX + hBarWidth;

        buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(posMatrix, (float) leftX, (float) topY, 0).color(1f, 1f, 1f, 1f).endVertex();
        buffer.vertex(posMatrix, (float) leftX, (float) botY, 0).color(0f, 1f, 1f, 1f).endVertex();
        buffer.vertex(posMatrix, (float) rightX, (float) botY, 0).color(0f, 1f, 1f, 1f).endVertex();
        buffer.vertex(posMatrix, (float) rightX, (float) topY, 0).color(1f, 1f, 1f, 1f).endVertex();

        Tesselator.getInstance().end();

        // Render saturation value indicator
        int hueY = topY + Math.round((1f - hue) * svSquareSize);
        guiGraphics.fill(leftX - 2, hueY - 4, rightX + 2, hueY + 4, 0xFF000000);
        guiGraphics.fill(leftX - 1, hueY - 3, rightX + 1, hueY + 3, argbHueOnly);

        // Render final color box
        guiGraphics.fill(getX(), botY + SEPARATOR, getX() + width, getY() + height, argbColor);
    }

    public boolean mouseEvent(double mouseX, double mouseY, int button, boolean playSound) {
        if (!this.active || !this.visible || !isValidClickButton(button) || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (Minecraft.getInstance().screen != null) {
            Minecraft.getInstance().screen.setFocused(this);
        }

        double leftX = getX();
        double topY = getY();
        double rightX = leftX + svSquareSize;
        double botY = topY + svSquareSize;

        boolean updated = false;

        // check if mouse in SV selector
        if (mouseX >= leftX && mouseX <= rightX && mouseY >= topY && mouseY <= botY) {
            if (playSound) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
            value = 1f - (float) ((mouseY - topY) / (botY - topY));
            saturation = (float) ((mouseX - leftX) / (rightX - leftX));
            updated = true;
        }

        leftX = rightX + SEPARATOR;
        rightX = leftX + hBarWidth;

        // check if mouse in hue selector
        if (mouseX >= leftX && mouseX <= rightX && mouseY >= topY && mouseY <= botY) {
            if (playSound) {
                this.playDownSound(Minecraft.getInstance().getSoundManager());
            }
            hue = 1f - (float) ((mouseY - topY) / (botY - topY));
            updated = true;
        }

        argbColor = Color.HSBtoRGB(hue, saturation, value);
        argbHueOnly = Color.HSBtoRGB(hue, 1f, 1f);
        if (updated) {
            runUpdater();
        }
        return updated;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return mouseEvent(mouseX, mouseY, button, true);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        mouseEvent(mouseX, mouseY, 0, false);
    }

    public void runUpdater() {
        if (updater == null) {
            return;
        }

        updater.doUpdate(
                (int) (hue * 360f),
                (int) (saturation * 100f),
                (int) (value * 100f)
        );
    }

    public void setUpdater(ColorUpdater updater) {
        this.updater = updater;
    }

    public void updateHSV(int h, int s, int v) {
        hue = (float) h / 360f;
        saturation = (float) s / 100f;
        value = (float) v / 100f;
        argbColor = Color.HSBtoRGB(hue, saturation, value);
        argbHueOnly = Color.HSBtoRGB(hue, 1f, 1f);
        runUpdater();
    }

    public void updateRGB(int rgb) {
        final int r = (rgb >> 16) & 0xFF;
        final int g = (rgb >> 8) & 0xFF;
        final int b = (rgb >> 0) & 0xFF;

        final float[] hsv = Color.RGBtoHSB(r, g, b, null);
        hue = hsv[0];
        saturation = hsv[1];
        value = hsv[2];
        argbColor = Color.HSBtoRGB(hue, saturation, value);
        argbHueOnly = Color.HSBtoRGB(hue, 1f, 1f);
        runUpdater();
    }

    public int colorRGB() {
        return argbColor & 0x00FFFFFF;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Do nothing
    }

    public interface ColorUpdater {
        void doUpdate(int h, int s, int v);
    }
}
