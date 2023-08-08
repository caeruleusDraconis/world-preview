package caeruleusTait.world.preview.client.gui.screens.settings;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.WorldPreviewConfig;
import caeruleusTait.world.preview.backend.color.ColorMap;
import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.client.WorldPreviewClient;
import caeruleusTait.world.preview.client.gui.widgets.WGCheckbox;
import caeruleusTait.world.preview.client.gui.widgets.WGLabel;
import caeruleusTait.world.preview.client.gui.widgets.lists.AbstractSelectionListHolder;
import caeruleusTait.world.preview.client.gui.widgets.lists.BaseObjectSelectionList;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.*;
import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.LINE_HEIGHT;
import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.LINE_VSPACE;

public class HeightmapTab implements Tab {

    private final WorldPreviewConfig cfg;

    private final WGLabel disabledWarning;
    private final WGLabel presetsHead;
    private final HeightPresetList heightPresetList;
    private final AbstractSelectionListHolder<HeightPresetList.HeightPresetEntry, HeightPresetList> heightPresetListHolder;

    private final EditBox minYBox;
    private final EditBox maxYBox;

    private final WGLabel minYLabel;
    private final WGLabel maxYLabel;

    private final WGCheckbox visualYRange;

    private final WGLabel colormapHead;
    private final ColormapList colormapList;
    private final AbstractSelectionListHolder<ColormapList.ColormapEntry, ColormapList> colormapListHolder;

    private final List<AbstractWidget> toRender = new ArrayList<>();

    public HeightmapTab(Minecraft minecraft, PreviewData previewData) {
        cfg = WorldPreview.get().cfg();
        final Font font = minecraft.font;

        disabledWarning = new WGLabel(font, 0, 0, 100, LINE_HEIGHT, WGLabel.TextAlignment.CENTER, SETTINGS_HEIGHTMAP_DISABLED, 0xFFFFFF);
        toRender.add(disabledWarning);

        presetsHead = new WGLabel(font, 0, 0, 100, LINE_HEIGHT / 2, WGLabel.TextAlignment.CENTER, SETTINGS_HEIGHTMAP_PRESETS, 0xFFFFFF);
        colormapHead = new WGLabel(font, 0, 0, 100, LINE_HEIGHT / 2, WGLabel.TextAlignment.CENTER, SETTINGS_HEIGHTMAP_COLORMAP, 0xFFFFFF);
        toRender.add(presetsHead);
        toRender.add(colormapHead);

        minYBox = new EditBox(font, 0, 0, 100, LINE_HEIGHT, SETTINGS_HEIGHTMAP_MIN_Y);
        maxYBox = new EditBox(font, 0, 0, 100, LINE_HEIGHT, SETTINGS_HEIGHTMAP_MAX_Y);
        minYBox.setTooltip(Tooltip.create(SETTINGS_HEIGHTMAP_MIN_Y_TOOLTIP));
        maxYBox.setTooltip(Tooltip.create(SETTINGS_HEIGHTMAP_MAX_Y_TOOLTIP));
        minYBox.setFilter(HeightmapTab::isInteger);
        maxYBox.setFilter(HeightmapTab::isInteger);
        minYBox.setValue(String.valueOf(cfg.heightmapMinY));
        maxYBox.setValue(String.valueOf(cfg.heightmapMaxY));
        minYBox.setResponder(x -> cfg.heightmapMinY = x.isBlank() ? 0 : Integer.parseInt(x));
        maxYBox.setResponder(x -> cfg.heightmapMaxY = x.isBlank() ? 0 : Integer.parseInt(x));
        toRender.add(minYBox);
        toRender.add(maxYBox);

        minYLabel = new WGLabel(font, 0, 0, 64, LINE_HEIGHT, WGLabel.TextAlignment.LEFT, SETTINGS_HEIGHTMAP_MIN_Y, 0xFFFFFF);
        maxYLabel = new WGLabel(font, 0, 0, 64, LINE_HEIGHT, WGLabel.TextAlignment.LEFT, SETTINGS_HEIGHTMAP_MAX_Y, 0xFFFFFF);
        minYLabel.setTooltip(Tooltip.create(SETTINGS_HEIGHTMAP_MIN_Y_TOOLTIP));
        maxYLabel.setTooltip(Tooltip.create(SETTINGS_HEIGHTMAP_MAX_Y_TOOLTIP));
        toRender.add(minYLabel);
        toRender.add(maxYLabel);

        visualYRange = new WGCheckbox(0, 0, 100, LINE_HEIGHT, SETTINGS_HEIGHTMAP_VISUAL, x -> cfg.onlySampleInVisualRange = x.selected(), cfg.onlySampleInVisualRange);
        visualYRange.setTooltip(Tooltip.create(SETTINGS_HEIGHTMAP_VISUAL_TOOLTIP));
        toRender.add(visualYRange);

        heightPresetList = new HeightPresetList(minecraft, 100, 100, 0, 0);
        heightPresetList.replaceEntries(
                previewData.heightmapPresets()
                        .stream()
                        .map(entry -> heightPresetList.createEntry(entry.name(), entry.minY(), entry.maxY(), x -> {
                                cfg.heightmapMinY = x.minY;
                                cfg.heightmapMaxY = x.maxY;
                                minYBox.setValue(String.valueOf(x.minY));
                                maxYBox.setValue(String.valueOf(x.maxY));
                        }))
                        .toList()
        );

        heightPresetListHolder = new AbstractSelectionListHolder<>(heightPresetList, 0, 0, 100, 100, Component.empty());
        toRender.add(heightPresetListHolder);


        colormapList = new ColormapList(minecraft, 100, 100, 0, 0);
        var colormaps = previewData.colorMaps()
                .values()
                .stream()
                .map(colorMap -> colormapList.createEntry(colorMap, x -> cfg.colorMap = x.colorMap.key().toString()))
                .collect(Collectors.toMap(x -> x.colorMap.key().toString(), x -> x));
        colormapList.replaceEntries(colormaps.values().stream().sorted(Comparator.comparing(x -> x.name)).toList());
        colormapList.setSelected(colormaps.get(cfg.colorMap));
        colormapListHolder = new AbstractSelectionListHolder<>(colormapList, 0, 0, 100, 100, Component.empty());
        toRender.add(colormapListHolder);
    }

