package caeruleusTait.world.preview.backend.sampler;

import caeruleusTait.world.preview.backend.worker.WorkResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public class SingleQuartSampler implements ChunkSampler {
    @Override
    public List<BlockPos> blocksForChunk(ChunkPos chunkPos, int y) {
        final int xMin = SectionPos.sectionToBlockCoord(chunkPos.x, 0);
        final int zMin = SectionPos.sectionToBlockCoord(chunkPos.z, 0);

        return List.of(new BlockPos(xMin, y, zMin));
    }

    @Override
    public void expandRaw(BlockPos pos, short raw, WorkResult result) {
        final int quartX = QuartPos.fromBlock(pos.getX());
        final int quartZ = QuartPos.fromBlock(pos.getZ());

        for (int x = 0; x < 16 / QuartPos.SIZE; x++) {
            for (int z = 0; z < 16 / QuartPos.SIZE; z++) {
                result.results().add(new WorkResult.BlockResult(quartX + x, quartZ + z, raw));
            }
        }
    }

    @Override
    public int blockStride() {
        return 16;
    }
}
