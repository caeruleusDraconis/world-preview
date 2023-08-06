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

public class LayerChunkWorkUnit extends WorkUnit {
    private final ChunkSampler sampler;

    public LayerChunkWorkUnit(ChunkSampler sampler, ChunkPos pos, SampleUtils sampleUtils, PreviewData previewData, int y) {
        super(sampleUtils, pos, previewData, y);
        this.sampler = sampler;
    }

    @Override
    protected List<WorkResult> doWork() {
        WorkResult res = new WorkResult(this, QuartPos.fromBlock(y), primarySection, new ArrayList<>(16), List.of());
        for (BlockPos p : sampler.blocksForChunk(chunkPos, y)) {
            ResourceKey<Biome> biome = sampleUtils.doSample(p);
            sampler.expandRaw(p, biomeIdFrom(biome), res);
        }
        return List.of(res);
    }

    @Override
    public long flags() {
        return PreviewStorage.FLAG_BIOME;
    }
}
