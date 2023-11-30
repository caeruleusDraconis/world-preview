package caeruleusTait.world.preview.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Only exists to be able to override the tooltip positioner
 */
public class WGTooltip extends Tooltip {
    public WGTooltip(Component component) {
        super(component, component);
    }

    @Override
    protected @NotNull ClientTooltipPositioner createTooltipPositioner(boolean bl, boolean bl2, ScreenRectangle screenRectangle) {
        return DefaultTooltipPositioner.INSTANCE;
    }

    @Override
    public void refreshTooltipForNextRenderPass(boolean bl, boolean bl2, ScreenRectangle screenRectangle) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null) {
            screen.setTooltipForNextRenderPass(this, this.createTooltipPositioner(bl, bl2, screenRectangle), bl2);
        }
    }
}
