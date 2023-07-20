package caeruleusTait.world.preview.client.gui.screens.settings;

import caeruleusTait.world.preview.RenderSettings;
import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.client.gui.widgets.SelectionSlider;
import caeruleusTait.world.preview.client.gui.widgets.WGLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

import java.util.List;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.*;
import static caeruleusTait.world.preview.client.gui.screens.PreviewTab.LINE_HEIGHT;

public class SamplingTab extends GridLayoutTab {
    public SamplingTab(Minecraft minecraft) {
        super(SETTINGS_SAMPLE_TITLE);
        GridLayout.RowHelper rowHelper = layout.rowSpacing(8).createRowHelper(1);

        final RenderSettings renderSettings = WorldPreview.get().renderSettings();
        final int LINE_WIDTH = 320;

        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, LINE_WIDTH, LINE_HEIGHT, WGLabel.TextAlignment.CENTER, SETTINGS_SAMPLE_HEAD, 0xFFFFFF));

        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, LINE_WIDTH, LINE_HEIGHT / 2, WGLabel.TextAlignment.CENTER, SETTINGS_SAMPLE_PIXELS_TITLE_1, 0xCCCCCC));
        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, LINE_WIDTH, LINE_HEIGHT / 2, WGLabel.TextAlignment.CENTER, SETTINGS_SAMPLE_PIXELS_TITLE_2, 0xCCCCCC));
        rowHelper.addChild(new SelectionSlider<>(
                0, 0,
                LINE_WIDTH, LINE_HEIGHT,
                List.of(PixelsPerChunk.NUM_1, PixelsPerChunk.NUM_2, PixelsPerChunk.NUM_4, PixelsPerChunk.NUM_8, PixelsPerChunk.NUM_16),
                PixelsPerChunk.of(renderSettings.pixelsPerChunk()),
                x -> renderSettings.setPixelsPerChunk(x.value)
        ));
        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, 200, LINE_HEIGHT / 2, WGLabel.TextAlignment.CENTER, Component.literal(""), 0xFFFFFF));

        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, LINE_WIDTH, LINE_HEIGHT / 2, WGLabel.TextAlignment.CENTER, SETTINGS_SAMPLE_SAMPLE_TITLE_1, 0xCCCCCC));
        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, LINE_WIDTH, LINE_HEIGHT / 2, WGLabel.TextAlignment.CENTER, SETTINGS_SAMPLE_SAMPLE_TITLE_2, 0xCCCCCC));
        rowHelper.addChild(new SelectionSlider<>(
                0, 0,
                LINE_WIDTH, LINE_HEIGHT,
                List.of(GUISamplesType.AUTO, GUISamplesType.FULL, GUISamplesType.QUARTER, GUISamplesType.SINGLE),
                GUISamplesType.of(renderSettings.samplerType),
                x -> renderSettings.samplerType = x.samplerType
        ));
    }

    public enum PixelsPerChunk implements SelectionSlider.SelectionValues {
        NUM_16(16),
        NUM_8(8),
        NUM_4(4),
        NUM_2(2),
        NUM_1(1),

        ;

        public final int value;

        PixelsPerChunk(int value) {
            this.value = value;
        }

        public static PixelsPerChunk of(int num) {
            return PixelsPerChunk.valueOf("NUM_" + num);
        }

        @Override
        public Component message() {
            return Component.translatable("world_preview.settings.sample.numChunk.name." + name());
        }
    }

    public enum GUISamplesType implements SelectionSlider.SelectionValues {
        AUTO(RenderSettings.SamplerType.AUTO),
        FULL(RenderSettings.SamplerType.FULL),
        QUARTER(RenderSettings.SamplerType.QUARTER),
        SINGLE(RenderSettings.SamplerType.SINGLE),

        ;

        public final RenderSettings.SamplerType samplerType;

        public static GUISamplesType of(RenderSettings.SamplerType typ) {
            return GUISamplesType.valueOf(typ.name());
        }

        @Override
        public Component message() {
            return Component.translatable("world_preview.settings.sample.sampler.name." + name());
        }

        GUISamplesType(RenderSettings.SamplerType samplerType) {
            this.samplerType = samplerType;
        }
    }
}
