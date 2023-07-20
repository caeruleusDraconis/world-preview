package caeruleusTait.world.preview.backend.color;

import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

@SuppressWarnings("java:S6218")
public record PreviewData(
        BiomeData[] biomeId2BiomeData,
        StructureData[] structId2StructData,
        Object2ShortMap<String> biome2Id,
        Object2ShortMap<String> struct2Id,
        List<HeightmapPresetData> heightmapPresets,
        Map<String, ColorMap> colorMaps
) {
    public enum DataSource {
        MISSING,
        RESOURCE,
        CONFIG,
    }

    public record BiomeData(int id, ResourceLocation tag, int color, int resourceOnlyColor, boolean isCave, boolean resourceOnlyIsCave, String name, String resourceOnlyName, DataSource dataSource) {
    }

    public record StructureData(int id, ResourceLocation tag, String name, ResourceLocation icon, boolean showByDefault, DataSource dataSource) {
    }

    public record HeightmapPresetData(String name, int minY, int maxY) {
    }
}
