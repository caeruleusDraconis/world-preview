package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.storage.PreviewStorage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.List;

public class StructStartWorkUnit extends WorkUnit {
    public StructStartWorkUnit(SampleUtils sampleUtils, ChunkPos pos, PreviewData previewData) {
        super(sampleUtils, pos, previewData, 0);
    }

    @Override
    protected List<WorkResult> doWork() {
        List<Pair<ResourceLocation, StructureStart>> res = sampleUtils.doStructures(chunkPos);
        return List.of(
                new WorkResult(
                        this,
                        0,
                        primarySection,
                        List.of(),
                        res
                )
        );
    }

    @Override
    public long flags() {
        return PreviewStorage.FLAG_STRUCT_START;
    }
}
