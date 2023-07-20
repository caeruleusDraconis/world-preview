package caeruleusTait.world.preview.client.gui.screens;

import caeruleusTait.world.preview.RenderSettings;
import caeruleusTait.world.preview.backend.WorkManager;
import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.WorldPreviewConfig;
import caeruleusTait.world.preview.backend.color.ColorMap;
import caeruleusTait.world.preview.client.gui.PreviewDisplayDataProvider;
import caeruleusTait.world.preview.client.gui.widgets.PreviewDisplay;
import caeruleusTait.world.preview.client.gui.widgets.ToggleButton;
import caeruleusTait.world.preview.client.gui.widgets.lists.AbstractSelectionListHolder;
import caeruleusTait.world.preview.client.gui.widgets.lists.BiomesList;
import caeruleusTait.world.preview.client.gui.widgets.lists.SeedsList;
import caeruleusTait.world.preview.client.gui.widgets.lists.StructuresList;
import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.color.PreviewMappingData;
import caeruleusTait.world.preview.mixin.client.CreateWorldScreenAccessor;
import caeruleusTait.world.preview.mixin.client.ScreenAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.shorts.Short2LongMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;
import static caeruleusTait.world.preview.client.WorldPreviewComponents.*;

public class PreviewTab implements Tab, AutoCloseable, PreviewDisplayDataProvider {

    public static final ResourceLocation BUTTONS_TEXTURE = new ResourceLocation("world_preview:textures/gui/buttons.png");
    public static final int BUTTONS_TEX_WIDTH = 320;
    public static final int BUTTONS_TEX_HEIGHT = 60;

    public static final int LINE_HEIGHT = 20;
    public static final int LINE_VSPACE = 4;

    private final Minecraft minecraft;
    private final CreateWorldScreen createWorldScreen;
    private final WorldCreationUiState uiState;
    private final WorldPreview worldPreview;
    private final WorldPreviewConfig cfg;
    private final WorkManager workManager;
    private final RenderSettings renderSettings;
    private final PreviewMappingData previewMappingData;
    private PreviewData previewData;

    private List<ResourceLocation> levelStemKeys;
    private Registry<LevelStem> levelStemRegistry;

    private final EditBox seedEdit;
    private final Button randomSeedButton;
    private final Button saveSeed;
    private final Button settings;
    private final Button resetToZeroZero;
    private final ToggleButton toggleCaves;
    private final ToggleButton toggleShowStructures;
    private final ToggleButton toggleHeightmap;
    private final Button resetDefaultStructureVisibility;
    private final Button switchBiomes;
    private final Button switchStructures;
    private final Button switchSeeds;
    private final PreviewDisplay previewDisplay;
    private final BiomesList biomesList;
    private final StructuresList structuresList;
    private final SeedsList seedsList;
    private final AbstractSelectionListHolder<BiomesList.BiomeEntry, BiomesList> biomesListHolder;
    private final AbstractSelectionListHolder<StructuresList.StructureEntry, StructuresList> structuresListHolder;
    private final AbstractSelectionListHolder<SeedsList.SeedEntry, SeedsList> seedsListHolder;
    private BiomesList.BiomeEntry[] allBiomes;
    private StructuresList.StructureEntry[] allStructures;
    private NativeImage[] allStructureIcons;
    private List<SeedsList.SeedEntry> seedEntries;

    private boolean inhibitUpdates = true;
    private boolean isUpdating = false;
    private final Executor loadingExecutor = Executors.newFixedThreadPool(2);
    private final Executor reloadExecutor = Executors.newSingleThreadExecutor();
    private final AtomicInteger reloadRevision = new AtomicInteger(0);

    private final List<AbstractWidget> toRender = new ArrayList<>();

