package caeruleusTait.world.preview.client.gui.screens.settings;

import caeruleusTait.world.preview.RenderSettings;
import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.client.gui.widgets.WGLabel;
import caeruleusTait.world.preview.client.gui.widgets.lists.AbstractSelectionListHolder;
import caeruleusTait.world.preview.client.gui.widgets.lists.BaseObjectSelectionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.SETTINGS_DIM_HEAD;
import static caeruleusTait.world.preview.client.WorldPreviewComponents.SETTINGS_DIM_TITLE;
import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.LINE_HEIGHT;
import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.LINE_VSPACE;

public class DimensionsTab implements Tab {
    private final Minecraft minecraft;
    private final RenderSettings renderSettings;

    private final WGLabel headLabel;
    private final DimensionList dimensionList;
    private final AbstractSelectionListHolder<DimensionList.DimensionEntry, DimensionList> dimensionListHolder;

    public DimensionsTab(Minecraft minecraft, List<ResourceLocation> levelStemKeys) {
        this.minecraft = minecraft;
        this.renderSettings = WorldPreview.get().renderSettings();

        headLabel = new WGLabel(minecraft.font, 0, 0, 256, LINE_HEIGHT, WGLabel.TextAlignment.CENTER, SETTINGS_DIM_HEAD, 0xFFFFFFFF);
        dimensionList = new DimensionList(minecraft, 256, 100, 0, 0);
        dimensionList.replaceEntries(levelStemKeys.stream().map(dimensionList::entryFactory).toList());
        dimensionList.select(renderSettings.dimension);
        dimensionListHolder = new AbstractSelectionListHolder<>(dimensionList, 0, 0, 256, 100, SETTINGS_DIM_TITLE);
    }

    @Override
    public @NotNull Component getTabTitle() {
        return SETTINGS_DIM_TITLE;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        consumer.accept(headLabel);
        consumer.accept(dimensionListHolder);
    }

    @Override
    public void doLayout(ScreenRectangle rectangle) {
        int width = Math.min(rectangle.width() - 8, 256);
        int center = rectangle.left() + (rectangle.width() / 2);
        int left = center - width / 2;
        int top = rectangle.top() + LINE_VSPACE;
        int bottom = rectangle.bottom() - 16;

        headLabel.setWidth(width);
        headLabel.setPosition(left, top);

        top += LINE_HEIGHT + LINE_VSPACE;
        dimensionListHolder.setPosition(left, top);
        dimensionListHolder.setSize(width, bottom - top);
        dimensionList.setRenderBackground(true);
        dimensionList.setRenderTopAndBottom(false);
    }

    public class DimensionList extends BaseObjectSelectionList<DimensionList.DimensionEntry> {
        public DimensionList(Minecraft minecraft, int width, int height, int x, int y) {
            super(minecraft, width, height, x, y, 16);
        }

        public DimensionEntry entryFactory(ResourceLocation dimensionKey) {
            return new DimensionEntry(dimensionKey);
        }

        public void select(ResourceLocation dimensionKey) {
            for (DimensionEntry entry : children()) {
                if (entry.dimensionKey.equals(dimensionKey)) {
                    setSelected(entry);
                    return;
                }
            }
            setSelected(null);
        }

        public class DimensionEntry extends BaseObjectSelectionList.Entry<DimensionEntry> {
            private final ResourceLocation dimensionKey;
            private final Component component;

            public DimensionEntry(ResourceLocation dimensionKey) {
                this.dimensionKey = dimensionKey;
                this.component = Component.literal(dimensionKey.toString());
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal("");
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
                guiGraphics.drawString(minecraft.font, component, left + 5, top + 2, 16777215);
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                if (i != 0) {
                    return false;
                }

                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                renderSettings.dimension = dimensionKey;
                return true;
            }
        }
    }
}
