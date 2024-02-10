package caeruleusTait.world.preview.backend.storage;

import org.apache.commons.lang3.NotImplementedException;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;

public class PreviewSectionHalf extends PreviewSection {

    @Serial
    private static final long serialVersionUID = 2274224369048667840L;

    private final short[] data = new short[HALF_SIZE * HALF_SIZE];

    public PreviewSectionHalf(int quartX, int quartZ) {
        super(quartX, quartZ);
        Arrays.fill(data, Short.MIN_VALUE);
    }

    @Override
    public short get(int x, int z) {
        return data[(x >> HALF_SHIFT) * HALF_SIZE + (z >> HALF_SHIFT)];
    }

    @Override
    public void set(int x, int z, short biome) {
        data[(x >> HALF_SHIFT) * HALF_SIZE + (z >> HALF_SHIFT)] = biome;
    }

    @Override
    public int size() {
        return HALF_SIZE;
    }

    @Override
    public List<PreviewStruct> structures() {
        throw new NotImplementedException();
    }

    @Override
    public void addStructure(PreviewStruct structureData) {
        throw new NotImplementedException();
    }
}
