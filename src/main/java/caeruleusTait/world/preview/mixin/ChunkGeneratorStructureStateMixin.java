package caeruleusTait.world.preview.mixin;

import caeruleusTait.world.preview.WorldPreview;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGeneratorStructureState.class)
public abstract class ChunkGeneratorStructureStateMixin {

    @Shadow
    @Final
    @Mutable
    private Map<Structure, List<StructurePlacement>> placementsForStructure;

    @Shadow
    @Final
    @Mutable
    private Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> ringPositions;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void threadSafeFixup(RandomState randomState, BiomeSource biomeSource, long l, long m, List list, CallbackInfo ci) {
        placementsForStructure = Object2ObjectMaps.synchronize((Object2ObjectMap<Structure, List<StructurePlacement>>) placementsForStructure);
        ringPositions = Object2ObjectMaps.synchronize((Object2ObjectMap<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>>) ringPositions);
    }

}
