package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.sampler.ChunkSampler;
import caeruleusTait.world.preview.backend.storage.PreviewStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public class SlowHeightmapWorkUnit extends WorkUnit {
    private final ChunkSampler sampler;

    public SlowHeightmapWorkUnit(ChunkSampler sampler, SampleUtils sampleUtils, ChunkPos chunkPos, PreviewData previewData) {
        super(sampleUtils, chunkPos, previewData, 0);
        this.sampler = sampler;
    }

    @Override
    protected List<WorkResult> doWork() {
        WorkResult res = new WorkResult(this, QuartPos.fromBlock(0), primarySection, new ArrayList<>(16), List.of());
        for (BlockPos p : sampler.blocksForChunk(chunkPos, y)) {
            sampler.expandRaw(p, sampleUtils.doHeightSlow(p), res);
        }
        return List.of(res);
    }

    @Override
    public long flags() {
        return PreviewStorage.FLAG_HEIGHT;
    }
}
