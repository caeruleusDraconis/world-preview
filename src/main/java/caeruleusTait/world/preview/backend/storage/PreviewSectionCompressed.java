package caeruleusTait.world.preview.backend.storage;

import org.apache.commons.lang3.NotImplementedException;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;

public abstract class PreviewSectionCompressed extends PreviewSection {
    @Serial
    private static final long serialVersionUID = 6458820535476205432L;

    private final int size;

    private short[] data = new short[1];

    private short[] mapData = new short[0];

    /**
     * Number of entries actually in use in {@link #mapData}.
     *
     * <p>For single-value mode ({@code mapData.length == 0}) and no-compression
     * mode ({@code mapData.length == 1}) this field is unused and should be 0.
     */
    private short mapDataCount = 0;

    /**
     * Holds the data and mapData arrays together so that unsynchronized readers
     * always see a consistent pair. The field is volatile so that a single read
     * in {@link #get} obtains both arrays from the same compression generation.
     *
     * <p>Transient because the canonical data lives in {@link #data}/{@link #mapData}
     * which are serialized normally. Reconstructed in {@link #readObject} and the
     * constructor.
     */
    private transient volatile CompressedState state;

    private transient short lastIdx = 0;

    private record CompressedState(short[] data, short[] mapData) {}

    public PreviewSectionCompressed(int quartX, int quartZ, int size) {
        super(quartX, quartZ);
        this.size = size;
        data[0] = Short.MIN_VALUE;
        state = new CompressedState(data, mapData);
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        state = new CompressedState(data, mapData);
    }

    //   ________  _________ _
    //  |_   _|  \/  || ___ \ |
    //    | | | .  . || |_/ / |
    //    | | | |\/| ||  __/| |
    //   _| |_| |  | || |   | |____
    //   \___/\_|  |_/\_|   \_____/
    //

    public static class Full extends PreviewSectionCompressed {
        public Full(int quartX, int quartZ) {
            super(quartX, quartZ, SIZE);
        }

        @Override
        public int xzToIdx(int x, int z) {
            return x * SIZE + z;
        }
    }

    public static class Half extends PreviewSectionCompressed {
        public Half(int quartX, int quartZ) {
            super(quartX, quartZ, HALF_SIZE);
        }

        @Override
        public int xzToIdx(int x, int z) {
            return (x >> HALF_SHIFT) * HALF_SIZE + (z >> HALF_SHIFT);
        }
    }

    public static class Quarter extends PreviewSectionCompressed {
        public Quarter(int quartX, int quartZ) {
            super(quartX, quartZ, SECTION_SIZE);
        }

        @Override
        public int xzToIdx(int x, int z) {
            return (x >> QUART_TO_SECTION_SHIFT) * SECTION_SIZE + (z >> QUART_TO_SECTION_SHIFT);
        }
    }

    //   _     _____ _____ _____ _____
    //  | |   |  _  |  __ \_   _/  __ \
    //  | |   | | | | |  \/ | | | /  \/
    //  | |   | | | | | __  | | | |
    //  | |___\ \_/ / |_\ \_| |_| \__/\
    //  \_____/\___/ \____/\___/ \____/
    //

    public abstract int xzToIdx(int x, int z);

    public short get(int x, int z) {
        final int idx = xzToIdx(x, z);
        // Read the volatile state once to get a consistent (data, mapData) pair.
        final CompressedState snap = state;
        try {
            return getReal(idx, snap.data, snap.mapData);
        } catch (IndexOutOfBoundsException e) {
            return Short.MIN_VALUE;
        }
    }

    private static short getReal(int idx, short[] data, short[] mapData) {
        return switch (mapData.length) {
            // The entire section only contains one single value
            case 0 -> data[0];

            // There is no cache (magic array length 1)
            case 1 -> data[idx];

            // First compression level (oct - 4 unique values | 2 bit per value)
            case 4 -> {
                final short word = data[idx >> 3];
                final int map_idx = (word >> ((idx & 0b111) << 1)) & 0b11;
                yield mapData[map_idx];
            }

            // Second compression level (quart - 16 unique values | 4 bit per value)
            case 16 -> {
                final short word = data[idx >> 2];
                final int map_idx = (word >> ((idx & 0b11) << 2)) & 0b1111;
                yield mapData[map_idx];
            }

            // Third compression level (quart - 256 unique values | 8 bit per value)
            case 256 -> {
                final short word = data[idx >> 1];
                final int map_idx = (word >> ((idx & 0b1) << 3)) & 0b11111111;
                yield mapData[map_idx];
            }
            default -> throw new IllegalStateException("Unexpected value: " + mapData.length);
        };
    }


    private void internalSetData(int x, int z, short value) {
        final CompressedState s = state;
        final int idx = xzToIdx(x, z);
        switch (s.mapData.length) {
            // The entire section only contains one single value
            case 0 -> s.data[0] = value;

            // There is no cache (magic array length 1)
            case 1 -> s.data[idx] = value;

            // First compression level (oct - 4 unique values | 2 bit per value)
            case 4 -> {
                final int didx = idx >> 3;
                final int shift = (idx & 0b111) << 1;
                final int mask = ~(0b11 << shift);
                s.data[didx] = (short) ((s.data[didx] & mask) | (value & 0b11) << shift);
            }

            // Second compression level (quart - 16 unique values | 4 bit per value)
            case 16 -> {
                final int didx = idx >> 2;
                final int shift = (idx & 0b11) << 2;
                final int mask = ~(0b1111 << shift);
                s.data[didx] = (short) ((s.data[didx] & mask) | (value & 0b1111) << shift);
            }

            // Third compression level (quart - 256 unique values | 8 bit per value)
            case 256 -> {
                final int didx = idx >> 1;
                final int shift = (idx & 0b1) << 3;
                final int mask = ~(0b11111111 << shift);
                s.data[didx] = (short) ((s.data[didx] & mask) | (value & 0b11111111) << shift);
            }
            default -> throw new IllegalStateException("Unexpected value: " + s.mapData.length);
        }
    }