    public PreviewTab(CreateWorldScreen screen) {
        final Font font = ((ScreenAccessor) screen).getFont();
        minecraft = ((ScreenAccessor) screen).getMinecraft();
        createWorldScreen = screen;
        allBiomes = new BiomesList.BiomeEntry[0];
        uiState = ((CreateWorldScreenAccessor) screen).getUiState();
        worldPreview = WorldPreview.get();
        cfg = worldPreview.cfg();
        workManager = worldPreview.workManager();
        previewMappingData = worldPreview.biomeColorMap();
        renderSettings = worldPreview.renderSettings();

        seedEdit = new EditBox(font, 0, 0, 100, LINE_HEIGHT - 2, SEED_FIELD);
        seedEdit.setHint(SEED_FIELD);
        seedEdit.setValue(uiState.getSeed());
        seedEdit.setResponder(this::setSeed);
        seedEdit.setTooltip(Tooltip.create(SEED_LABEL));
        toRender.add(seedEdit);

        // seedLabel = new WGLabel(font, 0, 0, 100, LINE_HEIGHT, WGLabel.TextAlignment.LEFT, SEED_LABEL, 0xFFFFFF);
        // toRender.add(seedLabel);

        randomSeedButton = new ImageButton(
                0, 0, 20, 20, /* x, y, width, height */
                0, 20, 20, /* xTexStart, yTexStart, yDiffTex */
                BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                this::randomizeSeed
        );
        randomSeedButton.setTooltip(Tooltip.create(BTN_RANDOM));
        toRender.add(randomSeedButton);

        saveSeed = new ImageButton(
                0, 0, 20, 20, /* x, y, width, height */
                20, 20, 20, /* xTexStart, yTexStart, yDiffTex */
                BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                this::saveCurrentSeed
        );
        saveSeed.setTooltip(Tooltip.create(BTN_SAVE_SEED));
        saveSeed.active = false;
        toRender.add(saveSeed);

        settings = new ImageButton(
                0, 0, 20, 20, /* x, y, width, height */
                60, 20, 20, /* xTexStart, yTexStart, yDiffTex */
                BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                x -> {
                    workManager.cancel();
                    minecraft.setScreen(new SettingsScreen(screen, this));
                }
        );
        settings.setTooltip(Tooltip.create(BTN_SETTINGS));
        settings.active = false; // Do not allow clicking away until we loaded levelStemKeys
        toRender.add(settings);

        resetToZeroZero = new ImageButton(
                0, 0, 20, 20, /* x, y, width, height */
                120, 20, 20, /* xTexStart, yTexStart, yDiffTex */
                BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                x -> renderSettings.resetCenter()
        );
        resetToZeroZero.setTooltip(Tooltip.create(BTN_HOME));
        toRender.add(resetToZeroZero);

        resetDefaultStructureVisibility = new ImageButton(
                0, 0, 20, 20, /* x, y, width, height */
                180, 20, 20, /* xTexStart, yTexStart, yDiffTex */
                BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                x -> Arrays.stream(allStructures).forEach(StructuresList.StructureEntry::reset)
        );
        resetDefaultStructureVisibility.active = false; // Deactivate first in case sampleStructures is off
        toRender.add(resetDefaultStructureVisibility);

        switchBiomes = Button.builder(DisplayType.BIOMES.component(), x -> onTabButtonChange(x, DisplayType.BIOMES))
                .size(100, LINE_HEIGHT)
                .build();
        switchStructures = Button.builder(DisplayType.STRUCTURES.component(), x -> onTabButtonChange(x, DisplayType.STRUCTURES))
                .size(100, LINE_HEIGHT)
                .build();
        switchSeeds = Button.builder(DisplayType.SEEDS.component(), x -> onTabButtonChange(x, DisplayType.SEEDS))
                .size(100, LINE_HEIGHT)
                .build();

        toRender.add(switchBiomes);
        toRender.add(switchStructures);
        toRender.add(switchSeeds);

        biomesList = new BiomesList(this, minecraft, 200, 300, 4, 100, true);
        biomesListHolder = new AbstractSelectionListHolder<>(biomesList, 0, 0, screen.width, screen.height, TITLE);
        biomesListHolder.visible = true;
        toRender.add(biomesListHolder);

        structuresList = new StructuresList(minecraft, 200, 300, 4, 100);
        structuresListHolder = new AbstractSelectionListHolder<>(structuresList, 0, 0, screen.width, screen.height, TITLE);
        structuresListHolder.visible = false;
        toRender.add(structuresListHolder);

        seedsList = new SeedsList(minecraft, this, 200, 300, 4, 100);
        seedsListHolder = new AbstractSelectionListHolder<>(seedsList, 0, 0, screen.width, screen.height, TITLE);
        seedsListHolder.visible = false;
        updateSeedListWidget();
        toRender.add(seedsListHolder);

        previewDisplay = new PreviewDisplay(minecraft, this, TITLE);
        toRender.add(previewDisplay);

        toggleCaves = new ToggleButton(
                0, 0, 20, 20, /* x, y, width, height */
                80, 20, 20, 20, /* xTexStart, yTexStart, xDiffTex, yDiffTex */
                BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                x -> {
                    biomesList.setSelected(null);
                    previewDisplay.setSelectedBiomeId((short) -1);
                    previewDisplay.setHighlightCaves(((ToggleButton) x).selected);
                }
        );
        toggleCaves.setTooltip(Tooltip.create(BTN_CAVES));
        toRender.add(toggleCaves);

        toggleShowStructures = new ToggleButton(
                0, 0, 20, 20, /* x, y, width, height */
                140, 20, 20, 20, /* xTexStart, yTexStart, xDiffTex, yDiffTex */
                BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                x -> renderSettings.hideAllStructures = !((ToggleButton) x).selected
        );
        toggleShowStructures.selected = true;
        toggleShowStructures.active = false; // Deactivate first in case sampleStructures is off
        toRender.add(toggleShowStructures);

        toggleHeightmap = new ToggleButton(
                0, 0, 20, 20, /* x, y, width, height */
                200, 20, 20, 20, /* xTexStart, yTexStart, xDiffTex, yDiffTex */
                BUTTONS_TEXTURE, BUTTONS_TEX_WIDTH, BUTTONS_TEX_HEIGHT, /* resourceLocation, textureWidth, textureHeight*/
                x -> renderSettings.showHeightMap = ((ToggleButton) x).selected
        );
        toggleHeightmap.selected = false;
        toggleHeightmap.active = false;
        toRender.add(toggleHeightmap);

        biomesList.setBiomeChangeListener(x -> {
            previewDisplay.setSelectedBiomeId(x == null ? -1 : x.id());
            toggleCaves.selected = x == null && toggleCaves.selected;
            previewDisplay.setHighlightCaves(x == null && toggleCaves.selected);
        });
        uiState.addListener(this::updateSettings);

        onTabButtonChange(switchBiomes, DisplayType.BIOMES);
    }

