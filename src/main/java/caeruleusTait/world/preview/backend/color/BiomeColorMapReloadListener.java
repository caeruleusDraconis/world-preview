package caeruleusTait.world.preview.backend.color;

import caeruleusTait.world.preview.WorldPreview;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;

public class BiomeColorMapReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();

    public BiomeColorMapReloadListener() {
        super(GSON, "biome_preview");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        final WorldPreview worldPreview = WorldPreview.get();
        final PreviewMappingData previewMappingData = worldPreview.biomeColorMap();
        previewMappingData.clearBiomes();

        LOGGER.debug("Loading color resource entries");
        for (var entry : object.entrySet()) {
            LOGGER.debug(" - loading entries from {}", entry.getKey());
            Map<ResourceLocation, PreviewMappingData.ColorEntry> curr = parseColorData(entry.getValue());
            previewMappingData.update(curr, PreviewData.DataSource.RESOURCE);
        }

        // load user config
        if (!Files.exists(worldPreview.userColorConfigFile())) {
            return;
        }

        // Ensure that we don't lose the resource pack colors
        previewMappingData.makeBiomeResourceOnlyBackup();

        LOGGER.debug(" - loading entries from {}", worldPreview.userColorConfigFile());
        JsonElement el = null;
        try {
            el = JsonParser.parseString(Files.readString(worldPreview.userColorConfigFile()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<ResourceLocation, PreviewMappingData.ColorEntry> curr = parseColorData(el);
        previewMappingData.update(curr, PreviewData.DataSource.CONFIG);

    }

    private Map<ResourceLocation, PreviewMappingData.ColorEntry> parseColorData(JsonElement jsonElement) {
        final Map<ResourceLocation, PreviewMappingData.ColorEntry> res = new HashMap<>();
        final JsonObject obj = jsonElement.getAsJsonObject();

        for (var entry : obj.entrySet()) {
            final ResourceLocation location = new ResourceLocation(entry.getKey());
            final PreviewMappingData.ColorEntry value = GSON.fromJson(entry.getValue(), PreviewMappingData.ColorEntry.class);
            LOGGER.debug("   - {}: {}", location, String.format("0x%06X", (value.color & 0xFFFFFF)));
            res.put(location, value);
        }

        return res;
    }
}
