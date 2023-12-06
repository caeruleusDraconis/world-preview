package caeruleusTait.world.preview.client.gui.screens.settings;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.client.gui.screens.PreviewContainer;
import caeruleusTait.world.preview.client.gui.widgets.ColorChooser;
import caeruleusTait.world.preview.client.gui.widgets.WGLabel;
import caeruleusTait.world.preview.client.gui.widgets.lists.BiomesList;
import caeruleusTait.world.preview.mixin.client.CheckboxAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.*;
import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.LINE_HEIGHT;
import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.LINE_VSPACE;

public class BiomesTab implements Tab {
    private final PreviewContainer previewContainer;
    private final Minecraft minecraft;
    private final GridLayout layout = new GridLayout();
    private final BiomesList biomesList;
    private final CycleButton<BiomeListFilter> filterCycleButton;
    private final List<AbstractWidget> toRender = new ArrayList<>();
    private final ColorChooser colorChooser;
    private BiomesList.BiomeEntry selectedEntry;
    private boolean blockUpdates = false;

    private final Button resetBtn;
    private final Button applyBtn;
    private final Checkbox isCaveCB;

    public BiomesTab(Minecraft _minecraft, PreviewContainer _previewTab) {
        previewContainer = _previewTab;
        minecraft = _minecraft;
        final Font font = minecraft.font;
        biomesList = new BiomesList(previewContainer, minecraft, 100, 100, 0, 0, false);
        toRender.add(biomesList);

        colorChooser = new ColorChooser(0, 0);
        toRender.add(colorChooser);

        final int EDIT_WIDTH = 36;
        final int LABEL_WIDTH = 75;
        final int COLUMN_SPACING = LINE_VSPACE * 2;
        final int FULL_WIDTH = EDIT_WIDTH + LABEL_WIDTH + COLUMN_SPACING + 1; // +1 because of EditBox

        filterCycleButton = CycleButton
                .builder(BiomeListFilter::toComponent)
                .withValues(BiomeListFilter.values())
                .withInitialValue(BiomeListFilter.DIMENSION)
                .create(
                        0,
                        0,
                        FULL_WIDTH,
                        LINE_HEIGHT,
                        COLOR_LIST_FILTER,
                        (b, v) -> biomesList.replaceEntries(v.apply(previewContainer.allBiomes()))
                );
        toRender.add(filterCycleButton);

        WGLabel statusLabel = new WGLabel(
                font,
                0,
                0,
                FULL_WIDTH,
                LINE_HEIGHT,
                WGLabel.TextAlignment.CENTER,
                Component.literal(""),
                0xFFAAAAAA
        );

        EditBox hueBox = new EditBox(font, 0, 0, EDIT_WIDTH, LINE_HEIGHT, COLOR_HUE);
        EditBox satBox = new EditBox(font, 0, 0, EDIT_WIDTH, LINE_HEIGHT, COLOR_SAT);
        EditBox valBox = new EditBox(font, 0, 0, EDIT_WIDTH, LINE_HEIGHT, COLOR_VAL);

        isCaveCB = Checkbox.builder(COLOR_CAVE, minecraft.font).selected(false).onValueChange((x, y) -> updateStatus()).build();

        resetBtn = Button
                .builder(COLOR_RESET, x -> {
                    selectedEntry.reset();
                    ((CheckboxAccessor) isCaveCB).setSelected(selectedEntry.isCave());
                    colorChooser.updateRGB(selectedEntry.color());
                    statusLabel.setText(selectedEntry.statusComponent());
                })
                .width(FULL_WIDTH)
                .build();

        applyBtn = Button
                .builder(COLOR_APPLY, x -> {
                    selectedEntry.changeColor(colorChooser.colorRGB());
                    selectedEntry.setCave(isCaveCB.selected());
                    colorChooser.updateRGB(selectedEntry.color());
                    statusLabel.setText(selectedEntry.statusComponent());
                })
                .width(FULL_WIDTH)
                .build();

        hueBox.setFilter(x -> validateMaxInt(x, 360));
        satBox.setFilter(x -> validateMaxInt(x, 100));
        valBox.setFilter(x -> validateMaxInt(x, 100));

        Consumer<String> hsvConsumer = x -> {
            if (!blockUpdates) {
                colorChooser.updateHSV(
                        intOrZero(hueBox.getValue()),
                        intOrZero(satBox.getValue()),
                        intOrZero(valBox.getValue())
                );
                colorChooser.runUpdater();
            }
        };

        hueBox.setResponder(hsvConsumer);
        satBox.setResponder(hsvConsumer);
        valBox.setResponder(hsvConsumer);

        colorChooser.setUpdater((h, s, v) -> {
            try {
                blockUpdates = true;
                updateIfChanged(hueBox, h);
                updateIfChanged(satBox, s);
                updateIfChanged(valBox, v);
                updateStatus();
            } finally {
                blockUpdates = false;
            }
        });

        biomesList.setBiomeChangeListener(biomeEntry -> {
            selectedEntry = biomeEntry;
            if (selectedEntry != null) {
                colorChooser.updateRGB(selectedEntry.color());
                ((CheckboxAccessor) isCaveCB).setSelected(selectedEntry.isCave());
                statusLabel.setText(selectedEntry.statusComponent());
            }
        });
        biomesList.setSelected(previewContainer.allBiomes().isEmpty() ? null : previewContainer.allBiomes().get(0));

        layout.rowSpacing(LINE_VSPACE).columnSpacing(COLUMN_SPACING);

        GridLayout.RowHelper rowHelper = layout.createRowHelper(2);
        rowHelper.addChild(new WGLabel(font, 0, 0, LABEL_WIDTH, LINE_HEIGHT, WGLabel.TextAlignment.LEFT, COLOR_HUE, 0xFFFFFFFF));
        rowHelper.addChild(hueBox);

        rowHelper.addChild(new WGLabel(font, 0, 0, LABEL_WIDTH, LINE_HEIGHT, WGLabel.TextAlignment.LEFT, COLOR_SAT, 0xFFFFFFFF));
        rowHelper.addChild(satBox);

        rowHelper.addChild(new WGLabel(font, 0, 0, LABEL_WIDTH, LINE_HEIGHT, WGLabel.TextAlignment.LEFT, COLOR_VAL, 0xFFFFFFFF));
        rowHelper.addChild(valBox);

        rowHelper.addChild(new WGLabel(font, 0, 0, LABEL_WIDTH, LINE_HEIGHT / 2, WGLabel.TextAlignment.LEFT, Component.literal(""), 0xFFFFFF));

        rowHelper.addChild(isCaveCB, 2);
        rowHelper.addChild(resetBtn, 2);
        rowHelper.addChild(applyBtn, 2);

        rowHelper.addChild(new WGLabel(font, 0, 0, LABEL_WIDTH, LINE_HEIGHT / 2, WGLabel.TextAlignment.LEFT, Component.literal(""), 0xFFFFFF));
        rowHelper.addChild(statusLabel, 2);
    }

