package caeruleusTait.world.preview.client.gui.screens;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.TITLE;

public class PreviewTab implements Tab, AutoCloseable {

    private final PreviewContainer previewContainer;

    public PreviewTab(CreateWorldScreen screen) {
        previewContainer = new PreviewContainer(screen, 0, 0, 100, 100);
    }

    @Override
    public @NotNull Component getTabTitle() {
        return TITLE;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        previewContainer.widgets().forEach(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        previewContainer.doLayout(screenRectangle);
    }

    @Override
    public void close() {
        previewContainer.close();
    }

    public PreviewContainer mainScreenWidget() {
        return previewContainer;
    }
}
