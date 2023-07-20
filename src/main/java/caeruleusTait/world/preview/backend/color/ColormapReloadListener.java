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

public class ColormapReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();

    public ColormapReloadListener() {
        super(GSON, "colormap_preview");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        final WorldPreview worldPreview = WorldPreview.get();
        final PreviewMappingData previewMappingData = worldPreview.biomeColorMap();
        previewMappingData.clearColorMappings();

        LOGGER.debug("Loading colormaps:");
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            final ColorMap.RawColorMap value = GSON.fromJson(entry.getValue(), ColorMap.RawColorMap.class);
            LOGGER.debug(" - {}: {} | {} entries", entry.getKey(), value.name(), value.data().size());
            previewMappingData.addColormap(new ColorMap(entry.getKey(), value));
        }
    }
}
