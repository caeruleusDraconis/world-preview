package caeruleusTait.world.preview.client.gui.screens;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.color.PreviewMappingData;
import caeruleusTait.world.preview.client.gui.screens.settings.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.stream.Collectors;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.SETTINGS_TITLE;

public class SettingsScreen extends Screen {
    public static final ResourceLocation HEADER_SEPERATOR = new ResourceLocation("textures/gui/header_separator.png");
    public static final ResourceLocation FOOTER_SEPERATOR = new ResourceLocation("textures/gui/footer_separator.png");
    public static final ResourceLocation LIGHT_DIRT_BACKGROUND = new ResourceLocation("textures/gui/light_dirt_background.png");

    private final Screen lastScreen;
    private final PreviewTab previewTab;

    private TabManager tabManager;
    private TabNavigationBar tabNavigationBar;
    private GridLayout bottomButtons;

    public SettingsScreen(Screen lastScreen, PreviewTab previewTab) {
        super(SETTINGS_TITLE);
        this.lastScreen = lastScreen;
        this.previewTab = previewTab;

        this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
    }

    @Override
    protected void init() {
        tabNavigationBar = TabNavigationBar.builder(tabManager, this.width)
                .addTabs(
                        new GeneralTab(minecraft),
                        new SamplingTab(minecraft),
                        new HeightmapTab(minecraft, previewTab.previewData()),
                        new DimensionsTab(minecraft, previewTab.levelStemKeys()),
                        new BiomesTab(minecraft, previewTab)
                )
                .build();
        tabNavigationBar.selectTab(0, false);
        addRenderableWidget(tabNavigationBar);

        bottomButtons = new GridLayout().columnSpacing(10);
        GridLayout.RowHelper rowHelper = bottomButtons.createRowHelper(1);
        rowHelper.addChild(Button.builder(CommonComponents.GUI_BACK, button -> onClose()).build());
        this.bottomButtons.visitWidgets((abstractWidget) -> {
            abstractWidget.setTabOrderGroup(1);
            this.addRenderableWidget(abstractWidget);
        });

        repositionElements();
    }

    @Override
    public void repositionElements() {
        if (tabNavigationBar != null) {
            tabNavigationBar.setWidth(this.width);
            tabNavigationBar.arrangeElements();

            bottomButtons.arrangeElements();
            FrameLayout.centerInRectangle(this.bottomButtons, 0, this.height - 36, this.width, 36);
            int i = this.tabNavigationBar.getRectangle().bottom();
            ScreenRectangle screenRectangle = new ScreenRectangle(0, i, this.width, this.bottomButtons.getY() - i);
            this.tabManager.setTabArea(screenRectangle);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(FOOTER_SEPERATOR, 0, Mth.roundToward(this.height - 36 - 2, 2), 0.0F, 0.0F, this.width, 2, 32, 2);
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void renderDirtBackground(GuiGraphics guiGraphics) {
        guiGraphics.blit(LIGHT_DIRT_BACKGROUND, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
    }

    @Override
    public void tick() {
        this.tabManager.tickCurrent();
    }

    @Override
    public void onClose() {
        Map<ResourceLocation, PreviewMappingData.ColorEntry> toWrite = previewTab.allBiomes()
                .stream()
                .filter(x -> x.dataSource() == PreviewData.DataSource.CONFIG)
                .collect(
                        Collectors.toMap(
                                x -> x.entry().key().location(),
                                x -> new PreviewMappingData.ColorEntry(PreviewData.DataSource.CONFIG, x.color(), x.isCave(), x.name())
                        )
                );
        WorldPreview.get().writeUserColorConfig(toWrite);

        // Apply transient changes to the color data
        previewTab.patchColorData();
        previewTab.resetTabs();

        // Go back
        minecraft.setScreen(lastScreen);
    }


}
