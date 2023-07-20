package caeruleusTait.world.preview.backend.storage;

import caeruleusTait.world.preview.RenderSettings;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;

import static caeruleusTait.world.preview.backend.WorkManager.Y_BLOCK_SHIFT;

public class PreviewStorage {

    public static final long FLAG_BITS = 4;
    public static final long FLAG_MASK = (1L << FLAG_BITS) - 1L;

    public static final long XZ_BITS = 30;
    public static final long XZ_MASK = (1L << XZ_BITS) - 1L;
    public static final long XZ_OFFSET = 1L << (XZ_BITS - 1);

    public static final long FLAG_SHIFT = 0L;
    public static final long Z_SHIFT = FLAG_SHIFT + FLAG_BITS;
    public static final long X_SHIFT = Z_SHIFT + XZ_BITS;

    public static final long FLAG_BIOME = 0b0000;
    public static final long FLAG_STRUCT_START = 0b0001;
    public static final long FLAG_HEIGHT = 0b0010;
    public static final long FLAG_STRUCT_REF = 0b1111;

    private final Long2ObjectMap<PreviewSection>[] sections;
    private final RenderSettings renderSettings;

    private final int yMin;
    private final int yMax;

    @SuppressWarnings("unchecked")
    public PreviewStorage(RenderSettings renderSettings, int yMin, int yMax) {
        sections = new Long2ObjectMap[((yMax - yMin) >> Y_BLOCK_SHIFT) + 1];
        for (int i = 0; i < sections.length; ++i) {
            sections[i] = new Long2ObjectOpenHashMap<>(1024, Hash.FAST_LOAD_FACTOR);
        }
        this.renderSettings = renderSettings;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public PreviewSection section4(BlockPos bp, long flags) {
        final int quartX = QuartPos.fromBlock(bp.getX());
        final int indexY = (bp.getY() - yMin) >> Y_BLOCK_SHIFT;
        final int quartZ = QuartPos.fromBlock(bp.getZ());
        synchronized (sections[indexY]) {
            return sections[indexY].computeIfAbsent(quartPosToSectionLong(quartX, quartZ, flags), x -> sectionFactory(quartX, quartZ, flags));
        }
    }

    public PreviewSection section4(ChunkPos chunkPos, int y, long flags) {
        final int quartX = QuartPos.fromSection(chunkPos.x);
        final int indexY = (y - yMin) >> Y_BLOCK_SHIFT;
        final int quartZ = QuartPos.fromSection(chunkPos.z);
        synchronized (sections[indexY]) {
            return sections[indexY].computeIfAbsent(quartPosToSectionLong(quartX, quartZ, flags), x -> sectionFactory(quartX, quartZ, flags));
        }
    }

    public PreviewSection section4(int quartX, int quartY, int quartZ, long flags) {
        final int indexY = (QuartPos.toBlock(quartY) - yMin) >> Y_BLOCK_SHIFT;
        synchronized (sections[indexY]) {
            return sections[indexY].computeIfAbsent(quartPosToSectionLong(quartX, quartZ, flags), x -> sectionFactory(quartX, quartZ, flags));
        }
    }

    private PreviewSection sectionFactory(int quartX, int quartZ, long flags) {
        if (flags == FLAG_STRUCT_START) {
            return new PreviewSectionStructure(quartX, quartZ);
        }
        return switch (renderSettings.quartStride()) {
            case 1 -> new PreviewSectionFull(quartX, quartZ);
            case 2 -> new PreviewSectionHalf(quartX, quartZ);
            case 4 -> new PreviewSectionQuarter(quartX, quartZ);
            default -> throw new IllegalStateException("Unexpected quartStride value: " + renderSettings.quartStride());
        };
    }

    /**
     * Returns {@link Short#MIN_VALUE} when not found. Only use this when querying a single position!
     */
    public short getBiome4(BlockPos bp) {
        final int quartX = QuartPos.fromBlock(bp.getX());
        final int quartY = QuartPos.fromBlock(bp.getY());
        final int quartZ = QuartPos.fromBlock(bp.getZ());
        return getRawData4(quartX, quartY, quartZ, FLAG_BIOME);
    }

    /**
     * Returns {@link Short#MIN_VALUE} when not found. Only use this when querying a single position!
     */
    public short getRawData4(int quartX, int quartY, int quartZ, long flags) {
        final int indexY = (QuartPos.toBlock(quartY) - yMin) >> Y_BLOCK_SHIFT;
        PreviewSection section;
        synchronized (sections[indexY]) {
            section = sections[indexY].get(quartPosToSectionLong(quartX, quartZ, flags));
        }
        if (section == null) {
            return Short.MIN_VALUE;
        }
        return section.get(quartX - section.quartX(), quartZ - section.quartZ());
    }

    public static long blockPos2SectionLong(BlockPos bp, long flags) {
        return quartPosToSectionLong(QuartPos.fromBlock(bp.getX()), QuartPos.fromBlock(bp.getZ()), flags);
    }

    public static long quartPosToSectionLong(long quartX, long quartZ, long flags) {
        final long sX = quartX >> PreviewSection.SHIFT;
        final long sZ = quartZ >> PreviewSection.SHIFT;
        return (sX & XZ_MASK) << X_SHIFT | (sZ & XZ_MASK) << Z_SHIFT | (flags & FLAG_MASK) << FLAG_SHIFT;
    }

    public static long compressXYZ(long x, long z, long flags) {
        return (x & XZ_MASK) << X_SHIFT | (z & XZ_MASK) << Z_SHIFT | (flags & FLAG_MASK) << FLAG_SHIFT;
    }
}
