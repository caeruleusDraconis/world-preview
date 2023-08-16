package caeruleusTait.world.preview.client.gui.screens;

import caeruleusTait.world.preview.client.WorldPreviewComponents;
import caeruleusTait.world.preview.client.gui.PreviewContainerDataProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.security.InvalidParameterException;

import static net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.FOOTER_SEPERATOR;

public class InGamePreviewScreen extends Screen implements PreviewContainerDataProvider {

    private IntegratedServer integratedServer;
    private PreviewContainer previewContainer;

    public InGamePreviewScreen() {
        super(WorldPreviewComponents.TITLE_FULL);
    }

    @Override
    protected void init() {
        if (integratedServer == null) {
            integratedServer = minecraft.getSingleplayerServer();
            if (integratedServer == null) {
                throw new InvalidParameterException("No integrated server!");
            }
        }

        if (previewContainer == null) {
            previewContainer = new PreviewContainer(this, this);
            previewContainer.start();
        }

        previewContainer.widgets().forEach(this::addRenderableWidget);
        previewContainer.doLayout(new ScreenRectangle(0, 18, width, height - 38));

        Button btn = Button
                .builder(CommonComponents.GUI_BACK, x -> onClose())
                .width(100)
                .pos(width / 2 - 50, height - 24)
                .build();
        addRenderableWidget(btn);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDirtBackground(guiGraphics);
        // renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(minecraft.font, WorldPreviewComponents.TITLE_FULL, width / 2, 6, 0xFFFFFF);
        guiGraphics.blit(FOOTER_SEPERATOR, 0, Mth.roundToward(this.height - 30, 2), 0.0F, 0.0F, this.width, 2, 32, 2);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Nothing to do, since we already have an integrated server
     */
    @Override
    public @Nullable WorldCreationContext previewWorldCreationContext() {
        return null;
    }

    @Override
    public void registerSettingsChangeListener(Runnable listener) {
        // Nothing to do
    }

    @Override
    public String seed() {
        return String.valueOf(integratedServer.overworld().getSeed());
    }

    @Override
    public void updateSeed(String newSeed) {
        // Do nothing
    }

    @Override
    public @Nullable Path tempDataPackDir() {
        return null;
    }

    @Override
    public @Nullable MinecraftServer minecraftServer() {
        return integratedServer;
    }

    @Override
    public WorldOptions worldOptions(@Nullable WorldCreationContext wcContext) {
        return integratedServer.getWorldData().worldGenOptions();
    }

    @Override
    public WorldDataConfiguration worldDataConfiguration(@Nullable WorldCreationContext wcContext) {
        return integratedServer.getWorldData().getDataConfiguration();
    }

    @Override
    public RegistryAccess.Frozen registryAccess(@Nullable WorldCreationContext wcContext) {
        return integratedServer.registryAccess();
    }

    @Override
    public Registry<LevelStem> levelStemRegistry(@Nullable WorldCreationContext wcContext) {
        return integratedServer.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
    }

    @Override
    public LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess(@Nullable WorldCreationContext wcContext) {
        return integratedServer.registries();
    }
}
