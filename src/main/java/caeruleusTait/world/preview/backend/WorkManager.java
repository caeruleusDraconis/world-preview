package caeruleusTait.world.preview.backend;

import caeruleusTait.world.preview.RenderSettings;
import caeruleusTait.world.preview.WorldPreviewConfig;
import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.sampler.ChunkSampler;
import caeruleusTait.world.preview.backend.storage.PreviewSection;
import caeruleusTait.world.preview.backend.storage.PreviewStorage;
import caeruleusTait.world.preview.backend.worker.*;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import static caeruleusTait.world.preview.WorldPreview.LOGGER;

public class WorkManager {
    public static final int Y_BLOCK_SHIFT = 3;
    public static final int Y_BLOCK_STRIDE = 1 << Y_BLOCK_SHIFT;

    private final Object completedSynchro = new Object();

    private LevelStem levelStem;
    private DimensionType dimensionType;
    private ChunkGenerator chunkGenerator;
    private BiomeSource biomeSource;
    private RandomState randomState;
    private ChunkSampler chunkSampler;
    private SampleUtils sampleUtils;

    private PreviewData previewData;
    private PreviewStorage previewStorage;
    private final RenderSettings renderSettings;
    private final WorldPreviewConfig config;

    private final List<WorkBatch> currentBatches = new ArrayList<>();
    private final List<Future<?>> futures = new ArrayList<>();
    private final List<Future<?>> queueFutures = new ArrayList<>();
    private final SplittableRandom random = new SplittableRandom();

    private ExecutorService executorService;
    private ExecutorService queueChunksService;

    private ChunkPos lastQueuedTopLeft;
    private ChunkPos lastQueuedBotRight;
    private int lastY;

    private boolean queueIsRunning = false;
    private boolean shouldEarlyAbortQueuing = false;

    public WorkManager(RenderSettings renderSettings, WorldPreviewConfig config) {
        this.config = config;
        this.renderSettings = renderSettings;
    }

    public synchronized void changeWorldGenState(
            LevelStem _levelStem,
            LayeredRegistryAccess<RegistryLayer> _registryAccess,
            PreviewData _previewData,
            WorldOptions _worldOptions,
            WorldDataConfiguration _worldDataConfiguration,
            Proxy proxy,
            @Nullable Path tempDataPackDir,
            @Nullable MinecraftServer server
    ) {
        cancel();
        levelStem = _levelStem;
        dimensionType = levelStem.type().value();
        chunkGenerator = levelStem.generator();
        biomeSource = chunkGenerator.getBiomeSource();
        previewStorage = new PreviewStorage(renderSettings, yMin(), yMax());
        chunkSampler = renderSettings.samplerType.create(renderSettings.quartStride());
        previewData = _previewData;

        if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
            randomState = RandomState.create(
                    noiseBasedChunkGenerator.generatorSettings().value(),
                    _registryAccess.compositeAccess().lookupOrThrow(Registries.NOISE),
                    _worldOptions.seed()
            );
        } else {
            randomState = RandomState.create(
                    NoiseGeneratorSettings.dummy(),
                    _registryAccess.compositeAccess().lookupOrThrow(Registries.NOISE),
                    _worldOptions.seed()
            );
        }

