package caeruleusTait.world.preview.backend.sampler;

import caeruleusTait.world.preview.backend.worker.WorkResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;

public class FullQuartSampler implements ChunkSampler {
    @Override
    public List<BlockPos> blocksForChunk(ChunkPos chunkPos, int y) {
        final List<BlockPos> res = new ArrayList<>(16);

        final int xMin = SectionPos.sectionToBlockCoord(chunkPos.x, 0);
        final int zMin = SectionPos.sectionToBlockCoord(chunkPos.z, 0);

        for (int x = 0; x < 16; x += QuartPos.SIZE) {
            for (int z = 0; z < 16; z += QuartPos.SIZE) {
                res.add(new BlockPos(xMin + x, y, zMin + z));
            }
        }

        return res;
    }

    @Override
    public void expandRaw(BlockPos pos, short raw, WorkResult result) {
        final int quartX = QuartPos.fromBlock(pos.getX());
        final int quartZ = QuartPos.fromBlock(pos.getZ());
        result.results().add(new WorkResult.BlockResult(quartX, quartZ, raw));
    }

    @Override
    public int blockStride() {
        return QuartPos.SIZE;
    }
}