    /**
     * Calculates the map index for a specific value. If the value
     * is not already present, the new value is appended to the map.
     * <p>
     * If the map is already full, the compression will be migrated
     * to the next level.
     */
    private short cacheMapIdx(short value) {
        final CompressedState s = state;

        // Check cache
        if (lastIdx < mapDataCount && s.mapData[lastIdx] == value) {
            return lastIdx;
        }

        // Find in existing map
        for (short i = 0; i < mapDataCount; ++i) {
            if (value == s.mapData[i]) {
                return lastIdx = i;
            }
        }

        // Insert if there is room
        if (mapDataCount < s.mapData.length) {
            s.mapData[mapDataCount] = value;
            return lastIdx = mapDataCount++;
        }

        // We need to grow the array (expensive)
        return switch (s.mapData.length) {
            // Grow first level compression to second level compression
            case 4 -> {
                // Grow mapData
                short[] newMapData = Arrays.copyOf(s.mapData, 16);
                newMapData[mapDataCount] = value;
                mapDataCount++;

                // Grow data
                short[] newData = new short[s.data.length * 2];
                for (int i = 0; i < s.data.length; ++i) {
                    final short sv = s.data[i];
                    newData[i * 2 + 0] = (short) ((((sv >> 0) & 0b11) << 0) | (((sv >>  2) & 0b11) << 4) | (((sv >>  4) & 0b11) << 8) | (((sv >>  6) & 0b11) << 12));
                    newData[i * 2 + 1] = (short) ((((sv >> 8) & 0b11) << 0) | (((sv >> 10) & 0b11) << 4) | (((sv >> 12) & 0b11) << 8) | (((sv >> 14) & 0b11) << 12));
                }

                // Publish both arrays atomically via the volatile state field
                data = newData;
                mapData = newMapData;
                state = new CompressedState(newData, newMapData);
                yield (short) (mapDataCount - 1);
            }

            // Grow second level compression to third level compression
            case 16 -> {
                // Grow mapData
                short[] newMapData = Arrays.copyOf(s.mapData, 256);
                newMapData[mapDataCount] = value;
                mapDataCount++;

                // Grow data
                short[] newData = new short[s.data.length * 2];
                for (int i = 0; i < s.data.length; ++i) {
                    final short sv = s.data[i];
                    newData[i * 2 + 0] = (short) ((((sv >> 0) & 0b1111) << 0) | (((sv >>  4) & 0b1111) << 8));
                    newData[i * 2 + 1] = (short) ((((sv >> 8) & 0b1111) << 0) | (((sv >> 12) & 0b1111) << 8));
                }
                // Publish both arrays atomically via the volatile state field
                data = newData;
                mapData = newMapData;
                state = new CompressedState(newData, newMapData);
                yield (short) (mapDataCount - 1);
            }

            // Fully expand third level to no compression
            case 256 -> {
                // Grow data
                short[] newData = new short[s.data.length * 2];
                for (int i = 0; i < s.data.length; ++i) {
                    final short sv = s.data[i];
                    newData[i * 2 + 0] = s.mapData[((sv >> 0) & 0b11111111)];
                    newData[i * 2 + 1] = s.mapData[((sv >> 8) & 0b11111111)];
                }

                short[] newMapData = new short[1]; // There is no cache (magic array length 1)
                mapDataCount = 0;

                // Publish atomically
                data = newData;
                mapData = newMapData;
                state = new CompressedState(newData, newMapData);

                // No more compression --> no map --> no index, just the raw value
                yield value;
            }
            default -> throw new IllegalStateException("Unexpected value: " + s.mapData.length);
        };
    }


    public synchronized void set(int x, int z, short biome) {
        final CompressedState s = state;
        if (s.mapData.length == 0) {
            // Handle single value for entire section

            if (s.data[0] == biome) {
                // Nothing to do — either both are Short.MIN_VALUE (unsampled → unsampled)
                // or both are a real value V (same value → no change needed).
            } else {
                // Different value → expand to first level compression
                short[] newData = new short[(size * size) >> 3];
                short[] newMapData = new short[4];
                newMapData[0] = s.data[0];
                newMapData[1] = biome;
                mapDataCount = 2;
                // Publish both arrays atomically via the volatile state field
                data = newData;
                mapData = newMapData;
                state = new CompressedState(newData, newMapData);
                internalSetData(x, z, (short) 1);
            }
        } else if(s.mapData.length == 1) {
            // Handle no compression

            s.data[xzToIdx(x, z)] = biome;
        } else {
            // Some level of compression

            internalSetData(x, z, cacheMapIdx(biome));
        }
    }

    //   _____ _   _ ______ _____
    //  |_   _| \ | ||  ___|  _  |
    //    | | |  \| || |_  | | | |
    //    | | | . ` ||  _| | | | |
    //   _| |_| |\  || |   \ \_/ /
    //   \___/\_| \_/\_|    \___/
    //

    @Override
    public int size() {
        return size;
    }

    @Override
    public List<PreviewStruct> structures() {
        throw new NotImplementedException();
    }

    @Override
    public void addStructure(PreviewStruct structureData) {
        throw new NotImplementedException();
    }

    public synchronized short mapSize() {
        return mapDataCount;
    }
}
