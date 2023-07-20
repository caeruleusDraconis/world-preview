package caeruleusTait.world.preview.backend.color;

import caeruleusTait.world.preview.WorldPreview;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;

public class HeightmapPresetReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();

    public HeightmapPresetReloadListener() {
        super(GSON, "heightmap_preview_presets");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        final WorldPreview worldPreview = WorldPreview.get();
        final PreviewMappingData previewMappingData = worldPreview.biomeColorMap();
        previewMappingData.clearHeightmapPresets();

        LOGGER.debug("Loading heightmap presets:");
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            final PreviewData.HeightmapPresetData value = GSON.fromJson(entry.getValue(), PreviewData.HeightmapPresetData.class);
            LOGGER.debug(" - {}: {} | {} to {}", entry.getKey(), value.name(), value.minY(), value.maxY());
            previewMappingData.addHeightmapPreset(value);
        }
    }
}
