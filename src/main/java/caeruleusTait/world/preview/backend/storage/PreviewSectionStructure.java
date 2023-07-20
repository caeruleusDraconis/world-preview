package caeruleusTait.world.preview.backend.storage;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class PreviewSectionStructure extends PreviewSection {
    private final List<PreviewStruct> structures = new ArrayList<>();

    public PreviewSectionStructure(int quartX, int quartZ) {
        super(quartX, quartZ);
    }

    @Override
    public synchronized List<PreviewStruct> structures() {
        return new ArrayList<>(structures);
    }

    @Override
    public synchronized void addStructure(PreviewStruct structureData) {
        structures.add(structureData);
    }

    @Override
    public short get(int x, int z) {
        throw new NotImplementedException();
    }

    @Override
    public void set(int x, int z, short biome) {
        throw new NotImplementedException();
    }

    @Override
    public int size() {
        return structures.size();
    }
}
