package caeruleusTait.world.preview;

import caeruleusTait.world.preview.backend.sampler.ChunkSampler;
import caeruleusTait.world.preview.backend.sampler.FullQuartSampler;
import caeruleusTait.world.preview.backend.sampler.QuarterQuartSampler;
import caeruleusTait.world.preview.backend.sampler.SingleQuartSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.IntFunction;

import static caeruleusTait.world.preview.backend.WorkManager.Y_BLOCK_STRIDE;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_BIOME;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_HEIGHT;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_INTERSECT;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_NOISE_CONTINENTALNESS;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_NOISE_DEPTH;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_NOISE_EROSION;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_NOISE_HUMIDITY;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_NOISE_TEMPERATURE;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_NOISE_WEIRDNESS;

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
    public transient RenderMode mode = RenderMode.BIOMES;
    public transient RenderMode lastNoise = RenderMode.NOISE_TEMPERATURE;

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
        int nextY = (Math.min(center.getY() + Y_BLOCK_STRIDE, WorldPreview.get().workManager().yMax()) / Y_BLOCK_STRIDE) * Y_BLOCK_STRIDE;
        center = new BlockPos(center.getX(), nextY, center.getZ());
    }

    public void decrementY() {
        int nextY = (Math.max(center.getY() - Y_BLOCK_STRIDE, WorldPreview.get().workManager().yMin()) / Y_BLOCK_STRIDE) * Y_BLOCK_STRIDE;
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

    public enum RenderMode {
        BIOMES(FLAG_BIOME, true),
        HEIGHTMAP(FLAG_HEIGHT, false),
        INTERSECTIONS(FLAG_INTERSECT, true),

        NOISE_TEMPERATURE(FLAG_NOISE_TEMPERATURE, true),
        NOISE_HUMIDITY(FLAG_NOISE_HUMIDITY, true),
        NOISE_CONTINENTALNESS(FLAG_NOISE_CONTINENTALNESS, true),
        NOISE_EROSION(FLAG_NOISE_EROSION, true),
        NOISE_DEPTH(FLAG_NOISE_DEPTH, true),
        NOISE_WEIRDNESS(FLAG_NOISE_WEIRDNESS, true),
        NOISE_PEAKS_AND_VALLEYS(FLAG_NOISE_WEIRDNESS, true),

        ;

        public final long flag;
        public final boolean useY;

        RenderMode(long flag, boolean useY) {
            this.flag = flag;
            this.useY = useY;
        }

        public boolean isNoise() {
            return this.name().startsWith("NOISE");
        }

        public Component toComponent() {
            return Component.literal(name().replace("NOISE_", ""));
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
