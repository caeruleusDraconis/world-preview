package caeruleusTait.world.preview.client.gui.screens.settings;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.WorldPreviewConfig;
import caeruleusTait.world.preview.client.gui.widgets.SelectionSlider;
import caeruleusTait.world.preview.client.gui.widgets.WGCheckbox;
import caeruleusTait.world.preview.client.gui.widgets.WGLabel;
import net.minecraft.client.Minecraft;
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

        WGCheckbox cbBg = new WGCheckbox(0, 0, LINE_WIDTH, LINE_HEIGHT, SETTINGS_GENERAL_BG, x -> cfg.backgroundSampleVertChunk = x.selected(), cfg.backgroundSampleVertChunk);
        WGCheckbox cbFc = new WGCheckbox(0, 0, LINE_WIDTH, LINE_HEIGHT, SETTINGS_GENERAL_FC, x -> cfg.buildFullVertChunk = x.selected(), cfg.buildFullVertChunk);
        WGCheckbox cbStruct = new WGCheckbox(0, 0, LINE_WIDTH / 2 - 4, LINE_HEIGHT, SETTINGS_GENERAL_STRUCT, x -> cfg.sampleStructures = x.selected(), cfg.sampleStructures);
        WGCheckbox cbHm = new WGCheckbox(0, 0, LINE_WIDTH / 2 - 4, LINE_HEIGHT, SETTINGS_GENERAL_HEIGHTMAP, x -> cfg.sampleHeightmap = x.selected(), cfg.sampleHeightmap);
        WGCheckbox cbInt = new WGCheckbox(0, 0, LINE_WIDTH / 2 - 4, LINE_HEIGHT, SETTINGS_GENERAL_INTERSECT, x -> cfg.sampleIntersections = x.selected(), cfg.sampleIntersections);
        WGCheckbox cbCtrl = new WGCheckbox(0, 0, LINE_WIDTH / 2 - 4, LINE_HEIGHT, SETTINGS_GENERAL_CONTROLS, x -> cfg.showControls = x.selected(), cfg.showControls);
        WGCheckbox cbFt = new WGCheckbox(0, 0, LINE_WIDTH / 2 - 4, LINE_HEIGHT, SETTINGS_GENERAL_FRAMETIME, x -> cfg.showFrameTime = x.selected(), cfg.showFrameTime);
        WGCheckbox cbPause = new WGCheckbox(0, 0, LINE_WIDTH / 2 - 4, LINE_HEIGHT, SETTINGS_GENERAL_SHOW_IN_MENU, x -> cfg.showInPauseMenu = x.selected(), cfg.showInPauseMenu);
        WGCheckbox cbPlayer = new WGCheckbox(0, 0, LINE_WIDTH / 2 - 4, LINE_HEIGHT, SETTINGS_GENERAL_SHOW_PLAYER, x -> cfg.showPlayer = x.selected(), cfg.showPlayer);

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
