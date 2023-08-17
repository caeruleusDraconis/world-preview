package caeruleusTait.world.preview.client.gui.widgets.lists;

import caeruleusTait.world.preview.client.gui.screens.PreviewContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;

import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.*;

public class SeedsList extends BaseObjectSelectionList<SeedsList.SeedEntry> {
    private final PreviewContainer previewContainer;
    private final boolean seedCanChange;

    public SeedsList(Minecraft minecraft, PreviewContainer previewContainer) {
        super(minecraft, 100, 100, 0, 0, 24);
        this.previewContainer = previewContainer;
        this.seedCanChange = this.previewContainer.dataProvider().seedIsEditable();
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
            this.deleteButton.active = seedCanChange;
        }

        private void deleteEntry(Button btn) {
            seedsList.previewContainer.deleteSeed(seed);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.translatable("narrator.select", this.seed);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean bl, float partialTick) {
            guiGraphics.drawString(seedsList.minecraft.font, seed, left + 4, top + 6, seedCanChange ? 0xFFFFFF : 0x999999);
            deleteButton.setPosition(seedsList.getRowRight() - 22, top);
            deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double d, double e, int i) {
            if (!seedCanChange) {
                return true;
            }
            if (deleteButton.isHovered()) {
                deleteButton.mouseClicked(d, e, i);
            }
            if (i == 0 && d < seedsList.getRowRight() - 22) {
                seedsList.setSelected(this);
                seedsList.previewContainer.setSeed(seed);
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else {
                return false;
            }
        }
    }
}
