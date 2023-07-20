package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.backend.storage.PreviewLevel;
import caeruleusTait.world.preview.backend.stubs.DummyMinecraftServer;
import caeruleusTait.world.preview.backend.stubs.EmptyAquifer;
import caeruleusTait.world.preview.mixin.NoiseBasedChunkGeneratorAccessor;
import caeruleusTait.world.preview.mixin.NoiseChunkAccessor;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.*;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.level.validation.PathAllowList;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;
import static net.minecraft.core.registries.Registries.LEVEL_STEM;

public class SampleUtils implements AutoCloseable {
    private final Path tempDir;
    private final DataFixer dataFixer;
    private final LevelStorageSource levelStorageSource;
    private final LevelStorageSource.LevelStorageAccess levelStorageAccess;
    private final LevelHeightAccessor levelHeightAccessor;
    private final CloseableResourceManager resourceManager;
    private final PackRepository packRepository;
    private final BiomeSource biomeSource;
    private final RandomState randomState;
    private final ChunkGenerator chunkGenerator;
    private final RegistryAccess registryAccess;
    private final ChunkGeneratorStructureState chunkGeneratorStructureState;
    private final StructureCheck structureCheck;
    private final StructureManager structureManager;
    private final StructureTemplateManager structureTemplateManager;
    private final PreviewLevel previewLevel;
    private final Registry<Structure> structureRegistry;
    private final NoiseGeneratorSettings noiseGeneratorSettings;
    private final MinecraftServer minecraftServer;

