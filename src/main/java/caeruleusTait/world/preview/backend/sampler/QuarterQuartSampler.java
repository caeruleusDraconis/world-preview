package caeruleusTait.world.preview.backend.sampler;

import caeruleusTait.world.preview.backend.worker.WorkResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;

public class QuarterQuartSampler implements ChunkSampler {
    @Override
    public List<BlockPos> blocksForChunk(ChunkPos chunkPos, int y) {
        final List<BlockPos> res = new ArrayList<>(16);

        final int xMin = SectionPos.sectionToBlockCoord(chunkPos.x, 0);
        final int zMin = SectionPos.sectionToBlockCoord(chunkPos.z, 0);

        for (int x = 0; x < 16; x += QuartPos.SIZE * 2) {
            for (int z = 0; z < 16; z += QuartPos.SIZE * 2) {
                res.add(new BlockPos(xMin + x, y, zMin + z));
            }
        }

        return res;
    }

    @Override
    public void expandRaw(BlockPos pos, short raw, WorkResult result) {
        final int quartX = QuartPos.fromBlock(pos.getX());
        final int quartZ = QuartPos.fromBlock(pos.getZ());
        result.results().add(new WorkResult.BlockResult(quartX + 0, quartZ + 0, raw));
        result.results().add(new WorkResult.BlockResult(quartX + 0, quartZ + 1, raw));
        result.results().add(new WorkResult.BlockResult(quartX + 1, quartZ + 0, raw));
        result.results().add(new WorkResult.BlockResult(quartX + 1, quartZ + 1, raw));
    }

    @Override
    public int blockStride() {
        return QuartPos.SIZE * 2;
    }
}