    private void updateStatus() {
        if (selectedEntry != null) {
            resetBtn.active = selectedEntry.color() != colorChooser.colorRGB() || selectedEntry.isCave() != isCaveCB.selected() || selectedEntry.dataSource() == PreviewData.DataSource.CONFIG;
            applyBtn.active = selectedEntry.color() != colorChooser.colorRGB() || selectedEntry.isCave() != isCaveCB.selected();
            isCaveCB.active = true;
        } else {
            resetBtn.active = false;
            applyBtn.active = false;
            isCaveCB.active = false;
        }
    }

    @Override
    public @NotNull Component getTabTitle() {
        return SETTINGS_BIOMES_TITLE;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        toRender.forEach(consumer);
        layout.visitWidgets(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        int leftWidth = screenRectangle.width() / 3;
        int left = screenRectangle.left() + 3;
        int top = screenRectangle.top() + 2;
        int bottom = screenRectangle.bottom() - 8;

        filterCycleButton.setPosition(left, top);
        filterCycleButton.setWidth(leftWidth);

        int listTop = top + LINE_HEIGHT + LINE_VSPACE;
        biomesList.setPosition(left, listTop);
        biomesList.setSize(leftWidth, bottom - listTop - LINE_VSPACE);
        biomesList.setRenderBackground(true);
        biomesList.replaceEntries(filterCycleButton.getValue().apply(previewContainer.allBiomes()));

        colorChooser.setSquareSize(screenRectangle.width() / 4);
        colorChooser.setPosition(left + leftWidth + LINE_VSPACE * 2, top + ((bottom - top) / 2) - colorChooser.getHeight() / 2);

        layout.arrangeElements();
        left = colorChooser.getX() + colorChooser.getWidth();
        ScreenRectangle controlRectangle = new ScreenRectangle(
                left,
                top + 2,
                screenRectangle.right() - left + 16,
                bottom - top - 2
        );
        FrameLayout.alignInRectangle(layout, controlRectangle, .5F, .5F);
    }

    private boolean validateMaxInt(String in, int max) {
        if (in.isBlank()) {
            return true;
        }
        try {
            int i = Integer.parseInt(in);
            return i >= 0 && i <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int intOrZero(String src) {
        return src.isBlank() ? 0 : Integer.parseInt(src);
    }

    private void updateIfChanged(EditBox box, int value) {
        final String strValue = String.valueOf(value);
        if (!box.getValue().equals(strValue)) {
            box.setValue(strValue);
        }
    }

    public enum BiomeListFilter {
        DIMENSION(x -> {
            LevelStem levelStem = x.previewTab().levelStemRegistry().get(WorldPreview.get().renderSettings().dimension);
            if (levelStem == null) {
                return true;
            }
            Set<ResourceLocation> supportedBiomes = levelStem.generator()
                    .getBiomeSource()
                    .possibleBiomes()
                    .stream()
                    .map(Holder::unwrapKey)
                    .map(Optional::orElseThrow)
                    .map(ResourceKey::location)
                    .collect(Collectors.toSet());
            return supportedBiomes.contains(x.entry().key().location());
        }),
        ALL(x -> true),
        MISSING(x -> x.dataSource() == PreviewData.DataSource.MISSING),
        CUSTOM(x -> x.dataSource() == PreviewData.DataSource.CONFIG),
        MISSING_CUSTOM(x -> x.dataSource() == PreviewData.DataSource.MISSING || x.dataSource() == PreviewData.DataSource.CONFIG),
        DATA_PACK(x -> x.dataSource() == PreviewData.DataSource.RESOURCE),
        DATA_PACK_CUSTOM(x -> x.dataSource() == PreviewData.DataSource.RESOURCE || x.dataSource() == PreviewData.DataSource.CONFIG),
        ;

        private final Predicate<BiomesList.BiomeEntry> filterFn;

        BiomeListFilter(Predicate<BiomesList.BiomeEntry> filterFn) {
            this.filterFn = filterFn;
        }

        public List<BiomesList.BiomeEntry> apply(List<BiomesList.BiomeEntry> orig) {
            return orig.stream().filter(filterFn).toList();
        }

        public static Component toComponent(BiomeListFilter x) {
            return Component.translatable("world_preview.settings.biomes.filter." + x.name());
        }
    }
}
