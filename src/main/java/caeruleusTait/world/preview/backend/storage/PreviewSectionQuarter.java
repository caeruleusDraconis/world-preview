package caeruleusTait.world.preview.backend.storage;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.List;

public class PreviewSectionQuarter extends PreviewSection {
    private final short[] data = new short[SECTION_SIZE * SECTION_SIZE];

    public PreviewSectionQuarter(int quartX, int quartZ) {
        super(quartX, quartZ);
        Arrays.fill(data, Short.MIN_VALUE);
    }

    @Override
    public short get(int x, int z) {
        return data[(x >> QUART_TO_SECTION_SHIFT) * SECTION_SIZE + (z >> QUART_TO_SECTION_SHIFT)];
    }

    @Override
    public void set(int x, int z, short biome) {
        data[(x >> QUART_TO_SECTION_SHIFT) * SECTION_SIZE + (z >> QUART_TO_SECTION_SHIFT)] = biome;
    }

    @Override
    public int size() {
        return SECTION_SIZE;
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
