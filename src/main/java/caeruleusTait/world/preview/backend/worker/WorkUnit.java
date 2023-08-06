package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.backend.WorkManager;
import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.storage.PreviewSection;
import caeruleusTait.world.preview.backend.storage.PreviewStorage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public abstract class WorkUnit {
    protected final WorkManager workManager;
    protected final SampleUtils sampleUtils;
    protected final PreviewStorage storage;
    protected final PreviewSection primarySection;
    protected final ChunkPos chunkPos;
    protected final PreviewData previewData;
    protected final int y;
    private boolean isCanceled;

    protected WorkUnit(SampleUtils sampleUtils, ChunkPos chunkPos, PreviewData previewData, int y) {
        this.workManager = WorldPreview.get().workManager();
        this.sampleUtils = sampleUtils;
        this.storage = workManager.previewStorage();
        this.primarySection = storage.section4(chunkPos, y, flags());
        this.chunkPos = chunkPos;
        this.previewData = previewData;
        this.y = y;
    }

    public short biomeIdFrom(ResourceKey<Biome> resourceKey) {
        return previewData.biome2Id().getShort(resourceKey.location().toString());
    }
    public short biomeIdFrom(ResourceLocation location) {
        return previewData.biome2Id().getShort(location.toString());
    }

    /**
     * Return {@code true} on successful completion
     */
    protected abstract List<WorkResult> doWork();

    public abstract long flags();

    public boolean isCompleted() {
        return primarySection.isCompleted(chunkPos);
    }

    public void markCompleted() {
        primarySection.markCompleted(chunkPos);
    }

    public List<WorkResult> work() {
        try {
            return doWork();
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ChunkPos chunk() {
        return chunkPos;
    }

    public int y() {
        return y;
    }

    public void cancel() {
        isCanceled = true;
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
