package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.sampler.ChunkSampler;
import caeruleusTait.world.preview.backend.storage.PreviewStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class FullChunkWorkUnit extends WorkUnit {
    private final ChunkSampler sampler;
    private final int yMin;
    private final int yMax;
    private final int yStride;

    public FullChunkWorkUnit(ChunkSampler sampler, ChunkPos pos, SampleUtils sampleUtils, PreviewData previewData, int yMin, int yMax, int yStride) {
        super(sampleUtils, pos, previewData, 0);
        this.sampler = sampler;
        this.yMin = yMin;
        this.yMax = yMax;
        this.yStride = yStride;
    }

    @Override
    protected List<WorkResult> doWork() {
        List<WorkResult> results = new ArrayList<>((yMax - yMin) / yStride);
        for (int y = yMin; y <= yMax; y += yStride) {
            WorkResult res = new WorkResult(
                    this,
                    QuartPos.fromBlock(y),
                    y == this.y ? primarySection : storage.section4(chunkPos, y, flags()),
                    new ArrayList<>(16),
                    List.of()
            );
            for (BlockPos p : sampler.blocksForChunk(chunkPos, y)) {
                ResourceKey<Biome> biome = sampleUtils.doSample(p);
                sampler.expandRaw(p, biomeIdFrom(biome), res);
            }
            results.add(res);
        }
        return results;
    }

    @Override
    public long flags() {
        return PreviewStorage.FLAG_BIOME;
    }

}
