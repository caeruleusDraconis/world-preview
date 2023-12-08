package caeruleusTait.world.preview.client.gui.screens.settings;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.WorldPreviewConfig;
import caeruleusTait.world.preview.client.gui.widgets.SelectionSlider;
import caeruleusTait.world.preview.client.gui.widgets.WGLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.*;
import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.LINE_HEIGHT;

public class GeneralTab extends GridLayoutTab {
    public GeneralTab(Minecraft minecraft) {
        super(SETTINGS_GENERAL_TITLE);


        final WorldPreviewConfig cfg = WorldPreview.get().cfg();
        final int LINE_WIDTH = 320;

        List<ThreadCount> threadCounts = new ArrayList<>(Runtime.getRuntime().availableProcessors());
        for (int i = 1; i <= Runtime.getRuntime().availableProcessors(); ++i) {
            threadCounts.add(new ThreadCount(i));
        }
        SelectionSlider<ThreadCount> threadsSlider = new SelectionSlider<>(
                0, 0,
                LINE_WIDTH, LINE_HEIGHT,
                threadCounts,
                threadCounts.get(cfg.numThreads() - 1),
                x -> cfg.setNumThreads(x.value)
        );

        Checkbox cbBg     = Checkbox.builder(SETTINGS_GENERAL_BG,           minecraft.font).selected(cfg.backgroundSampleVertChunk).onValueChange((box, val) -> cfg.backgroundSampleVertChunk = val).build();
        Checkbox cbFc     = Checkbox.builder(SETTINGS_GENERAL_FC,           minecraft.font).selected(cfg.buildFullVertChunk       ).onValueChange((box, val) -> cfg.buildFullVertChunk        = val).build();
        Checkbox cbStruct = Checkbox.builder(SETTINGS_GENERAL_STRUCT,       minecraft.font).selected(cfg.sampleStructures         ).onValueChange((box, val) -> cfg.sampleStructures          = val).build();
        Checkbox cbHm     = Checkbox.builder(SETTINGS_GENERAL_HEIGHTMAP,    minecraft.font).selected(cfg.sampleHeightmap          ).onValueChange((box, val) -> cfg.sampleHeightmap           = val).build();
        Checkbox cbInt    = Checkbox.builder(SETTINGS_GENERAL_INTERSECT,    minecraft.font).selected(cfg.sampleIntersections      ).onValueChange((box, val) -> cfg.sampleIntersections       = val).build();
        Checkbox cbCtrl   = Checkbox.builder(SETTINGS_GENERAL_CONTROLS,     minecraft.font).selected(cfg.showControls             ).onValueChange((box, val) -> cfg.showControls              = val).build();
        Checkbox cbFt     = Checkbox.builder(SETTINGS_GENERAL_FRAMETIME,    minecraft.font).selected(cfg.showFrameTime            ).onValueChange((box, val) -> cfg.showFrameTime             = val).build();
        Checkbox cbPause  = Checkbox.builder(SETTINGS_GENERAL_SHOW_IN_MENU, minecraft.font).selected(cfg.showInPauseMenu          ).onValueChange((box, val) -> cfg.showInPauseMenu           = val).build();
        Checkbox cbPlayer = Checkbox.builder(SETTINGS_GENERAL_SHOW_PLAYER,  minecraft.font).selected(cfg.showPlayer               ).onValueChange((box, val) -> cfg.showPlayer                = val).build();

        threadsSlider.setTooltip(Tooltip.create(SETTINGS_GENERAL_THREADS_TOOLTIP));
        cbFc.setTooltip(Tooltip.create(SETTINGS_GENERAL_FC_TOOLTIP));
        cbBg.setTooltip(Tooltip.create(SETTINGS_GENERAL_BG_TOOLTIP));
        cbStruct.setTooltip(Tooltip.create(SETTINGS_GENERAL_STRUCT_TOOLTIP));
        cbHm.setTooltip(Tooltip.create(SETTINGS_GENERAL_HEIGHTMAP_TOOLTIP));
        cbInt.setTooltip(Tooltip.create(SETTINGS_GENERAL_INTERSECT_TOOLTIP));
        cbCtrl.setTooltip(Tooltip.create(SETTINGS_GENERAL_CONTROLS_TOOLTIP));
        cbFt.setTooltip(Tooltip.create(SETTINGS_GENERAL_FRAMETIME_TOOLTIP));
        cbPause.setTooltip(Tooltip.create(SETTINGS_GENERAL_SHOW_IN_MENU_TOOLTIP));
        cbPlayer.setTooltip(Tooltip.create(SETTINGS_GENERAL_SHOW_PLAYER_TOOLTIP));

        GridLayout.RowHelper rowHelper = layout.rowSpacing(4).createRowHelper(2);
        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, LINE_WIDTH, LINE_HEIGHT, WGLabel.TextAlignment.CENTER, SETTINGS_GENERAL_HEAD, 0xFFFFFF), 2);
        rowHelper.addChild(threadsSlider, 2);
        rowHelper.addChild(cbFc, 2);
        rowHelper.addChild(cbBg, 2);
        rowHelper.addChild(cbStruct, 1);
        rowHelper.addChild(cbHm, 1);
        rowHelper.addChild(cbInt, 1);
        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, 200, LINE_HEIGHT / 10, WGLabel.TextAlignment.CENTER, Component.literal(""), 0xFFFFFF), 2);
        rowHelper.addChild(cbCtrl);
        rowHelper.addChild(cbFt);
        rowHelper.addChild(cbPause);
        rowHelper.addChild(cbPlayer);
    }

    public static class ThreadCount implements SelectionSlider.SelectionValues {
        public final int value;

        public ThreadCount(int value) {
            this.value = value;
        }

        @Override
        public Component message() {
            return Component.translatable("world_preview.settings.general.threads", value);
        }
    }
}
