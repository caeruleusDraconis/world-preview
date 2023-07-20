package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.backend.color.PreviewData;
import caeruleusTait.world.preview.backend.storage.PreviewSection;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;

public class WorkBatch {
    public final List<WorkUnit> workUnits;
    private final Object completedSynchro;
    private final PreviewData previewData;
    private boolean isCanceled = false;

    public WorkBatch(List<WorkUnit> workUnits, Object completedSynchro, PreviewData previewData) {
        this.workUnits = workUnits;
        this.completedSynchro = completedSynchro;
        this.previewData = previewData;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void cancel() {
        isCanceled = true;
        workUnits.forEach(WorkUnit::cancel);
    }

    public void process() {
        try {
            if (isCanceled()) {
                return;
            }

            List<WorkResult> res = new ArrayList<>();
            for (WorkUnit unit : workUnits) {
                res.addAll(unit.work());
                if (isCanceled()) {
                    return;
                }
            }

            // Mark as completed early to avoid duplicate work
            synchronized (completedSynchro) {
                workUnits.forEach(WorkUnit::markCompleted);
            }

            applyChunkResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyChunkResult(List<WorkResult> workResultList) {
        try {
            for (WorkResult workResult : workResultList) {
                if (workResult == null) {
                    return;
                }

                final ChunkPos chunkPos = workResult.workUnit().chunk();
                final int qStartX = QuartPos.fromSection(chunkPos.x);
                final int qStartZ = QuartPos.fromSection(chunkPos.z);

                PreviewSection section = workResult.section();
                PreviewSection.AccessData offsetData = section.calcQuartOffsetData(qStartX, qStartZ, qStartX + 4, qStartZ + 4);

                // Assume that one chunk always fits
                for (var x : workResult.results()) {
                    section.set(
                            offsetData.minX() + x.quartX() - qStartX,
                            offsetData.minZ() + x.quartZ() - qStartZ,
                            x.value()
                    );
                }

                for (Pair<ResourceLocation, StructureStart> x : workResult.structures()) {
                    StructureStart structureStart = x.getSecond();
                    short id = previewData.struct2Id().getShort(x.getFirst().toString());
                    section.addStructure(new PreviewSection.PreviewStruct(
                            structureStart.getBoundingBox().getCenter(),
                            id,
                            structureStart.getBoundingBox()
                    ));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