    public SampleUtils(
            BiomeSource biomeSource,
            RandomState randomState,
            ChunkGenerator chunkGenerator,
            LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess,
            WorldOptions worldOptions,
            LevelStem levelStem,
            LevelHeightAccessor levelHeightAccessor,
            WorldDataConfiguration worldDataConfiguration,
            Proxy proxy,
            @Nullable Path tempDataPackDir
    ) throws IOException {
        try {
            tempDir = Files.createTempDirectory("world_preview");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.dataFixer = DataFixers.getDataFixer();
        this.levelStorageSource = new LevelStorageSource(tempDir, tempDir.resolve("backups"), new DirectoryValidator(new PathAllowList(List.of())), dataFixer);
        this.levelStorageAccess = levelStorageSource.createAccess("world_preview");
        this.levelHeightAccessor = levelHeightAccessor;

        Path dataPackDir = levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR);
        FileUtil.createDirectoriesSafe(dataPackDir);
        if (tempDataPackDir != null) {
            try (Stream<Path> stream = Files.walk(tempDataPackDir)) {
                stream.filter(x -> !x.equals(tempDataPackDir)).forEach(x -> {
                    try {
                        Util.copyBetweenDirs(tempDataPackDir, dataPackDir, x);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        this.biomeSource = biomeSource;
        this.randomState = randomState;
        this.chunkGenerator = chunkGenerator;
        this.registryAccess = layeredRegistryAccess.compositeAccess();
        this.structureRegistry = this.registryAccess.registryOrThrow(Registries.STRUCTURE);
        this.previewLevel = new PreviewLevel(this.registryAccess, this.levelHeightAccessor);
        this.chunkGeneratorStructureState = this.chunkGenerator.createState(
                this.registryAccess.lookupOrThrow(Registries.STRUCTURE_SET),
                this.randomState,
                worldOptions.seed()
        );

        packRepository = ServerPacksSource.createPackRepository(levelStorageAccess);
        resourceManager = (new WorldLoader.PackConfig(packRepository, worldDataConfiguration, false, false)).createResourceManager().getSecond();

        HolderGetter<Block> holderGetter = this.registryAccess.registryOrThrow(Registries.BLOCK).asLookup().filterFeatures(worldDataConfiguration.enabledFeatures());
        this.structureTemplateManager = new StructureTemplateManager(resourceManager, levelStorageAccess, dataFixer, holderGetter);

        ResourceKey<LevelStem> levelStemResourceKey = this.registryAccess.registryOrThrow(LEVEL_STEM).getResourceKey(levelStem).orElseThrow();
        ResourceKey<Level> levelResourceKey = Registries.levelStemToLevel(levelStemResourceKey);
        this.structureCheck = new StructureCheck(
                null, // Should never be required because `tryLoadFromStorage` must not be called
                this.registryAccess,
                this.structureTemplateManager,
                levelResourceKey,
                this.chunkGenerator,
                this.randomState,
                this.levelHeightAccessor,
                chunkGenerator.getBiomeSource(),
                worldOptions.seed(),
                dataFixer
        );

        this.structureManager = new StructureManager(this.previewLevel, worldOptions, this.structureCheck);

        // Some mods listen on the <init> of MinecraftServer
        LevelSettings levelSettings = new LevelSettings("temp", GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(), worldDataConfiguration);
        PrimaryLevelData primaryLevelData = new PrimaryLevelData(levelSettings, worldOptions, PrimaryLevelData.SpecialWorldProperty.NONE, Lifecycle.stable());
        ReloadableServerResources reloadableServerResources = new ReloadableServerResources(layeredRegistryAccess.compositeAccess(), FeatureFlagSet.of(), Commands.CommandSelection.ALL, 0);
        WorldStem worldStem = new WorldStem(resourceManager, reloadableServerResources, layeredRegistryAccess, primaryLevelData);

        minecraftServer = new DummyMinecraftServer(
                null,
                levelStorageAccess,
                packRepository,
                worldStem,
                proxy,
                dataFixer,
                new Services(null, null, null, null),
                i -> new ChunkProgressListener() {
                    @Override
                    public void updateSpawnPos(ChunkPos center) {
                    }

                    @Override
                    public void onStatusChange(ChunkPos chunkPosition, @Nullable ChunkStatus newStatus) {
                    }

                    @Override
                    public void start() {
                    }

                    @Override
                    public void stop() {
                    }
                }
        );

        // Noise / Heightmap stuff
        if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            noiseGeneratorSettings = noiseBasedChunkGenerator.generatorSettings().value();
        } else {
            noiseGeneratorSettings = null;
        }

        // All this stuff, just so we can give Forge a fake minecraft server...
        WorldPreview.get().loaderSpecificSetup(minecraftServer);

        // Initialize early
        chunkGeneratorStructureState.ensureStructuresGenerated();
    }

    public ResourceKey<Biome> doSample(BlockPos pos) {
        return biomeSource.getNoiseBiome(
                QuartPos.fromBlock(pos.getX()),
                QuartPos.fromBlock(pos.getY()),
                QuartPos.fromBlock(pos.getZ()),
                randomState.sampler()
        ).unwrapKey().orElseThrow();
    }

    public List<Pair<ResourceLocation, StructureStart>> doStructures(ChunkPos chunkPos) {
        ProtoChunk protoChunk = (ProtoChunk) previewLevel.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
        chunkGenerator.createStructures(registryAccess, chunkGeneratorStructureState, structureManager, protoChunk, structureTemplateManager);
        Map<Structure, StructureStart> raw = protoChunk.getAllStarts();
        List<Pair<ResourceLocation, StructureStart>> res = new ArrayList<>(raw.size());
        for (Map.Entry<Structure, StructureStart> x : protoChunk.getAllStarts().entrySet()) {
            res.add(new Pair<>(structureRegistry.getKey(x.getKey()), x.getValue()));
        }
        return res;
    }

    public NoiseChunk getNoiseChunk(ChunkPos startChunk, int numChunks) {
        NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
        NoiseChunk noiseChunk = new NoiseChunk(
                (numChunks * 16) / noiseSettings.getCellWidth(),
                randomState,
                startChunk.getMinBlockX(),
                startChunk.getMinBlockZ(),
                noiseSettings,
                DensityFunctions.BeardifierMarker.INSTANCE,
                noiseGeneratorSettings,
                null, // ((NoiseBasedChunkGeneratorAccessor) chunkGenerator).getGlobalFluidPicker().get(),
                Blender.empty()
        );
        ((NoiseChunkAccessor) noiseChunk).setAquifer(new EmptyAquifer());
        return noiseChunk;
    }

    public NoiseGeneratorSettings noiseGeneratorSettings() {
        return noiseGeneratorSettings;
    }

    public short doHeightSlow(BlockPos pos) {
        return (short) chunkGenerator.getBaseHeight(
                pos.getX(),
                pos.getZ(),
                Heightmap.Types.OCEAN_FLOOR_WG,
                levelHeightAccessor,
                randomState
        );
    }

    /*
    public void doStructureRefs(ChunkPos chunkPos) {
        ProtoChunk protoChunk = (ProtoChunk) previewLevel.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
        chunkGenerator.createReferences(previewLevel, structureManager, protoChunk);
        Map<Structure, LongSet> refs = protoChunk.getAllReferences();
        Registry<Structure> structureRegistry = registryAccess.registryOrThrow(Registries.STRUCTURE);
        for (Structure x : refs.keySet()) {
            ResourceLocation structName = structureRegistry.getKey(x);
            LOGGER.info("[{} x {}] | {}", chunkPos.x, chunkPos.z, structName);
        }
    }
     */

    @Override
    public void close() throws Exception {
        // FileUtils.deleteDirectory(tempDir.toFile());
        WorldPreview.get().loaderSpecificTeardown(minecraftServer);
        deleteDirectoryLegacyIO(tempDir.toFile());
    }

    // Source https://mkyong.com/java/how-to-delete-directory-in-java/
    public static void deleteDirectoryLegacyIO(File file) {
        File[] list = file.listFiles();
        if (list != null) {
            for (File temp : list) {
                //recursive delete
                deleteDirectoryLegacyIO(temp);
            }
        }

        if (!file.delete()) {
            LOGGER.warn("Unable to delete file or directory : {}", file);
        }
    }

    public CloseableResourceManager resourceManager() {
        return resourceManager;
    }
}
