package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.sampler.ChunkSampler;
import caeruleusTait.world.preview.backend.storage.PreviewStorage;
import caeruleusTait.world.preview.mixin.NoiseChunkAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class IntersectionWorkUnit extends WorkUnit {
    private final ChunkSampler sampler;
    private final int numChunks;
    private final int yStride;

    public IntersectionWorkUnit(
            ChunkSampler sampler,
            SampleUtils sampleUtils,
            ChunkPos chunkPos,
            int numChunks,
            PreviewData previewData,
            int yStride
    ) {
        super(sampleUtils, chunkPos, previewData, 0);
        this.sampler = sampler;
        this.numChunks = numChunks;
        this.yStride = yStride;
    }

    private record XZPair(int x, double dX, int z, double dZ) {
        // record
    }

    @Override
    protected List<WorkResult> doWork() {
        final NoiseGeneratorSettings noiseGeneratorSettings = sampleUtils.noiseGeneratorSettings();

        if (noiseGeneratorSettings == null) {
            return List.of();
        }

        final NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
        final NoiseChunk noiseChunk = sampleUtils.getNoiseChunk(chunkPos, numChunks, true);
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        final int yMin = noiseSettings.minY();
        final int yMax = yMin + noiseSettings.height();
        final int cellWidth = noiseSettings.getCellWidth();
        final int cellHeight = noiseSettings.getCellHeight();

        final int cellMinY = Mth.floorDiv(yMin, noiseSettings.getCellHeight());
        final int cellCountY = Mth.floorDiv(noiseSettings.height(), noiseSettings.getCellHeight());

        final int minBlockX = chunkPos.getMinBlockX();
        final int minBlockZ = chunkPos.getMinBlockZ();
        final int cellCountXZ = (16 * numChunks) / cellWidth;
        final int cellStrideXZ = Math.max(1, sampler.blockStride() / cellWidth);
        final int todoArraySize = Math.max(1, cellWidth / sampler.blockStride()) * Math.max(1, cellWidth / sampler.blockStride());

        final List<WorkResult> results = new ArrayList<>((yMax - yMin) / yStride);

        // Initialize the results for each y-level
        for (int y = yMin; y <= yMax; y += yStride) {
            results.add(
                    new WorkResult(
                            this,
                            QuartPos.fromBlock(y),
                            y == this.y ? primarySection : storage.section4(chunkPos, y, flags()),
                            new ArrayList<>(numChunks * numChunks * 4 * 4),
                            List.of()
                    )
            );
        }

        noiseChunk.initializeForFirstCellX();

        try {
            // Iterate over cell X Z Y
            for(int cellX = 0; cellX < cellCountXZ && !isCanceled(); cellX += cellStrideXZ) {
                noiseChunk.advanceCellX(cellX);

                for(int cellZ = 0; cellZ < cellCountXZ && !isCanceled(); cellZ += cellStrideXZ) {

                    List<XZPair> positions = new ArrayList<>(todoArraySize);
                    for (int xInCell = 0; xInCell < cellWidth; xInCell += sampler.blockStride()) {
                        for (int zInCell = 0; zInCell < cellWidth; zInCell += sampler.blockStride()) {
                            int x = minBlockX + cellX * cellWidth + xInCell;
                            int z = minBlockZ + cellZ * cellWidth + zInCell;
                            positions.add(new XZPair(
                                    x, (double) xInCell / (double) cellWidth,
                                    z, (double) zInCell / (double) cellWidth
                            ));
                        }
                    }

                    int lastCellY = Integer.MIN_VALUE;
                    for (int yTemp = yMin; yTemp <= yMax; yTemp += yStride) {
                        final int y = Math.min(yTemp, yMax - 1);
                        final int cellY = Math.min(Math.floorDiv(y - yMin, cellHeight), cellCountY - 1);
                        final int yInCell = y % cellHeight;
                        if (cellY != lastCellY) {
                            noiseChunk.selectCellYZ(cellY, cellZ);
                        }
                        noiseChunk.updateForY(y, (double) yInCell / (double) cellHeight);
                        lastCellY = cellY;

                        final WorkResult res = results.get((yTemp - yMin) / yStride);
                        for (XZPair curr : positions) {
                            noiseChunk.updateForX(curr.x, curr.dX);
                            noiseChunk.updateForZ(curr.z, curr.dZ);

                            BlockState blockState = ((NoiseChunkAccessor) noiseChunk).invokeGetInterpolatedState();
                            if (blockState == null) {
                                blockState = noiseGeneratorSettings.defaultBlock();
                            }

                            mutableBlockPos.set(curr.x, yTemp, curr.z);
                            sampler.expandRaw(mutableBlockPos, (short) blockState.getMapColor(null, null).id, res);
                        }
                    }
                }

                // Whatever this does, but it is required...
                noiseChunk.swapSlices();
            }
        } finally {
            noiseChunk.stopInterpolation();
        }

        return results;
    }

    @Override
    public long flags() {
        return PreviewStorage.FLAG_INTERSECT;
    }
}