    private static boolean isInteger(String s) {
        if (s.isBlank()) {
            return true;
        }
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @NotNull Component getTabTitle() {
        return SETTINGS_HEIGHTMAP_TITLE;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        toRender.forEach(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        int center = screenRectangle.width() / 2;
        int leftL = screenRectangle.left() + 3;
        int leftR = center + 3;
        int topL = screenRectangle.top() + 2;
        int topR = topL;
        int bottomL = screenRectangle.bottom() - 36;
        int bottomR = bottomL;
        int secWidth = (screenRectangle.width() / 2) - leftL - 4;

        // TOP WARNING
        if (cfg.sampleHeightmap) {
            disabledWarning.visible = false;
        } else {
            disabledWarning.visible = true;
            disabledWarning.setPosition(screenRectangle.left(), topL);
            disabledWarning.setWidth(screenRectangle.width());
            topL += LINE_HEIGHT + LINE_VSPACE;
            topR = topL;
        }

        // LEFT COLUMN
        //  - BOTTOM
        final int labelWidth = 100;

        visualYRange.setPosition(leftL, bottomL);
        visualYRange.setWidth(secWidth);

        //      New line
        bottomL -= (LINE_HEIGHT + LINE_VSPACE);
        maxYLabel.setPosition(leftL, bottomL);
        maxYLabel.setWidth(labelWidth);

        maxYBox.setPosition(leftL + labelWidth, bottomL);
        maxYBox.setWidth(secWidth - labelWidth);

        //      New line
        bottomL -= (LINE_HEIGHT + LINE_VSPACE);
        minYLabel.setPosition(leftL, bottomL);
        minYLabel.setWidth(labelWidth);

        minYBox.setPosition(leftL + labelWidth, bottomL);
        minYBox.setWidth(secWidth - labelWidth);

        //  - TOP
        presetsHead.setPosition(leftL, topL);
        presetsHead.setWidth(secWidth);

        //      New Line
        topL += (LINE_HEIGHT / 2) + LINE_VSPACE;
        heightPresetListHolder.setPosition(leftL, topL);
        heightPresetListHolder.setSize(secWidth, bottomL - topL - LINE_VSPACE);
        heightPresetList.setRenderBackground(true);
        heightPresetList.setRenderTopAndBottom(false);

        // RIGHT COLUMN
        //  - TOP
        colormapHead.setPosition(leftR, topR);
        colormapHead.setWidth(secWidth);

        //      New Line
        topR += (LINE_HEIGHT / 2) + LINE_VSPACE;
        colormapListHolder.setPosition(leftR, topR);
        colormapListHolder.setSize(secWidth, bottomR - topR + LINE_HEIGHT);
        colormapList.setRenderBackground(true);
        colormapList.setRenderTopAndBottom(false);
    }


    public static class HeightPresetList extends BaseObjectSelectionList<HeightPresetList.HeightPresetEntry> {

        public HeightPresetList(Minecraft minecraft, int width, int height, int x, int y) {
            super(minecraft, width, height, x, y, 16);
        }

        public HeightPresetEntry createEntry(String name, int minY, int maxY, Consumer<HeightPresetEntry> onClick) {
            return new HeightPresetEntry(name, minY, maxY, onClick);
        }

        public class HeightPresetEntry extends Entry<HeightPresetEntry> {
            public final String name;
            public final int minY;
            public final int maxY;
            private final Consumer<HeightPresetEntry> onClick;

            private final String displayString;

            public HeightPresetEntry(String name, int minY, int maxY, Consumer<HeightPresetEntry> onClick) {
                this.name = name;
                this.minY = minY;
                this.maxY = maxY;
                this.onClick = onClick;

                this.displayString = String.format("%s: §3y=§b%d§r§3-§b%d§r", this.name, this.minY, this.maxY);
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(
                    GuiGraphics guiGraphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,

                    int mouseY,
                    boolean bl,
                    float partialTick
            ) {
                guiGraphics.drawString(minecraft.font, displayString, left + 4, top + 2, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                onClick.accept(this);
                return true;
            }
        }
    }

    public static class ColormapList extends BaseObjectSelectionList<ColormapList.ColormapEntry> {

        public ColormapList(Minecraft minecraft, int width, int height, int x, int y) {
            super(minecraft, width, height, x, y, 32);
        }

        public ColormapEntry createEntry(ColorMap colorMap, Consumer<ColormapEntry> onClick) {
            return new ColormapEntry(colorMap, onClick);
        }

        public class ColormapEntry extends Entry<ColormapEntry> {
            public final String name;
            public final ColorMap colorMap;
            private final Consumer<ColormapEntry> onClick;

            private final NativeImage colormapImg;
            private final DynamicTexture colormapTexture;

            public ColormapEntry(ColorMap colorMap, Consumer<ColormapEntry> onClick) {
                this.name = colorMap.name();
                this.colorMap = colorMap;
                this.onClick = onClick;

                this.colormapImg = new NativeImage(NativeImage.Format.RGBA, 1024, 1, true);
                this.colormapTexture = new DynamicTexture(this.colormapImg);

                for (int i = 0; i < 1024; ++i) {
                    this.colormapImg.setPixelRGBA(i, 0, colorMap.getARGB((float)i / 1024f));
                }
                this.colormapTexture.upload();
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }

            @Override
            public void render(
                    GuiGraphics guiGraphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,

                    int mouseY,
                    boolean bl,
                    float partialTick
            ) {
                guiGraphics.drawString(minecraft.font, name, left + 4, top + 2, 0xFFFFFF);

                final int xMin = left + 5;
                final int yMin = top + 14;
                final int xMax = left + width - 5;
                final int yMax = top + height - 3;

                WorldPreviewClient.renderTexture(colormapTexture, xMin, yMin, xMax, yMax);

                final int colorBorder = 0xFF999999;

                // Create a border
                guiGraphics.fill(xMin-1, yMin-1, xMax+1, yMin, colorBorder); // Right
                guiGraphics.fill(xMax, yMin, xMax+1, yMax, colorBorder); // Down
                guiGraphics.fill(xMin-1, yMax, xMax+1, yMax+1, colorBorder); // Left
                guiGraphics.fill(xMin-1, yMin, xMin, yMax, colorBorder); // Up
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                onClick.accept(this);
                return true;
            }
        }
    }
}
