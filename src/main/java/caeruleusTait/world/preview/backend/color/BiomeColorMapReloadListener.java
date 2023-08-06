package caeruleusTait.world.preview.backend.color;

import caeruleusTait.world.preview.WorldPreview;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;

public class BiomeColorMapReloadListener extends BaseMultiJsonResourceReloadListener {
    public BiomeColorMapReloadListener() {
        super("biome_colors.json");
    }

    @Override
    protected void apply(Map<ResourceLocation, List<JsonElement>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        final WorldPreview worldPreview = WorldPreview.get();
        final PreviewMappingData previewMappingData = worldPreview.biomeColorMap();
        previewMappingData.clearBiomes();

        // Load from old location
        LOGGER.debug("Loading color resource entries");
        for (var entry : object.entrySet()) {
            LOGGER.debug(" - loading entries from {}", entry.getKey());
            for (JsonElement j : entry.getValue()) {
                Map<ResourceLocation, PreviewMappingData.ColorEntry> curr = parseColorData(
                        entry.getKey().getNamespace(),
                        j,
                        PreviewData.DataSource.RESOURCE
                );
                previewMappingData.update(curr);
            }
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
        Map<ResourceLocation, PreviewMappingData.ColorEntry> curr = parseColorData("", el, PreviewData.DataSource.CONFIG);
        previewMappingData.update(curr);

    }

    public static Map<ResourceLocation, PreviewMappingData.ColorEntry> parseColorData(String namespace, JsonElement jsonElement, PreviewData.DataSource dataSource) {
        final Map<ResourceLocation, PreviewMappingData.ColorEntry> res = new HashMap<>();
        final JsonObject obj = jsonElement.getAsJsonObject();

        for (var entry : obj.entrySet()) {
            final ResourceLocation location;
            if (entry.getKey().indexOf(':') < 0) {
                location = new ResourceLocation(namespace, entry.getKey());
            } else {
                location = new ResourceLocation(entry.getKey());
            }
            final PreviewMappingData.ColorEntry value = new PreviewMappingData.ColorEntry();
            final JsonElement rawEl = entry.getValue();

            value.dataSource = dataSource;

            try {
                JsonObject raw = rawEl.getAsJsonObject();
                JsonElement nameEl = raw.get("name");
                JsonElement colorEl = raw.get("color");
                JsonElement rEl = raw.get("r");
                JsonElement gEl = raw.get("g");
                JsonElement bEl = raw.get("b");
                value.name = nameEl == null ? null : nameEl.getAsString();
                if (colorEl != null) {
                    value.color = colorEl.getAsInt() & 0xFFFFFF;
                } else if (rEl != null && gEl != null && bEl != null) {
                    final int r = rEl.getAsInt() & 0xFF;
                    final int g = gEl.getAsInt() & 0xFF;
                    final int b = bEl.getAsInt() & 0xFF;
                    value.color = (r << 16) | (g << 8) | b;
                } else {
                    throw new IllegalStateException("No color was provided!");
                }
            } catch (IllegalStateException | UnsupportedOperationException | NullPointerException e) {
                LOGGER.warn("   - {}: Invalid color entry format: {}", location, e.getMessage());
                continue;
            }

            LOGGER.debug("   - {}: {}", location, String.format("0x%06X", (value.color & 0xFFFFFF)));
            res.put(location, value);
        }

        return res;
    }

}
