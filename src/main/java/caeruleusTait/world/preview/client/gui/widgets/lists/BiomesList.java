package caeruleusTait.world.preview.client.gui.widgets.lists;

import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.client.WorldPreviewClient;
import caeruleusTait.world.preview.client.gui.screens.PreviewContainer;
import caeruleusTait.world.preview.client.gui.widgets.WGTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

import static caeruleusTait.world.preview.WorldPreview.nativeColor;

public class BiomesList extends BaseObjectSelectionList<BiomesList.BiomeEntry> {
    private Consumer<BiomeEntry> onBiomeSelected;
    private final boolean allowDeselecting;
    private final PreviewContainer previewContainer;

    public BiomesList(PreviewContainer previewContainer, Minecraft minecraft, int width, int height, int x, int y, boolean allowDeselecting) {
        super(minecraft, width, height, x, y, 16);
        this.allowDeselecting = allowDeselecting;
        this.previewContainer = previewContainer;
    }

    public BiomeEntry createEntry(Holder.Reference<Biome> entry, short id, int color, int initialColor, boolean isCave, boolean initialIsCave, String explicitName, PreviewData.DataSource dataSource) {
        return new BiomeEntry(entry, id, color, initialColor, isCave, initialIsCave, explicitName, dataSource);
    }

    public void setSelected(@Nullable BiomesList.BiomeEntry entry)
    {
        setSelected(entry, false);
    }

    public void setSelected(@Nullable BiomesList.BiomeEntry entry, boolean centerScroll) {
        super.setSelected(entry);
        if(centerScroll == true) {
            super.centerScrollOn(entry);
        }
        onBiomeSelected.accept(entry);
    }



    /**
     * On deselect, {@code null} will be sent!
     */
    public void setBiomeChangeListener(Consumer<BiomeEntry> onBiomeSelected) {
        this.onBiomeSelected = onBiomeSelected;
    }

    @Override
    public void replaceEntries(Collection<BiomeEntry> entryList) {
        final BiomeEntry oldEntry = getSelected();
        super.replaceEntries(entryList);

        if (entryList.contains(oldEntry)) {
            setSelected(oldEntry);
        }

        // If we have more than one page, make sure we don't let the scrollbar run away
        double maxScroll = Math.max(0.0, super.getItemCount() * super.itemHeight - super.height);
        if(super.getScrollAmount() > maxScroll) {
            // Make sure that the top entry is visible
            super.setScrollAmount(maxScroll);
        }
    }

    public class BiomeEntry extends BaseObjectSelectionList.Entry<BiomeEntry> {
        private final short id;
        private final String name;
        private int color;
        private boolean isCave;
        private final int initialColor;
        private final boolean initialIsCave;
        private final Holder.Reference<Biome> entry;
        private PreviewData.DataSource dataSource;
        private final Tooltip tooltip;
        private final PreviewData.DataSource initialDataSource;
        private final boolean isPrimaryNamespace;

        public BiomeEntry(Holder.Reference<Biome> entry, short id, int color, int initialColor, boolean isCave, boolean initialIsCave, String explicitName, PreviewData.DataSource dataSource) {
            this.entry = entry;
            this.id = id;
            this.color = color;
            this.initialColor = initialColor;
            this.isCave = isCave;
            this.initialIsCave = initialIsCave;
            this.dataSource = dataSource;
            this.initialDataSource = dataSource;
            final ResourceLocation resourceLocation = entry.key().location();
            final String langKey = resourceLocation.toLanguageKey("biome");
            if (Language.getInstance().has(langKey)) {
                this.name = Component.translatable(langKey).getString();
            } else if (explicitName != null && !explicitName.isBlank()) {
                this.name = explicitName;
            } else {
                this.name = WorldPreviewClient.toTitleCase(resourceLocation.getPath().replace("_", " "));
            }
            this.isPrimaryNamespace = resourceLocation.getNamespace().equals("minecraft");

            String tag = "§5§o" + resourceLocation.getNamespace() + "§r\n§9" + resourceLocation.getPath() + "§r";
            this.tooltip = new WGTooltip(Component.literal(this.name + "\n\n" + tag));
        }

        public String name() {
            return name;
        }

        public Component statusComponent() {
            return Component.translatable("world_preview.settings.biomes.source." + dataSource.name());
        }

        public Holder.Reference<Biome> entry() {
            return entry;
        }

        public short id() {
            return id;
        }

        public int color() {
            return color;
        }

        public boolean isCave() {
            return isCave;
        }

        public PreviewData.DataSource dataSource() {
            return dataSource;
        }

        public PreviewContainer previewTab() {
            return previewContainer;
        }

        @Override
        public Tooltip tooltip() {
            return tooltip;
        }

        public void reset() {
            color = initialColor;
            isCave = initialIsCave;
            dataSource = initialDataSource == PreviewData.DataSource.CONFIG ? PreviewData.DataSource.RESOURCE : initialDataSource;
        }

        public void changeColor(int newColor) {
            color = newColor & 0x00FFFFFF;
            dataSource = PreviewData.DataSource.CONFIG;
        }

        public void setCave(boolean cave) {
            isCave = cave;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("narrator.select", this.name);
        }

        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            guiGraphics.fill(k + 3, j + 1, k + 13, j + 11, nativeColor(color));
            String formatName = isPrimaryNamespace ? name : "§o" + name;
            guiGraphics.drawString(BiomesList.this.minecraft.font, formatName, k + 16, j + 2, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (i != 0) {
                return false;
            }

            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            boolean isSelected = getSelected() != null && id == getSelected().id;
            if (isSelected && allowDeselecting) {
                setSelected(null);
                return false;
            } else {
                return true;
            }
        }
    }
}
