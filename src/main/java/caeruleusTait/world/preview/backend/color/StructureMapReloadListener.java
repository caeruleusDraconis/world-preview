package caeruleusTait.world.preview.backend.color;

import caeruleusTait.world.preview.WorldPreview;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;

public class StructureMapReloadListener extends BaseMultiJsonResourceReloadListener {
    public StructureMapReloadListener() {
        super("structure_icons.json");
    }

    @Override
    protected void apply(Map<ResourceLocation, List<JsonElement>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        final WorldPreview worldPreview = WorldPreview.get();
        final PreviewMappingData previewMappingData = worldPreview.biomeColorMap();
        previewMappingData.clearStructures();

        LOGGER.debug("Loading structure resource entries");
        for (var entry : object.entrySet()) {
            LOGGER.debug(" - loading entries from {}", entry.getKey());
            for (JsonElement jsonElement : entry.getValue()) {
                Map<ResourceLocation, PreviewMappingData.StructureEntry> curr = parseStructureData(
                        entry.getKey().getNamespace(),
                        jsonElement,
                        PreviewData.DataSource.RESOURCE
                );
                previewMappingData.updateStruct(curr);
            }
        }
    }

    public static Map<ResourceLocation, PreviewMappingData.StructureEntry> parseStructureData(String namespace, JsonElement jsonElement, PreviewData.DataSource dataSource) {
        final Map<ResourceLocation, PreviewMappingData.StructureEntry> res = new HashMap<>();
        final JsonObject obj = jsonElement.getAsJsonObject();

        for (var entry : obj.entrySet()) {
            final ResourceLocation location;
            if (entry.getKey().indexOf(':') < 0) {
                location = new ResourceLocation(namespace, entry.getKey());
            } else {
                location = new ResourceLocation(entry.getKey());
            }
            final PreviewMappingData.StructureEntry value = new PreviewMappingData.StructureEntry();
            final JsonElement rawEl = entry.getValue();

            value.dataSource = dataSource;

            try {
                if (rawEl.isJsonPrimitive()) {
                    if (rawEl.getAsString().equals("hidden")) {
                        continue;
                    }
                    value.item = rawEl.getAsString();
                } else {
                    JsonObject raw = rawEl.getAsJsonObject();
                    JsonElement nameEl = raw.get("name");
                    JsonElement itemEl = raw.get("item");
                    JsonElement iconEl = raw.get("icon");
                    JsonElement textureEl = raw.get("texture");
                    value.name = nameEl == null ? null : nameEl.getAsString();
                    if (textureEl == null) {
                        textureEl = iconEl;
                    }
                    if (textureEl != null) {
                        value.texture = textureEl.getAsString();
                    } else if (itemEl != null) {
                        if (itemEl.getAsString().equals("hidden")) {
                            continue;
                        }
                        value.item = itemEl.getAsString();
                    } else {
                        value.texture = "world_preview:textures/structure/unknown.png";
                    }
                }
            } catch (IllegalStateException | UnsupportedOperationException | NullPointerException e) {
                LOGGER.warn("   - {}: Invalid structure entry format: {}", location, e.getMessage());
                continue;
            }

            LOGGER.debug("   - {}: {} - {}", location, value.name, value.texture);
            res.put(location, value);
        }

        return res;
    }
}
