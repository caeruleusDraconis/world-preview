package caeruleusTait.world.preview.backend.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.NotImplementedException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PreviewSectionStructure extends PreviewSection {
    @Serial
    private static final long serialVersionUID = -3170004481651979127L;

    private transient List<PreviewStruct> structures = new ArrayList<>();

    record SerializablePreviewStruct(int cX, int cY, int cZ,
                                     short structureId,
                                     int bbMinX, int bbMinY, int bbMinZ,
                                     int bbMaxX, int bbMaxY, int bbMaxZ) implements Serializable {
        static SerializablePreviewStruct fromStruct(PreviewStruct s) {
            final BlockPos c = s.center();
            final BoundingBox bb = s.boundingBox();
            return new SerializablePreviewStruct(
                    c.getX(), c.getY(), c.getZ(),
                    s.structureId(),
                    bb.minX(), bb.minY(), bb.minZ(),
                    bb.maxX(), bb.maxY(), bb.maxZ()
            );
        }

        PreviewStruct toStruct() {
            return new PreviewStruct(
                    new BlockPos(cX, cY, cZ),
                    structureId,
                    new BoundingBox(
                            bbMinX, bbMinY, bbMinZ,
                            bbMaxX, bbMaxY, bbMaxZ
                    )
            );
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        final SerializablePreviewStruct[] res = structures
                .stream()
                .map(SerializablePreviewStruct::fromStruct)
                .toArray(SerializablePreviewStruct[]::new);
        oos.writeObject(res);
    }

    @Serial
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        final SerializablePreviewStruct[] res = (SerializablePreviewStruct[]) ois.readObject();
        structures = Arrays.stream(res).map(SerializablePreviewStruct::toStruct).collect(Collectors.toList());
    }

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