    public void patchColorData() {
        Map<ResourceLocation, PreviewMappingData.ColorEntry> configured = Arrays.stream(allBiomes)
                .filter(x -> x.dataSource() == PreviewData.DataSource.CONFIG)
                .collect(Collectors.toMap(x -> x.entry().key().location(), x -> new PreviewMappingData.ColorEntry(x.color(), x.isCave())));

        Map<ResourceLocation, PreviewMappingData.ColorEntry> defaults = Arrays.stream(allBiomes)
                .filter(x -> x.dataSource() == PreviewData.DataSource.RESOURCE)
                .collect(Collectors.toMap(x -> x.entry().key().location(), x -> new PreviewMappingData.ColorEntry(x.color(), x.isCave())));

        Map<ResourceLocation, PreviewMappingData.ColorEntry> missing = Arrays.stream(allBiomes)
                .filter(x -> x.dataSource() == PreviewData.DataSource.MISSING)
                .collect(Collectors.toMap(x -> x.entry().key().location(), x -> new PreviewMappingData.ColorEntry(x.color(), x.isCave())));

        previewMappingData.update(missing, PreviewData.DataSource.MISSING);
        previewMappingData.update(defaults, PreviewData.DataSource.RESOURCE);
        previewMappingData.update(configured, PreviewData.DataSource.CONFIG);
        updateSettings(uiState);
    }

