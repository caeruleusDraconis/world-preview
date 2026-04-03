package caeruleusTait.world.preview.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SpriteSheetButton extends Button {
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffTex;
    protected final ResourceLocation resourceLocation;
    protected final int textureWidth;
    protected final int textureHeight;

    public SpriteSheetButton(
            int x,
            int y,
            int width,
            int height,
            int xTexStart,
            int yTexStart,
            int yDiffTex,
            ResourceLocation resourceLocation,
            int textureWidth,
            int textureHeight,
            OnPress onPress
    ) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, resourceLocation, textureWidth, textureHeight, onPress, CommonComponents.EMPTY);
    }

    public SpriteSheetButton(
            int x,
            int y,
            int width,
            int height,
            int xTexStart,
            int yTexStart,
            int yDiffTex,
            ResourceLocation resourceLocation,
            int textureWidth,
            int textureHeight,
            OnPress onPress,
            Component component
    ) {
        super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.resourceLocation = resourceLocation;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int y = this.yTexStart;
        if (!this.isActive()) {
            y += yDiffTex * 2;
        } else if (this.isHoveredOrFocused()) {
            y += yDiffTex;
        }

        RenderSystem.disableDepthTest();
        guiGraphics.blit(resourceLocation, getX(), getY(), xTexStart, y, width, height, textureWidth, textureHeight);
    }
}
