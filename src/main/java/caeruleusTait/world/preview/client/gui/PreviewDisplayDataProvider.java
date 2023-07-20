package caeruleusTait.world.preview.client.gui;

import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.client.gui.widgets.lists.BiomesList;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.shorts.Short2LongMap;

public interface PreviewDisplayDataProvider {
    PreviewData previewData();

    BiomesList.BiomeEntry biome4Id(int id);

    NativeImage[] structureIcons();

    void onBiomeVisuallySelected(BiomesList.BiomeEntry entry);

    void onVisibleBiomesChanged(Short2LongMap visibleBiomes);

    void onVisibleStructuresChanged(Short2LongMap visibleStructures);

    StructureRenderInfo[] renderStructureMap();

    int[] heightColorMap();

    int yMin();

    int yMax();

    boolean isUpdating();

    interface StructureRenderInfo {
        boolean show();
    }
}
