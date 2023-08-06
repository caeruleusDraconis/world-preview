package caeruleusTait.world.preview.backend.sampler;

import caeruleusTait.world.preview.backend.worker.WorkResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public interface ChunkSampler {
    List<BlockPos> blocksForChunk(ChunkPos chunkPos, int y);

    void expandRaw(BlockPos pos, short raw, WorkResult result);

    int blockStride();
}
