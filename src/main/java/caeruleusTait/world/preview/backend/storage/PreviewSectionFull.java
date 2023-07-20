package caeruleusTait.world.preview.backend.storage;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.List;

public class PreviewSectionFull extends PreviewSection {
    private final short[] data = new short[SIZE * SIZE];

    public PreviewSectionFull(int quartX, int quartZ) {
        super(quartX, quartZ);
        Arrays.fill(data, Short.MIN_VALUE);
    }

    public short get(int x, int z) {
        return data[x * SIZE + z];
    }

    public void set(int x, int z, short biome) {
        data[x * SIZE + z] = biome;
    }

    @Override
    public int size() {
        return SIZE;
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
