package caeruleusTait.world.preview.client.gui.widgets.lists;

import caeruleusTait.world.preview.client.WorldPreviewClient;
import caeruleusTait.world.preview.client.gui.widgets.ToggleButton;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import static caeruleusTait.world.preview.client.gui.screens.PreviewTab.*;

public class StructuresList extends BaseObjectSelectionList<StructuresList.StructureEntry> {

    public StructuresList(Minecraft minecraft, int width, int height, int x, int y) {
        super(minecraft, width, height, x, y, 24);
    }

    public StructureEntry createEntry(short id, ResourceLocation resourceLocation, NativeImage icon, Item item, String name, boolean show, boolean showByDefault) {
        return new StructureEntry(id, resourceLocation, icon, item, name, show, showByDefault);
    }

    @Override
    public void replaceEntries(Collection<StructureEntry> entryList) {
        super.replaceEntries(entryList);

        // If we have more than one page, make sure we don't let the scrollbar run away
        double maxScroll = Math.max(0.0, super.getItemCount() * super.itemHeight - super.height);
        if(super.getScrollAmount() > maxScroll) {
            // Make sure that the top entry is visible
            super.setScrollAmount(maxScroll);
        }
    }

    public class StructureEntry extends BaseObjectSelectionList.Entry<StructuresList.StructureEntry> implements StructureRenderInfo {
        private final short id;
        private final NativeImage icon;
        private final Item item;
        private final ItemStack itemStack;
        private final DynamicTexture iconTexture;
        private final int iconWidth;
        private final int iconHeight;
        private final String name;
        private final Tooltip tooltip;
        private final boolean showByDefault;
        private final boolean isPrimaryNamespace;

        private boolean show;
        public final ToggleButton toggleVisible;

        public StructureEntry(short id, ResourceLocation resourceLocation, @NotNull NativeImage icon, @Nullable Item item, String name, boolean show, boolean showByDefault) {
            this.id = id;
            this.item = item;
            this.itemStack = this.item == null ? null : new ItemStack(this.item, 1);
            this.icon = icon;
            this.iconTexture = new DynamicTexture(this.icon);
            this.iconWidth = this.icon.getWidth();
            this.iconHeight = this.icon.getHeight();
            this.showByDefault = showByDefault;
            this.show = show;
            this.toggleVisible = new ToggleButton(
                    0, 0, 20, 20, /* x, y, width, height */
                    140, 20, 20, 20, /* xTexStart, yTexStart, xDiffTex, yDiffTex */
                    BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                    this::toggleVisible
            );

            this.iconTexture.upload();
            this.toggleVisible.selected = show;

            this.isPrimaryNamespace = resourceLocation.getNamespace().equals("minecraft");
            if (Objects.equals(resourceLocation.toString(), name) || name == null) {
                this.name = WorldPreviewClient.toTitleCase(resourceLocation.getPath().replace("_", " "));
            } else {
                this.name = name;
            }

            String tag = "§5§o" + resourceLocation.getNamespace() + "§r\n§9" + resourceLocation.getPath() + "§r";
            this.tooltip = Tooltip.create(Component.literal(this.name + "\n\n" + tag));
        }

        public void reset() {
            show = showByDefault;
            toggleVisible.selected = show;
        }

        private void toggleVisible(Button btn) {
            show = toggleVisible.selected;
        }

        public void setVisible(boolean show) {
            this.show = show;
        }

        @Override
        public Tooltip tooltip() {
            return tooltip;
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
            final int xMin = left + 2;
            final int yMin = top + 2;
            final int xMax = xMin + iconWidth;
            final int yMax = yMin + iconHeight;

            if (item != null) {
                guiGraphics.renderItem(itemStack, xMin, yMin);
            } else {
                WorldPreviewClient.renderTexture(iconTexture, xMin, yMin, xMax, yMax);
            }
            String formatName = isPrimaryNamespace ? name : "§o" + name;
            guiGraphics.drawString(minecraft.font, formatName, left + 16 + 4, top + 6, 0xFFFFFF);
            toggleVisible.setPosition(getRowRight() - 22, top);
            toggleVisible.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (toggleVisible.isMouseOver(mouseX, mouseY)) {
                toggleVisible.onClick(mouseX, mouseY);
            }
            return true;
        }

        public String name() {
            return name;
        }

        public boolean showByDefault() {
            return showByDefault;
        }

        public boolean show() {
            return show;
        }

        public short id() {
            return id;
        }

        public Item item() {
            return item;
        }

        public ItemStack itemStack() {
            return itemStack;
        }
    }

}
