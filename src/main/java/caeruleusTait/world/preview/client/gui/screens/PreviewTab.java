package caeruleusTait.world.preview.client.gui.screens;

import caeruleusTait.world.preview.RenderSettings;
import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.backend.storage.PreviewStorage;
import caeruleusTait.world.preview.client.gui.PreviewContainerDataProvider;
import caeruleusTait.world.preview.mixin.MinecraftServerAccessor;
import caeruleusTait.world.preview.mixin.client.CreateWorldScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static caeruleusTait.world.preview.client.WorldPreviewComponents.*;

public class PreviewTab implements Tab, AutoCloseable, PreviewContainerDataProvider {

    private final CreateWorldScreen createWorldScreen;
    private final WorldCreationUiState uiState;
    private final PreviewContainer previewContainer;
    private final WorldPreview worldPreview = WorldPreview.get();
    private final Minecraft minecraft;

    private final Executor loadingExecutor = Executors.newFixedThreadPool(2);

    public PreviewTab(CreateWorldScreen screen, Minecraft _minecraft) {
        createWorldScreen = screen;
        minecraft = _minecraft;
        uiState = ((CreateWorldScreenAccessor) screen).getUiState();
        previewContainer = new PreviewContainer(screen, this);
    }

    @Override
    public @NotNull Component getTabTitle() {
        return TITLE;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        previewContainer.widgets().forEach(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        previewContainer.doLayout(screenRectangle);
    }

    @Override
    public void close() {
        previewContainer.close();
    }


    /**
     * Create a playground for mods to do their thing while minimizing the risk
     * to the real world creation stuff.
     */
    @Override
    public @Nullable WorldCreationContext previewWorldCreationContext() {
        WorldCreationContext wcContext = uiState.getSettings();
        WorldDataConfiguration worldDataConfiguration = wcContext.dataConfiguration();

        record Cookie(WorldGenSettings worldGenSettings) {}

        PackRepository packRepository = ((CreateWorldScreenAccessor) createWorldScreen).invokeGetDataPackSelectionSettings(worldDataConfiguration).getSecond();
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, true);
        WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 2);
        CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(
                initConfig,
                dataLoadContext -> {
                    ResourceKey<WorldPreset> worldPresetKey = uiState.getWorldType().preset().unwrapKey().orElseThrow();
                    WorldPreset worldPreset = dataLoadContext.datapackWorldgen().registryOrThrow(Registries.WORLD_PRESET).getOrThrow(worldPresetKey);
                    // WorldDimensions worldDimensions = WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen());
                    WorldDimensions worldDimensions = worldPreset.createWorldDimensions();
                    WorldGenSettings worldGenSettings = new WorldGenSettings(wcContext.options(), worldDimensions);
                    return new WorldLoader.DataLoadOutput<>(
                            new Cookie(worldGenSettings),
                            dataLoadContext.datapackDimensions()
                    );
                }
                ,
                (closeableResourceManager, reloadableServerResources, layeredRegistryAccess, cookie) -> {
                    closeableResourceManager.close();
                    return new WorldCreationContext(cookie.worldGenSettings, layeredRegistryAccess, reloadableServerResources, worldDataConfiguration);
                },
                loadingExecutor,
                loadingExecutor
        );

        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path cacheDir() {
        final Path previewDir = worldPreview.configDir().resolve("world-preview");
        previewDir.toFile().mkdirs();
        return previewDir;
    }

    private String filename(long seed) {
        return String.format("%s-%s.zip", seed, cacheFileCompatPart());
    }

    @Override
    public void storePreviewStorage(long seed, PreviewStorage storage) {
        if (!worldPreview.cfg().cacheInNew) {
            return;
        }
        minecraft.forceSetScreen(new PreviewCacheLoadingScreen(SAVING_PREVIEW));
        writeCacheFile(previewContainer.workManager().previewStorage(), cacheDir().resolve(filename(seed)));
        minecraft.forceSetScreen(createWorldScreen);
    }

    @Override
    public PreviewStorage loadPreviewStorage(long seed, int yMin, int yMax) {
        if (!worldPreview.cfg().cacheInNew) {
            return new PreviewStorage(yMin, yMax);
        }

        minecraft.forceSetScreen(new PreviewCacheLoadingScreen(LOADING_PREVIEW));
        final PreviewStorage res = readCacheFile(yMin, yMax, cacheDir().resolve(filename(seed)));
        minecraft.forceSetScreen(createWorldScreen);
        return res;
    }

    public PreviewContainer mainScreenWidget() {
        return previewContainer;
    }

    @Override
    public void registerSettingsChangeListener(Runnable listener) {
        uiState.addListener(x -> listener.run());
    }

    @Override
    public String seed() {
        return uiState.getSeed();
    }

    @Override
    public void updateSeed(String newSeed) {
        uiState.setSeed(newSeed);
    }

    @Override
    public boolean seedIsEditable() {
        return true;
    }

    @Override
    public @Nullable Path tempDataPackDir() {
        return ((CreateWorldScreenAccessor) createWorldScreen).invokeGetTempDataPackDir();
    }

    @Override
    public @Nullable MinecraftServer minecraftServer() {
        return null;
    }

    @Override
    public WorldOptions worldOptions(@Nullable WorldCreationContext wcContext) {
        if (wcContext == null) throw new AssertionError();
        return wcContext.options();
    }

    @Override
    public WorldDataConfiguration worldDataConfiguration(@Nullable WorldCreationContext wcContext) {
        if (wcContext == null) throw new AssertionError();
        return wcContext.dataConfiguration();
    }

    @Override
    public RegistryAccess.Frozen registryAccess(@Nullable WorldCreationContext wcContext) {
        if (wcContext == null) throw new AssertionError();
        return wcContext.worldgenLoadContext();
    }

    @Override
    public Registry<LevelStem> levelStemRegistry(@Nullable WorldCreationContext wcContext) {
        if (wcContext == null) throw new AssertionError();
        WorldDimensions.Complete worldDimensions = wcContext.selectedDimensions().bake(wcContext.datapackDimensions());
        return worldDimensions.dimensions();
    }

    @Override
    public LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess(@Nullable WorldCreationContext wcContext) {
        if (wcContext == null) throw new AssertionError();
        WorldDimensions.Complete worldDimensions = wcContext.selectedDimensions().bake(wcContext.datapackDimensions());
        return wcContext
                .worldgenRegistries()
                .replaceFrom(RegistryLayer.DIMENSIONS, worldDimensions.dimensionsRegistryAccess());
    }
}
