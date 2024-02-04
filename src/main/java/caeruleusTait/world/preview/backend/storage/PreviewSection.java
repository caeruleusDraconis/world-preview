package caeruleusTait.world.preview.backend.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.List;

public abstract class PreviewSection implements Serializable {
    public static final int SHIFT = 8;
    public static final int SIZE = 1 << SHIFT;
    public static final int OFFSET = 1 << (SHIFT - 1);
    public static final int MASK = -SIZE;

    public static final int HALF_SHIFT = 1;
    public static final int HALF_SIZE = SIZE >> HALF_SHIFT;

    // SectionPos == ChunkPos
    public static final int QUART_TO_SECTION_SHIFT = 2;
    public static final int SECTION_SIZE = SIZE >> QUART_TO_SECTION_SHIFT;

    private final int quartX;
    private final int quartZ;

    private final int chunkX;
    private final int chunkZ;

    private final BitSet completed = new BitSet(SECTION_SIZE * HALF_SIZE);

    protected PreviewSection(int quartX, int quartZ) {
        this.quartX = quartX & MASK;
        this.quartZ = quartZ & MASK;
        this.chunkX = this.quartX >> QUART_TO_SECTION_SHIFT;
        this.chunkZ = this.quartZ >> QUART_TO_SECTION_SHIFT;
    }

    public abstract int size();

    public abstract short get(int x, int z);

    public abstract void set(int x, int z, short biome);

    public abstract List<PreviewStruct> structures();

    public abstract void addStructure(PreviewStruct structureData);

    /**
     * Chunk coords
     */
    public synchronized boolean isCompleted(ChunkPos chunkPos) {
        return completed.get((chunkPos.x - chunkX) * SECTION_SIZE + (chunkPos.z - chunkZ));
    }

    /**
     * Chunk coords
     */
    public synchronized void markCompleted(ChunkPos chunkPos) {
        completed.set((chunkPos.x - chunkX) * SECTION_SIZE + (chunkPos.z - chunkZ));
    }

    public AccessData calcQuartOffsetData(int minQuartX, int minQuartZ, int maxQuartX, int maxQuartZ) {
        final int accessMinX = minQuartX - quartX;
        final int accessMinZ = minQuartZ - quartZ;
        final int accessMaxX = maxQuartX - quartX;
        final int accessMaxZ = maxQuartZ - quartZ;
        return new AccessData(
                accessMinX,
                accessMinZ,
                Math.min(accessMaxX, SIZE),
                Math.min(accessMaxZ, SIZE),
                accessMaxX > SIZE,
                accessMaxZ > SIZE
        );
    }

    public int quartX() {
        return quartX;
    }

    public int quartZ() {
        return quartZ;
    }

    public int blockX() {
        return QuartPos.toBlock(quartX);
    }

    public int blockZ() {
        return QuartPos.toBlock(quartZ);
    }

    public record AccessData(int minX, int minZ, int maxX, int maxZ, boolean continueX, boolean continueZ) implements Serializable {
    }

    public record PreviewStruct(BlockPos center, short structureId, BoundingBox boundingBox) {
    }
}
