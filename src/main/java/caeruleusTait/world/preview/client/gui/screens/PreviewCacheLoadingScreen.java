package caeruleusTait.world.preview.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PreviewCacheLoadingScreen extends Screen {
    protected PreviewCacheLoadingScreen(Component component) {
        super(component);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDirtBackground(guiGraphics);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, title, width / 2, height / 2, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
