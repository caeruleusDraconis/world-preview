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
        if (sampleUtils.hasRawNoiseInfo()) {
            return doRawNoiseWork();
        } else {
            return doNormalWork();
        }
    }

    private List<WorkResult> doRawNoiseWork() {
        List<WorkResult> results = new ArrayList<>((yMax - yMin) / yStride);
        for (int y = yMin; y <= yMax; y += yStride) {
            WorkResult res             = new WorkResult(this, QuartPos.fromBlock(y), y == this.y ? primarySection : storage.section4(chunkPos, y, flags()),    new ArrayList<>(16), List.of());
            WorkResult temperature     = new WorkResult(this, QuartPos.fromBlock(y), storage.section4(chunkPos, y, PreviewStorage.FLAG_NOISE_TEMPERATURE),     new ArrayList<>(16), List.of());
            WorkResult humidity        = new WorkResult(this, QuartPos.fromBlock(y), storage.section4(chunkPos, y, PreviewStorage.FLAG_NOISE_HUMIDITY),        new ArrayList<>(16), List.of());
            WorkResult continentalness = new WorkResult(this, QuartPos.fromBlock(y), storage.section4(chunkPos, y, PreviewStorage.FLAG_NOISE_CONTINENTALNESS), new ArrayList<>(16), List.of());
            WorkResult erosion         = new WorkResult(this, QuartPos.fromBlock(y), storage.section4(chunkPos, y, PreviewStorage.FLAG_NOISE_EROSION),         new ArrayList<>(16), List.of());
            WorkResult depth           = new WorkResult(this, QuartPos.fromBlock(y), storage.section4(chunkPos, y, PreviewStorage.FLAG_NOISE_DEPTH),           new ArrayList<>(16), List.of());
            WorkResult weirdness       = new WorkResult(this, QuartPos.fromBlock(y), storage.section4(chunkPos, y, PreviewStorage.FLAG_NOISE_WEIRDNESS),       new ArrayList<>(16), List.of());
            for (BlockPos p : sampler.blocksForChunk(chunkPos, y)) {
                final var sample = sampleUtils.doSample(p);
                sampler.expandRaw(p, biomeIdFrom(sample.biome()), res);
                if(sample.noiseResult() != null) {
                    sampler.expandRaw(p, sample.noiseResult()[0], temperature);
                    sampler.expandRaw(p, sample.noiseResult()[1], humidity);
                    sampler.expandRaw(p, sample.noiseResult()[2], continentalness);
                    sampler.expandRaw(p, sample.noiseResult()[3], erosion);
                    sampler.expandRaw(p, sample.noiseResult()[4], depth);
                    sampler.expandRaw(p, sample.noiseResult()[5], weirdness);
                }
            }
            results.add(res);
            results.add(temperature);
            results.add(humidity);
            results.add(continentalness);
            results.add(erosion);
            results.add(depth);
            results.add(weirdness);
        }
        return results;
    }

    private List<WorkResult> doNormalWork() {
        List<WorkResult> results = new ArrayList<>(((yMax - yMin) / yStride) * 7);
        for (int y = yMin; y <= yMax; y += yStride) {
            WorkResult res = new WorkResult(
                    this,
                    QuartPos.fromBlock(y),
                    y == this.y ? primarySection : storage.section4(chunkPos, y, flags()),
                    new ArrayList<>(16),
                    List.of()
            );
            for (BlockPos p : sampler.blocksForChunk(chunkPos, y)) {
                final var sample = sampleUtils.doSample(p);
                sampler.expandRaw(p, biomeIdFrom(sample.biome()), res);
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
