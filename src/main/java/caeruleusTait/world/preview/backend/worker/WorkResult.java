package caeruleusTait.world.preview.backend.worker;

import caeruleusTait.world.preview.backend.storage.PreviewSection;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.List;

public record WorkResult(
        WorkUnit workUnit,
        int quartY,
        PreviewSection section,
        List<BlockResult> results,
        List<com.mojang.datafixers.util.Pair<net.minecraft.resources.ResourceLocation, StructureStart>> structures
) {

    public record BlockResult(int quartX, int quartZ, short value) {}
}
