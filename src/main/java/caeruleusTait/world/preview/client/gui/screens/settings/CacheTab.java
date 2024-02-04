package caeruleusTait.world.preview.client.gui.screens.settings;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.WorldPreviewConfig;
import caeruleusTait.world.preview.backend.storage.PreviewStorageCacheManager;
import caeruleusTait.world.preview.client.gui.widgets.WGCheckbox;
import caeruleusTait.world.preview.client.gui.widgets.WGLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.*;
import static caeruleusTait.world.preview.client.gui.screens.PreviewContainer.LINE_HEIGHT;

public class CacheTab extends GridLayoutTab {

    private final PreviewStorageCacheManager cacheManager;

    public CacheTab(Minecraft minecraft, PreviewStorageCacheManager cacheManager) {
        super(SETTINGS_CACHE_TITLE);
        this.cacheManager = cacheManager;

        final WorldPreviewConfig cfg = WorldPreview.get().cfg();
        final int LINE_WIDTH = 320;

        WGCheckbox cbGameEnable = new WGCheckbox(0, 0, LINE_WIDTH, LINE_HEIGHT, SETTINGS_CACHE_G_ENABLE, x -> cfg.cacheInGame = x.selected(), cfg.cacheInGame);
        WGCheckbox cbNewEnable = new WGCheckbox(0, 0, LINE_WIDTH, LINE_HEIGHT, SETTINGS_CACHE_N_ENABLE, x -> cfg.cacheInNew = x.selected(), cfg.cacheInNew);

        Button btnClear = Button
                .builder(SETTINGS_CACHE_CLEAR, this::onClearCache)
                .tooltip(Tooltip.create(SETTINGS_CACHE_CLEAR_TOOLTIP))
                .width(LINE_WIDTH)
                .build();

        GridLayout.RowHelper rowHelper = layout.rowSpacing(8).createRowHelper(1);
        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, LINE_WIDTH, LINE_HEIGHT, WGLabel.TextAlignment.CENTER, SETTINGS_CACHE_DESC, 0xFFFFFF));
        rowHelper.addChild(cbGameEnable);
        rowHelper.addChild(cbNewEnable);
        rowHelper.addChild(new WGLabel(minecraft.font, 0, 0, LINE_WIDTH, LINE_HEIGHT, WGLabel.TextAlignment.CENTER, Component.empty(), 0xFFFFFF));
        rowHelper.addChild(btnClear);
    }

    private void onClearCache(Button btn) {
        cacheManager.clearCache();
    }
}
