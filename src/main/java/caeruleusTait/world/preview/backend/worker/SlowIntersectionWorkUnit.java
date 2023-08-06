package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.sampler.ChunkSampler;
import caeruleusTait.world.preview.backend.storage.PreviewStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SlowIntersectionWorkUnit extends WorkUnit {
    private final ChunkSampler sampler;
    private final int yMin;
    private final int yMax;
    private final int yStride;

    public SlowIntersectionWorkUnit(
            ChunkSampler sampler,
            SampleUtils sampleUtils,
            ChunkPos chunkPos,
            PreviewData previewData,
            int yMin,
            int yMax,
            int yStride
    ) {
        super(sampleUtils, chunkPos, previewData, 0);
        this.sampler = sampler;
        this.yMin = yMin;
        this.yMax = yMax;
        this.yStride = yStride;
    }

    @Override
    protected List<WorkResult> doWork() {
        final List<WorkResult> results = new ArrayList<>((yMax - yMin) / yStride);

        // Initialize the results for each y-level
        for (int y = yMin; y <= yMax; y += yStride) {
            results.add(
                    new WorkResult(
                            this,
                            QuartPos.fromBlock(y),
                            y == this.y ? primarySection : storage.section4(chunkPos, y, flags()),
                            new ArrayList<>(16),
                            List.of()
                    )
            );
        }

        // Do the actual work
        for (BlockPos p : sampler.blocksForChunk(chunkPos, 0)) {
            if (isCanceled()) {
                break;
            }
            final NoiseColumn nc = sampleUtils.doIntersectionsSlow(p);
            short lastColorId = 0;
            for (int y = yMin; y <= yMax; y += yStride) {
                final WorkResult res = results.get((y - yMin) / yStride);
                final BlockState bs = nc.getBlock(y);

                // See through one layer of air logic
                short currId = (short) bs.getMapColor(null, null).id;
                if (currId == 0 && lastColorId > 0) {
                    currId = (short) -lastColorId;
                    lastColorId = 0;
                } else {
                    lastColorId = currId;
                }

                // Set value
                sampler.expandRaw(p, currId, res);
            }
        }
        return results;
    }

    @Override
    public long flags() {
        return PreviewStorage.FLAG_INTERSECT;
    }
}