    /**
     * Create a playground for mods to do their thing while minimizing the risk
     * to the real world creation stuff.
     */
    private WorldCreationContext previewWorldCreationContext() {
        WorldCreationContext wcContext = uiState.getSettings();
        WorldDataConfiguration worldDataConfiguration = wcContext.dataConfiguration();
        // new WorldGenSettings(WorldOptions.defaultWithRandomSeed(), WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen())

        record Cookie(WorldGenSettings worldGenSettings) {}

        // WorldGenSettings worldGenSettings = new WorldGenSettings(wcContext.options(), wcContext.selectedDimensions());

        PackRepository packRepository = ((CreateWorldScreenAccessor) createWorldScreen).invokeGetDataPackSelectionSettings(worldDataConfiguration).getSecond();
        WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, true);
        WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 2);
        CompletableFuture<WorldCreationContext> completableFuture = WorldLoader.load(
                initConfig,
                dataLoadContext -> {
                    WorldDimensions worldDimensions = WorldPresets.createNormalWorldDimensions(dataLoadContext.datapackWorldgen());
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

    private synchronized void updateSettings(WorldCreationUiState ignore) {
        if (inhibitUpdates) {
            return;
        }
        inhibitUpdates = true;
        try {
            final int revision;
            synchronized (reloadRevision) {
                revision = reloadRevision.incrementAndGet();
            }
            isUpdating = true;
            CompletableFuture
                .supplyAsync(() -> {
                    // Check if we are the latest update
                    if (reloadRevision.get() > revision) {
                        return null;
                    }
                    return previewWorldCreationContext();
                }, reloadExecutor)
                .thenAcceptAsync(x -> {
                    // Check if we are the latest update
                    if (reloadRevision.get() > revision || x == null) {
                        return;
                    }
                    updateSettings_real(x);
                    synchronized (reloadRevision) {
                        if (reloadRevision.get() <= revision) {
                            isUpdating = false;
                        }
                    }
                }, minecraft);
        } finally {
            inhibitUpdates = false;
        }
    }

    private void updateSettings_real(WorldCreationContext wcContext) {
        saveSeed.active = !uiState.getSeed().isEmpty() && !cfg.savedSeeds.contains(uiState.getSeed());
        updateSeedListWidget();
        seedEdit.setValue(uiState.getSeed());
        if (!seedEdit.isFocused()) {
            seedEdit.moveCursorToStart();
        }

        // Range validation
        if (cfg.heightmapMinY == cfg.heightmapMaxY) {
            cfg.heightmapMaxY++;
        } else if (cfg.heightmapMaxY < cfg.heightmapMinY) {
            int tmp = cfg.heightmapMaxY;
            cfg.heightmapMaxY = cfg.heightmapMinY;
            cfg.heightmapMinY = tmp;
        }

        // Basic world loading / generation setup
        WorldDataConfiguration worldDataConfiguration = wcContext.dataConfiguration();
        Registry<Biome> biomeRegistry = wcContext.worldgenLoadContext().registryOrThrow(Registries.BIOME);
        Registry<Structure> strucutreRegistry = wcContext.worldgenLoadContext().registryOrThrow(Registries.STRUCTURE);
        WorldDimensions.Complete worldDimensions = wcContext.selectedDimensions().bake(wcContext.datapackDimensions());
        levelStemRegistry = worldDimensions.dimensions();
        levelStemKeys = levelStemRegistry.keySet().stream().sorted(Comparator.comparing(Object::toString)).toList();

        // Now that the level stem keys are loaded, allow the user to go into properties!
        settings.active = true;

        if (renderSettings.dimension == null || !levelStemRegistry.containsKey(renderSettings.dimension)) {
            if (levelStemRegistry.containsKey(LevelStem.OVERWORLD)) {
                renderSettings.dimension = LevelStem.OVERWORLD.location();
            } else {
                renderSettings.dimension = levelStemRegistry.keySet().iterator().next();
            }
        }
        LevelStem levelStem = levelStemRegistry.get(renderSettings.dimension);

        previewData = previewMappingData.generateMapData(biomeRegistry.keySet(), strucutreRegistry.keySet());

        // Check whether we have a valid colormap stored
        ColorMap colorMap = previewData.colorMaps().get(cfg.colorMap);
        if (colorMap == null) {
            cfg.colorMap = "world_preview:inferno";
        }

        // WorkManager update
        LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = wcContext
                .worldgenRegistries()
                .replaceFrom(RegistryLayer.DIMENSIONS, worldDimensions.dimensionsRegistryAccess());

        workManager.cancel();
        workManager.changeWorldGenState(
                levelStem,
                layeredRegistryAccess,
                previewData,
                wcContext.options(),
                worldDataConfiguration,
                minecraft.getProxy(),
                ((CreateWorldScreenAccessor) createWorldScreen).invokeGetTempDataPackDir()
        );

        // Biomes
        List<String> missing = Arrays.stream(previewData.biomeId2BiomeData())
                .filter(x -> x.dataSource() == PreviewData.DataSource.MISSING)
                .map(PreviewData.BiomeData::tag)
                .map(ResourceLocation::toString)
                .toList();
        worldPreview.writeMissingColors(missing);

        allBiomes = biomeRegistry.holders()
                .map(x -> {
                    final short id = previewData.biome2Id().getShort(x.key().location().toString());
                    final PreviewData.BiomeData biomeData = previewData.biomeId2BiomeData()[id];
                    final int color = biomeData.color();
                    final int initialColor = biomeData.resourceOnlyColor();
                    final boolean isCave = biomeData.isCave();
                    final boolean initialIsCave = biomeData.resourceOnlyIsCave();
                    final String explicitName = biomeData.name();
                    final PreviewData.DataSource dataSource = biomeData.dataSource();
                    return biomesList.createEntry(x, id, color, initialColor, isCave, initialIsCave, explicitName, dataSource);
                })
                .sorted(Comparator.comparing(BiomesList.BiomeEntry::id))
                .toArray(BiomesList.BiomeEntry[]::new);

        biomesList.replaceEntries(new ArrayList<>());
        biomesList.setSelected(null);

        // Structures
        missing = Arrays.stream(previewData.structId2StructData())
                .filter(x -> x.dataSource() == PreviewData.DataSource.MISSING)
                .map(PreviewData.StructureData::tag)
                .map(ResourceLocation::toString)
                .toList();
        worldPreview.writeMissingStructures(missing);

        //  - Icons
        freeStructureIcons();
        ResourceManager builtinResourceManager = minecraft.getResourceManager();
        Map<ResourceLocation, NativeImage> icons = new HashMap<>();
        allStructureIcons = new NativeImage[previewData.structId2StructData().length];
        for (int i = 0; i < previewData.structId2StructData().length; ++i) {
            PreviewData.StructureData data = previewData.structId2StructData()[i];
            allStructureIcons[i] = icons.computeIfAbsent(data.icon(), x -> {
                Optional<Resource> resource = builtinResourceManager.getResource(x);
                if (resource.isEmpty()) {
                    resource = workManager.sampleResourceManager().getResource(x);
                }
                if (resource.isEmpty()) {
                    LOGGER.error("Failed to load structure icon: '{}'", x);
                    resource = builtinResourceManager.getResource(new ResourceLocation("world_preview:textures/structure/unknown.png"));
                }
                if (resource.isEmpty()) {
                    LOGGER.error("FATAL ERROR LOADING: '{}' -- unable to load fallback!", x);
                    return null;
                }
                try {
                    try(InputStream in = resource.get().open()) {
                        return NativeImage.read(in);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });
        }

        //  - List entries
        allStructures = strucutreRegistry.holders()
                .map(x -> {
                    final short id = previewData.struct2Id().getShort(x.key().location().toString());
                    final PreviewData.StructureData structureData = previewData.structId2StructData()[id];
                    return structuresList.createEntry(
                            id,
                            x.key().location(),
                            allStructureIcons[id],
                            structureData.name(),
                            structureData.showByDefault(),
                            structureData.showByDefault()
                    );
                })
                .sorted(Comparator.comparing(StructuresList.StructureEntry::id))
                .toArray(StructuresList.StructureEntry[]::new);

        structuresList.replaceEntries(new ArrayList<>());

        // Finalize the GUI
        renderSettings.resetCenter();

        if (cfg.sampleStructures) {
            resetDefaultStructureVisibility.active = true;
            toggleShowStructures.active = true;
            resetDefaultStructureVisibility.setTooltip(Tooltip.create(BTN_RESET_STRUCTURES));
            toggleShowStructures.setTooltip(Tooltip.create(BTN_TOGGLE_STRUCTURES));
        } else {
            resetDefaultStructureVisibility.active = false;
            toggleShowStructures.active = false;
            resetDefaultStructureVisibility.setTooltip(Tooltip.create(BTN_RESET_STRUCTURES_DISABLED));
            toggleShowStructures.setTooltip(Tooltip.create(BTN_TOGGLE_STRUCTURES_DISABLED));
        }

        if (cfg.sampleHeightmap) {
            toggleHeightmap.active = true;
            toggleHeightmap.setTooltip(Tooltip.create(BTN_TOGGLE_HEIGHTMAP));
        } else {
            toggleHeightmap.active = false;
            toggleHeightmap.setTooltip(Tooltip.create(BTN_TOGGLE_HEIGHTMAP_DISABLED));
            renderSettings.showHeightMap = false;
        }

        previewDisplay.reloadData();
        previewDisplay.setSelectedBiomeId((short) -1);
        previewDisplay.setHighlightCaves(false);
        toggleCaves.selected = false;
    }

    @Override
    public void onVisibleBiomesChanged(Short2LongMap visibleBiomes) {
        List<BiomesList.BiomeEntry> res = visibleBiomes.short2LongEntrySet()
                .stream()
                .sorted(Comparator.comparing(Short2LongMap.Entry::getLongValue).reversed())
                .map(Short2LongMap.Entry::getShortKey)
                .map(x -> allBiomes[x])
                .toList();

        biomesList.replaceEntries(res);
    }

    @Override
    public void onVisibleStructuresChanged(Short2LongMap visibleStructures) {
        List<StructuresList.StructureEntry> res = visibleStructures.short2LongEntrySet()
                .stream()
                .sorted(Comparator.comparing(Short2LongMap.Entry::getLongValue))
                .map(Short2LongMap.Entry::getShortKey)
                .map(x -> allStructures[x])
                .toList();

        structuresList.replaceEntries(res);
    }

    private void randomizeSeed(Button btn) {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES * 2);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        String uuidSeed = Base64.getEncoder().encodeToString(bb.array()).substring(0, 16);
        setSeed(uuidSeed);
        // setSeed(String.valueOf(WorldOptions.randomSeed()));
    }

    private void saveCurrentSeed(Button btn) {
        cfg.savedSeeds.add(uiState.getSeed());
        saveSeed.active = false;
        updateSeedListWidget();
    }

    public void deleteSeed(String seed) {
        cfg.savedSeeds.remove(seed);
        updateSeedListWidget();
    }

    public void setSeed(String seed) {
        if (Objects.equals(uiState.getSeed(), seed)) {
            return;
        }

        boolean initialInhibitUpdates = inhibitUpdates;
        inhibitUpdates = true;
        try {
            uiState.setSeed(seed);
        } finally {
            inhibitUpdates = initialInhibitUpdates;
        }
        updateSettings(null);
    }

    private void updateSeedListWidget() {
        seedEntries = cfg.savedSeeds.stream().map(seedsList::createEntry).toList();
        seedsList.replaceEntries(seedEntries);
        int idx = cfg.savedSeeds.indexOf(uiState.getSeed());
        if (idx >= 0) {
            seedsList.setSelected(seedEntries.get(idx));
        }
    }

    public void resetTabs() {
        onTabButtonChange(switchBiomes, DisplayType.BIOMES);
    }

    private void onTabButtonChange(Button btn, DisplayType type) {
        biomesListHolder.visible = false;
        structuresListHolder.visible = false;
        seedsListHolder.visible = false;

        switchBiomes.active = true;
        switchStructures.active = true;
        switchSeeds.active = true;

        if (cfg.sampleStructures) {
            switchStructures.setTooltip(null);
        } else {
            switchStructures.setTooltip(Tooltip.create(BTN_SWITCH_STRUCT_DISABLED));
            switchStructures.active = false;
        }

        btn.active = false;
        switch (type) {
            case BIOMES -> biomesListHolder.visible = true;
            case STRUCTURES -> structuresListHolder.visible = true;
            case SEEDS -> seedsListHolder.visible = true;
        }
    }

    /**
     * Start generating the biome data
     */
    public synchronized void start() {
        LOGGER.info("Start generating biome data...");
        if (uiState.getSeed().isEmpty()) {
            randomizeSeed(null);
        }
        inhibitUpdates = false;
        updateSettings(uiState);
    }

    /**
     * Stop processing
     */
    public synchronized void stop() {
        LOGGER.info("Stop generating biome data...");
        inhibitUpdates = true;
        workManager.cancel();
    }

    @Override
    public @NotNull Component getTabTitle() {
        return TITLE;
    }

    @Override
    public void visitChildren(Consumer<AbstractWidget> consumer) {
        toRender.forEach(consumer);
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        int leftWidth = Math.min(180, screenRectangle.width() / 3);
        int left = screenRectangle.left() + 3;
        int top = screenRectangle.top() + 2;
        int bottom = screenRectangle.bottom() - 32;

        // Preview
        previewDisplay.setPosition(left + leftWidth + 3, top + 1);
        previewDisplay.setSize(screenRectangle.right() - previewDisplay.getX() - 4, screenRectangle.bottom() - previewDisplay.getY() - 14);

        // BOTTOM

        seedEdit.setWidth(leftWidth - 1 - 22 * 2);
        seedEdit.setX(left);
        seedEdit.setY(bottom + 1);

        randomSeedButton.setX((left + leftWidth) - 20);
        randomSeedButton.setY(bottom);

        saveSeed.setX((left + leftWidth) - 22 - 20);
        saveSeed.setY(bottom);

        // TOP
        int cycleWith = leftWidth - 22 * 5;

        int btnStart = left + cycleWith + 2;
        settings.setPosition(left, top);
        int i = 0;
        toggleHeightmap.setPosition(btnStart + 22 * i++, top);
        resetDefaultStructureVisibility.setPosition(btnStart + 22 * i++, top);
        toggleShowStructures.setPosition(btnStart + 22 * i++, top);
        toggleCaves.setPosition(btnStart + 22 * i++, top);
        resetToZeroZero.setPosition(btnStart + 22 * i++, top);

        //  - new row
        top += LINE_HEIGHT + LINE_VSPACE;
        int switchBiomesWidth = 45;
        int switchSeedsWidth = 45;
        int switchStructuresWidth = leftWidth - switchBiomesWidth - switchSeedsWidth - 4;
        switchBiomes.setPosition(left, top);
        switchStructures.setPosition(left + switchBiomesWidth + 2, top);
        switchSeeds.setPosition(left + switchBiomesWidth + switchStructuresWidth + 4, top);

        switchBiomes.setWidth(switchBiomesWidth);
        switchStructures.setWidth(switchStructuresWidth);
        switchSeeds.setWidth(switchSeedsWidth);

        //  - new row
        top += LINE_HEIGHT + LINE_VSPACE;

        biomesListHolder.setPosition(left, top);
        biomesListHolder.setSize(leftWidth, bottom - top - LINE_VSPACE);
        biomesList.setRenderBackground(true);
        biomesList.setRenderTopAndBottom(false);

        structuresListHolder.setPosition(left, top);
        structuresListHolder.setSize(leftWidth, bottom - top - LINE_VSPACE);
        structuresList.setRenderBackground(true);
        structuresList.setRenderTopAndBottom(false);

        seedsListHolder.setPosition(left, top);
        seedsListHolder.setSize(leftWidth, bottom - top - LINE_VSPACE);
        seedsList.setRenderBackground(true);
        seedsList.setRenderTopAndBottom(false);
    }

    public void close() {
        workManager.cancel();
        previewDisplay.close();
        freeStructureIcons();
    }

    private void freeStructureIcons() {
        if (allStructureIcons == null) {
            return;
        }
        Arrays.stream(allStructureIcons).filter(Objects::nonNull).forEach(NativeImage::close);
    }

    public List<BiomesList.BiomeEntry> allBiomes() {
        return Arrays.stream(allBiomes).sorted(Comparator.comparing(BiomesList.BiomeEntry::name)).toList();
    }

    public List<ResourceLocation> levelStemKeys() {
        return levelStemKeys;
    }

    public Registry<LevelStem> levelStemRegistry() {
        return levelStemRegistry;
    }

    @Override
    public BiomesList.BiomeEntry biome4Id(int id) {
        return allBiomes[id];
    }

    @Override
    public NativeImage[] structureIcons() {
        return allStructureIcons;
    }

    @Override
    public void onBiomeVisuallySelected(BiomesList.BiomeEntry entry) {
        biomesList.setSelected(entry, true);
        toggleCaves.selected = false;
        previewDisplay.setHighlightCaves(false);
    }

    @Override
    public PreviewData previewData() {
        return previewData;
    }

    @Override
    public StructureRenderInfo[] renderStructureMap() {
        return allStructures;
    }

    @Override
    public int[] heightColorMap() {
        ColorMap colorMap = previewData.colorMaps().get(cfg.colorMap);
        if (colorMap == null) {
            int[] black = new int[workManager.yMax() - workManager.yMin()];
            Arrays.fill(black, 0xFF000000);
            return black;
        }
        return colorMap.bake(workManager.yMin(), workManager.yMax(), cfg.heightmapMinY, cfg.heightmapMaxY);
    }

    @Override
    public int yMin() {
        return workManager.yMin();
    }

    @Override
    public int yMax() {
        return workManager.yMax();
    }

    @Override
    public boolean isUpdating() {
        return isUpdating;
    }

    public enum DisplayType {
        BIOMES,
        STRUCTURES,
        SEEDS,
        ;

        public Component component() {
            return toComponent(this);
        }

        public static Component toComponent(DisplayType x) {
            return Component.translatable("world_preview.preview.btn-cycle." + x.name());
        }
    }
}