        LevelHeightAccessor levelHeightAccessor = LevelHeightAccessor.create(dimensionType.minY(), dimensionType.height());
        try {
            if (server == null) {
                sampleUtils = new SampleUtils(
                        biomeSource,
                        randomState,
                        chunkGenerator,
                        _registryAccess,
                        _worldOptions,
                        levelStem,
                        levelHeightAccessor,
                        _worldDataConfiguration,
                        proxy,
                        tempDataPackDir
                );
            } else {
                sampleUtils = new SampleUtils(
                        server,
                        biomeSource,
                        randomState,
                        chunkGenerator,
                        _worldOptions,
                        levelStem,
                        levelHeightAccessor
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Only create the executors at the end to ensure that there are no
        // null pointer exceptions
        executorService = Executors.newFixedThreadPool(config.numThreads());
        queueChunksService = Executors.newSingleThreadExecutor();
    }

    private void shutdownExecutors() {
        if (executorService == null) {
            return;
        }

        shouldEarlyAbortQueuing = true;

        synchronized (currentBatches) {
            currentBatches.forEach(WorkBatch::cancel);
            currentBatches.clear();
        }
        try {
            List<Future<?>> allFutures = new ArrayList<>();
            synchronized (futures) {
                allFutures.addAll(queueFutures);
                allFutures.addAll(futures);
            }
            for (Future<?> f : allFutures) {
                f.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        executorService.shutdownNow();
        queueChunksService.shutdownNow();
    }

    public void cancel() {
        shutdownExecutors();

        if (sampleUtils != null) {
            try {
                sampleUtils.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        levelStem = null;
        dimensionType = null;
        chunkGenerator = null;
        sampleUtils = null;
        previewStorage = null;
        lastQueuedTopLeft = null;
        lastQueuedBotRight = null;
        lastY = Integer.MIN_VALUE;
        queueIsRunning = false;
        futures.clear();
        executorService = null;
        queueChunksService = null;
    }

    private boolean requeueOnYOnlyChange() {
        if (config.buildFullVertChunk) {
            return false;
        }
        return true;
    }

    public void queueRange(BlockPos topLeftBlock, BlockPos bottomRightBlock) {
        final ChunkPos topLeft = new ChunkPos(topLeftBlock);
        final ChunkPos bottomRight = new ChunkPos(bottomRightBlock);
        if (executorService == null ||
                (
                        topLeft.equals(lastQueuedTopLeft)
                        && bottomRight.equals(lastQueuedBotRight)
                        && (topLeftBlock.getY() == lastY || !requeueOnYOnlyChange())
                )
        ) {
            return;
        }

        // Only have one in queue
        if (queueIsRunning) {
            // Signal the current queue algorithm to hurry up and skip
            // queueing more work units / batches since they will be canceled
            // the next run anyway.
            shouldEarlyAbortQueuing = true;
            return;
        }

        // Now, that we are definitely queueing, remember the last values
        lastQueuedTopLeft = topLeft;
        lastQueuedBotRight = bottomRight;
        lastY = topLeftBlock.getY();
        synchronized (futures) {
            queueFutures.add(queueChunksService.submit(() -> queueRangeWrapper(topLeftBlock, bottomRightBlock)));
        }
    }

    private void queueRangeWrapper(BlockPos topLeftBlock, BlockPos bottomRightBlock) {
        queueIsRunning = true;
        shouldEarlyAbortQueuing = false;
        try {
            queueRangeReal(topLeftBlock, bottomRightBlock);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            queueIsRunning = false;
        }
    }

    public void queueRangeReal(BlockPos topLeftBlock, BlockPos bottomRightBlock) {
        final Instant start = Instant.now();
        final ChunkPos topLeft = new ChunkPos(topLeftBlock);
        final ChunkPos bottomRight = new ChunkPos(bottomRightBlock);

        // Cancel current batches
        synchronized (currentBatches) {
            currentBatches.forEach(WorkBatch::cancel);
            currentBatches.clear();
        }
        synchronized (futures) {
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            futures.clear();
        }

        // Calculate new batches
        final List<ChunkPos> chunks = ChunkPos.rangeClosed(topLeft, bottomRight).toList();
        int units = 0;

        // Main biomes
        units += queueForLevel(chunks, topLeftBlock.getY(), 4096, this::workUnitFactory);

        // Structures
        if (config.sampleStructures && !shouldEarlyAbortQueuing) {
            units += queueForLevel(chunks, 0, 256, (pos, y) -> new StructStartWorkUnit(sampleUtils, pos, previewData));
        }

        // Height map
        if (config.sampleHeightmap && !shouldEarlyAbortQueuing && sampleUtils.noiseGeneratorSettings() != null) {
            LongSet queuedChunks = new LongOpenHashSet(chunks.size());
            List<ChunkPos> heightMapChunks = new ArrayList<>(chunks.size());
            final int sectionSizeExponent = PreviewSection.SHIFT - PreviewSection.QUART_TO_SECTION_SHIFT;
            final int numChunks = PreviewSection.SECTION_SIZE >> (sectionSizeExponent - 4);
            for (ChunkPos c : chunks) {
                ChunkPos shifted = new ChunkPos((c.x >> 4) << 4, (c.z >> 4) << 4);
                if (queuedChunks.add(shifted.toLong())) {
                    heightMapChunks.add(shifted);
                }
            }
            units += queueForLevel(heightMapChunks, 0, 1, (pos, y) -> new HeightmapWorkUnit(chunkSampler, sampleUtils, pos, numChunks, previewData));
        } else if (config.sampleHeightmap && !shouldEarlyAbortQueuing) {
            units += queueForLevel(chunks, 0, 64, (pos, y) -> new SlowHeightmapWorkUnit(chunkSampler, sampleUtils, pos, previewData));
        }

        // Intersections
        if (config.sampleIntersections && !shouldEarlyAbortQueuing && sampleUtils.noiseGeneratorSettings() != null) {
            LongSet queuedChunks = new LongOpenHashSet(chunks.size());
            List<ChunkPos> intersectChunks = new ArrayList<>(chunks.size());
            final int sectionSizeExponent = PreviewSection.SHIFT - PreviewSection.QUART_TO_SECTION_SHIFT;
            final int numChunks = PreviewSection.SECTION_SIZE >> (sectionSizeExponent - 4);
            for (ChunkPos c : chunks) {
                ChunkPos shifted = new ChunkPos((c.x >> 4) << 4, (c.z >> 4) << 4);
                if (queuedChunks.add(shifted.toLong())) {
                    intersectChunks.add(shifted);
                }
            }
            units += queueForLevel(intersectChunks, 0, 1, (pos, y) -> new IntersectionWorkUnit(chunkSampler, sampleUtils, pos, numChunks, previewData, Y_BLOCK_STRIDE));
        } else if (config.sampleIntersections && !shouldEarlyAbortQueuing) {
            units += queueForLevel(chunks, 0, 64, (pos, y) -> new SlowIntersectionWorkUnit(chunkSampler, sampleUtils, pos, previewData, yMin(), yMax(), Y_BLOCK_STRIDE));
        }

        // Now sample adjacent levels
        if (config.backgroundSampleVertChunk && !config.buildFullVertChunk) {
            for (int y : genAdjacentYLevels(topLeftBlock.getY())) {
                if (shouldEarlyAbortQueuing) {
                    break;
                }
                units += queueForLevel(chunks, y, 4096, this::workUnitFactory);
            }
        }

        final Instant end = Instant.now();
        LOGGER.info(
                "Queued {} chunks for generation using {} batches [{} ms] {}",
                units,
                currentBatches.size(),
                Duration.between(start, end).abs().toMillis(),
                shouldEarlyAbortQueuing ? "{early abort}" : ""
        );
    }

    private WorkUnit workUnitFactory(ChunkPos pos, int y) {
        if (config.buildFullVertChunk) {
            return new FullChunkWorkUnit(chunkSampler, pos, sampleUtils, previewData, yMin(), yMax(), Y_BLOCK_STRIDE);
        } else {
            return new LayerChunkWorkUnit(chunkSampler, pos, sampleUtils, previewData, y);
        }
    }

    private int queueForLevel(List<ChunkPos> chunks, int y, int maxBatchSize, BiFunction<ChunkPos, Integer, WorkUnit> workUnitFactoryFunc) {
        WorkUnit[] toQueue = new WorkUnit[chunks.size()];
        int size = 0;
        synchronized (completedSynchro) {
            for (ChunkPos chunkPos : chunks) {
                WorkUnit workUnit = workUnitFactoryFunc.apply(chunkPos, y);
                if (workUnit.isCompleted()) {
                    continue;
                }
                toQueue[size++] = workUnit;
            }
        }

        if (size == 0) {
            return 0;
        }

        // Add some randomness
        for (int i = size - 1; i > 1; --i) {
            int randomIndexToSwap = random.nextInt(size);
            WorkUnit temp = toQueue[randomIndexToSwap];
            toQueue[randomIndexToSwap] = toQueue[i];
            toQueue[i] = temp;
        }

        // Batch to reduce threading overhead
        int batchSize = maxBatchSize == 1 ? 1 : Math.max(8, Math.min(maxBatchSize, size / 4096));
        WorkBatch[] batches = new WorkBatch[batchSize == 1 ? size : (size / batchSize) + 1];
        if (batchSize > 1) {
            int batchIdx = 0;
            batches[batchIdx] = new WorkBatch(new ArrayList<>(batchSize), completedSynchro, previewData);
            for (int i = 0; i < size; ++i) {
                batches[batchIdx].workUnits.add(toQueue[i]);
                if (batches[batchIdx].workUnits.size() >= batchSize) {
                    batches[++batchIdx] = new WorkBatch(new ArrayList<>(batchSize), completedSynchro, previewData);
                }
            }
        } else {
            for (int i = 0; i < size; ++i) {
                batches[i] = new WorkBatch(List.of(toQueue[i]), completedSynchro, previewData);
            }
        }

        // Submit and store
        synchronized (futures) {
            for (WorkBatch batch : batches) {
                futures.add(executorService.submit(batch::process));
            }
        }
        synchronized (currentBatches) {
            currentBatches.addAll(Arrays.asList(batches));
        }

        return size;
    }

    private List<Integer> genAdjacentYLevels(int y) {
        final int yMin = yMin();
        final int yMax = yMax();

        final List<Integer> res = new ArrayList<>();

        final int max = dimensionType.height() / Y_BLOCK_STRIDE + 1; // Full height
        for (int i = 1; i <= max; ++i) {
            int y1 = y + i * Y_BLOCK_STRIDE;
            int y2 = y - i * Y_BLOCK_STRIDE;
            if (y2 >= yMin) {
                res.add(y2);
            }
            if (y1 <= yMax) {
                res.add(y1);
            }
            if (y1 > yMax && y2 < yMin) {
                break;
            }
        }

        return res;
    }

    public BiomeSource biomeSource() {
        return biomeSource;
    }

    public RandomState randomState() {
        return randomState;
    }

    public int yMin() {
        return dimensionType.minY();
    }

    public int yMax() {
        return yMin() + dimensionType.height();
    }

    public PreviewStorage previewStorage() {
        return previewStorage;
    }

    public boolean isSetup() {
        return executorService != null;
    }

    public WorldPreviewConfig config() {
        return config;
    }

    /**
     * This resource manager can access images in datapacks, while the
     * one provided in the GUI Minecraft class can't.
     */
    public ResourceManager sampleResourceManager() {
        return sampleUtils.resourceManager();
    }

    public SampleUtils sampleUtils() {
        return sampleUtils;
    }
}
