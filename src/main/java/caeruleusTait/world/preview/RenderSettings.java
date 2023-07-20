package caeruleusTait.world.preview;

import caeruleusTait.world.preview.backend.sampler.ChunkSampler;
import caeruleusTait.world.preview.backend.sampler.QuarterQuartSampler;
import caeruleusTait.world.preview.backend.sampler.FullQuartSampler;
import caeruleusTait.world.preview.backend.sampler.SingleQuartSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntFunction;

import static caeruleusTait.world.preview.backend.WorkManager.Y_BLOCK_STRIDE;

/**
 * Transient settings
 */
public class RenderSettings {
    private BlockPos center = new BlockPos(0, 0, 0);
    private int quartExpand = 1;
    private int quartStride = 1;
    public SamplerType samplerType = SamplerType.AUTO;
    public ResourceLocation dimension = null;

    public boolean hideAllStructures = false;
    public boolean showHeightMap = false;

    public BlockPos center() {
        return center;
    }

    public void setCenter(BlockPos center) {
        this.center = center;
    }

    public void resetCenter() {
        center = new BlockPos(0, WorldPreview.get().workManager().yMax(), 0);
    }

    public void incrementY() {
        int nextY = (Math.min(center.getY() + 16, WorldPreview.get().workManager().yMax()) / Y_BLOCK_STRIDE) * Y_BLOCK_STRIDE;
        center = new BlockPos(center.getX(), nextY, center.getZ());
    }

    public void decrementY() {
        int nextY = (Math.max(center.getY() - 16, WorldPreview.get().workManager().yMin()) / Y_BLOCK_STRIDE) * Y_BLOCK_STRIDE;
        center = new BlockPos(center.getX(), nextY, center.getZ());
    }

    public int quartExpand() {
        return quartExpand;
    }

    public int quartStride() {
        return quartStride;
    }

    public int pixelsPerChunk() {
        return (4 * quartExpand) / quartStride;
    }

    public void setPixelsPerChunk(int blocksPerChunk) {
        switch (blocksPerChunk) {
            case 16 -> {
                quartExpand = 4;
                quartStride = 1;
            }
            case 8 -> {
                quartExpand = 2;
                quartStride = 1;
            }
            case 4 -> {
                quartExpand = 1;
                quartStride = 1;
            }
            case 2 -> {
                quartExpand = 1;
                quartStride = 2;
            }
            case 1 -> {
                quartExpand = 1;
                quartStride = 4;
            }
            default -> throw new RuntimeException("Invalid blocksPerChunk=" + blocksPerChunk);
        }
    }

    public enum SamplerType {
        AUTO(x -> switch (x) {
            case 1 -> new FullQuartSampler();
            case 2 -> new QuarterQuartSampler();
            case 4 -> new SingleQuartSampler();
            default -> throw new RuntimeException("Unsupported quart stride: " + x);
        }),
        FULL(x -> new FullQuartSampler()),
        QUARTER(x -> new QuarterQuartSampler()),
        SINGLE(x -> new SingleQuartSampler()),
        ;

        private final IntFunction<ChunkSampler> samplerFactory;

        SamplerType(IntFunction<ChunkSampler> samplerFactory) {
            this.samplerFactory = samplerFactory;
        }

        public ChunkSampler create(int quartStride) {
            return samplerFactory.apply(quartStride);
        }
    }
}
