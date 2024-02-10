package caeruleusTait.world.preview;

import caeruleusTait.world.preview.backend.WorkManager;
import caeruleusTait.world.preview.backend.color.PreviewMappingData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WorldPreview implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("world_preview");

    private static WorldPreview INSTANCE;

    private Path configDir;
    private Path configFile;
    private Path renderConfigFile;
    private Path missingColorsFile;
    private Path missingStructuresFile;
    private Path userColorConfigFile;
    private Gson gson;

    private WorldPreviewConfig cfg;
    private WorkManager workManager;
    private PreviewMappingData previewMappingData;
    private RenderSettings renderSettings;

    public static WorldPreview get() {
        return INSTANCE;
    }

    @Override
    public void onInitialize() {
        INSTANCE = this;

        gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();

        configDir = FabricLoader.getInstance().getConfigDir().resolve("world_preview");
        if (!Files.exists(configDir)) {
            configDir.toFile().mkdirs();
        }
        configFile = configDir.resolve("config.json");
        renderConfigFile = configDir.resolve("renderConfig.json");
        missingColorsFile = configDir.resolve("missing-colors.json");
        missingStructuresFile = configDir.resolve("missing-structures.json");
        userColorConfigFile = configDir.resolve("biome-colors.json");

        loadConfig();

        workManager = new WorkManager(renderSettings, cfg);
        previewMappingData = new PreviewMappingData();
    }

    public Executor serverThreadPoolExecutor() {
        // Nothing to do on fabric
        return null;
    }

    public void loaderSpecificSetup(MinecraftServer minecraftServer) {
        // Nothing to do on fabric
        ServerLifecycleEvents.SERVER_STARTING.invoker().onServerStarting(minecraftServer);
    }

    public void loaderSpecificTeardown(MinecraftServer minecraftServer) {
        // Nothing to do for fabric
    }

    public WorldPreviewConfig cfg() {
        return cfg;
    }

    public WorkManager workManager() {
        return workManager;
    }

    public PreviewMappingData biomeColorMap() {
        return previewMappingData;
    }

    public RenderSettings renderSettings() {
        return renderSettings;
    }

    public Path userColorConfigFile() {
        return userColorConfigFile;
    }

    public Path configDir() {
        return configDir;
    }

    public void loadConfig() {
        LOGGER.info("Loading config file: {}", configFile);
        try {
            if (!Files.exists(configFile)) {
                cfg = new WorldPreviewConfig();
            } else {
                cfg = gson.fromJson(Files.readString(configFile), WorldPreviewConfig.class);
            }

            if (!Files.exists(renderConfigFile)) {
                renderSettings = new RenderSettings();
            } else {
                renderSettings = gson.fromJson(Files.readString(renderConfigFile), RenderSettings.class);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveConfig() {
        LOGGER.info("Saving config file: {}", configFile);
        try {
            Files.writeString(configFile, gson.toJson(cfg) + "\n");
            Files.writeString(renderConfigFile, gson.toJson(renderSettings) + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeMissingColors(List<String> missing) {
        try {
            Files.deleteIfExists(missingColorsFile);
            if (missing.isEmpty()) {
                return;
            }
            LOGGER.warn("No color mapping for {} biomes found. The list of biomes without a color mapping can be found in {}", missing.size(), missingColorsFile);
            final String raw = gson.toJson(missing);
            Files.writeString(missingColorsFile, raw + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeMissingStructures(List<String> missing) {
        try {
            Files.deleteIfExists(missingStructuresFile);
            if (missing.isEmpty()) {
                return;
            }
            LOGGER.warn("No structure data for {} structure found. The list of structures without data can be found in {}", missing.size(), missingStructuresFile);
            final String raw = gson.toJson(missing);
            Files.writeString(missingStructuresFile, raw + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeUserColorConfig(Map<ResourceLocation, PreviewMappingData.ColorEntry> userColorConfig) {
        record Entry(int r, int g, int b, boolean cave) {}
        Map<String, Entry> writeData = userColorConfig.entrySet()
                .stream()
                .collect(Collectors.toMap(x -> x.getKey().toString(), x -> {
                    PreviewMappingData.ColorEntry raw = x.getValue();
                    final int r = (raw.color >> 16) & 0xFF;
                    final int g = (raw.color >> 8) & 0xFF;
                    final int b = raw.color & 0xFF;
                    return new Entry(r, g, b, raw.cave.orElseThrow());
                }));

        final String raw = gson.toJson(writeData);
        try {
            Files.writeString(userColorConfigFile, raw + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int nativeColor(int orig) {
        /*
        final int R = (orig >> 16) & 0xFF;
        final int G = (orig >> 8) & 0xFF;
        final int B = (orig >> 0) & 0xFF;
        return (R << 16) | (G << 8) | (B << 0) | (0xFF << 24);
         */
        return orig | (0xFF << 24);
    }
}