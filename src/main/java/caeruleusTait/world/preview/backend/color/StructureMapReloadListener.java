package caeruleusTait.world.preview.backend.color;

import caeruleusTait.world.preview.WorldPreview;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;

public class StructureMapReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();

    public StructureMapReloadListener() {
        super(GSON, "structure_preview");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        final WorldPreview worldPreview = WorldPreview.get();
        final PreviewMappingData previewMappingData = worldPreview.biomeColorMap();
        previewMappingData.clearStructures();

        LOGGER.debug("Loading structure resource entries");
        for (var entry : object.entrySet()) {
            LOGGER.debug(" - loading entries from {}", entry.getKey());
            Map<ResourceLocation, PreviewMappingData.StructureEntry> curr = parseStructureData(entry.getValue());
            previewMappingData.updateStruct(curr, PreviewData.DataSource.RESOURCE);
        }
    }

    private Map<ResourceLocation, PreviewMappingData.StructureEntry> parseStructureData(JsonElement jsonElement) {
        final Map<ResourceLocation, PreviewMappingData.StructureEntry> res = new HashMap<>();
        final JsonObject obj = jsonElement.getAsJsonObject();

        for (var entry : obj.entrySet()) {
            final ResourceLocation location = new ResourceLocation(entry.getKey());
            final PreviewMappingData.StructureEntry value = GSON.fromJson(entry.getValue(), PreviewMappingData.StructureEntry.class);
            LOGGER.debug("   - {}: {} - {}", location, value.name, value.icon);
            res.put(location, value);
        }

        return res;
    }
}
