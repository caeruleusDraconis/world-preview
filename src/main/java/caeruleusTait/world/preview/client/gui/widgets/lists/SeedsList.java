package caeruleusTait.world.preview.client.gui.widgets.lists;

import caeruleusTait.world.preview.client.gui.screens.PreviewTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import static caeruleusTait.world.preview.client.gui.screens.PreviewTab.*;

public class SeedsList extends BaseObjectSelectionList<SeedsList.SeedEntry> {
    private final PreviewTab previewTab;

    public SeedsList(Minecraft minecraft, PreviewTab previewTab, int width, int height, int x, int y) {
        super(minecraft, width, height, x, y, 24);
        this.previewTab = previewTab;
    }

    public SeedEntry createEntry(String seed) {
        return new SeedEntry(this, seed);
    }

    public class SeedEntry extends BaseObjectSelectionList.Entry<SeedEntry> {
        public final SeedsList seedsList;
        public final String seed;
        public final Button deleteButton;

        public SeedEntry(SeedsList seedsList, String seed) {
            this.seedsList = seedsList;
            this.seed = seed;
            this.deleteButton = new ImageButton(
                    0, 0, 20, 20, /* x, y, width, height */
                    40, 20, 20, /* xTexStart, yTexStart, yDiffTex */
                    BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                    this::deleteEntry
            );
        }

        private void deleteEntry(Button btn) {
            seedsList.previewTab.deleteSeed(seed);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("narrator.select", this.seed);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean bl, float partialTick) {
            guiGraphics.drawString(seedsList.minecraft.font, seed, left + 4, top + 6, 0xFFFFFF);
            deleteButton.setPosition(seedsList.getRowRight() - 22, top);
            deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (deleteButton.isHovered()) {
                deleteButton.onClick(d, e);
            }
            if (i == 0 && d < seedsList.getRowRight() - 22) {
                seedsList.setSelected(this);
                seedsList.previewTab.setSeed(seed);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else {
                return false;
            }
        }
    }
}
