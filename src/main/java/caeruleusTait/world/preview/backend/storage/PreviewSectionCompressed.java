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

    private transient short lastIdx = 0;

    public PreviewSectionCompressed(int quartX, int quartZ, int size) {
        super(quartX, quartZ);
        this.size = size;
        data[0] = Short.MIN_VALUE;
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
        // Using synchronized is expensive. Solution: Only require eventual correctness for
        // reading and write the compression change in such a way to decrease the risk of
        // IndexOutOfBoundsException as much as possible.
        //
        // If still something goes wrong, catch the IndexOutOfBoundsException and return MIN_VALUE.
        try {
            return getReal(idx);
        } catch (IndexOutOfBoundsException e) {
            return Short.MIN_VALUE;
        }
    }

    private short getReal(int idx) {
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
        final int idx = xzToIdx(x, z);
        switch (mapData.length) {
            // The entire section only contains one single value
            case 0 -> data[0] = value;

            // There is no cache (magic array length 1)
            case 1 -> data[idx] = value;

            // First compression level (oct - 4 unique values | 2 bit per value)
            case 4 -> {
                final int didx = idx >> 3;
                final int shift = (idx & 0b111) << 1;
                final int mask = ~(0b11 << shift);
                data[didx] = (short) ((data[didx] & mask) | (value & 0b11) << shift);
            }

            // Second compression level (quart - 16 unique values | 4 bit per value)
            case 16 -> {
                final int didx = idx >> 2;
                final int shift = (idx & 0b11) << 2;
                final int mask = ~(0b1111 << shift);
                data[didx] = (short) ((data[didx] & mask) | (value & 0b1111) << shift);
            }

            // Third compression level (quart - 256 unique values | 8 bit per value)
            case 256 -> {
                final int didx = idx >> 1;
                final int shift = (idx & 0b1) << 3;
                final int mask = ~(0b11111111 << shift);
                data[didx] = (short) ((data[didx] & mask) | (value & 0b11111111) << shift);
            }
            default -> throw new IllegalStateException("Unexpected value: " + mapData.length);
        }
    }

    /**
     * Calculates the {@link #mapData} index for a specific value. If the value
     * is not already present, the new value is appended to the map
     * <p>
     * If the {@link #mapData} is already full, the compression will be migrated
     * to the next level.
     */
    private short cacheMapIdx(short value) {
        // Check cache
        if (mapData[lastIdx] == value) {
            return lastIdx;
        }

        // Find or insert in existing map
        for (short i = 0; i < mapData.length; ++i) {
            if (value == mapData[i]) {
                return lastIdx = i;
            } else if (mapData[i] == Short.MIN_VALUE) {
                mapData[i] = value;
                return lastIdx = i;
            }
        }

        // We need to grow the array (expensive)
        return switch (mapData.length) {
            // Grow first level compression to second level compression
            case 4 -> {
                // Grow mapData
                short[] newMapData = Arrays.copyOf(mapData, 16);
                newMapData[4] = value;
                Arrays.fill(newMapData, 5, 16, Short.MIN_VALUE);

                // Grow data
                short[] newData = new short[data.length * 2];
                for (int i = 0; i < data.length; ++i) {
                    final short s = data[i];
                    newData[i * 2 + 0] = (short) ((((s >> 0) & 0b11) << 0) | (((s >>  2) & 0b11) << 4) | (((s >>  4) & 0b11) << 8) | (((s >>  6) & 0b11) << 12));
                    newData[i * 2 + 1] = (short) ((((s >> 8) & 0b11) << 0) | (((s >> 10) & 0b11) << 4) | (((s >> 12) & 0b11) << 8) | (((s >> 14) & 0b11) << 12));
                }

                // Make the change as "atomic" as possible to reduce the risk of `IndexOutOfBoundsException`s
                mapData = newMapData;
                data = newData;
                yield 4;
            }

            // Grow second level compression to third level compression
            case 16 -> {
                // Grow mapData
                short[] newMapData = Arrays.copyOf(mapData, 256);
                newMapData[16] = value;
                Arrays.fill(newMapData, 17, 256, Short.MIN_VALUE);

                // Grow data
                short[] newData = new short[data.length * 2];
                for (int i = 0; i < data.length; ++i) {
                    final short s = data[i];
                    newData[i * 2 + 0] = (short) ((((s >> 0) & 0b1111) << 0) | (((s >>  4) & 0b1111) << 8));
                    newData[i * 2 + 1] = (short) ((((s >> 8) & 0b1111) << 0) | (((s >> 12) & 0b1111) << 8));
                }
                // Make the change as "atomic" as possible to reduce the risk of `IndexOutOfBoundsException`s
                mapData = newMapData;
                data = newData;
                yield 16;
            }

            // Fully expand third level to no compression
            case 256 -> {
                // Grow data
                short[] newData = new short[data.length * 2];
                for (int i = 0; i < data.length; ++i) {
                    final short s = data[i];
                    newData[i * 2 + 0] = mapData[((s >> 0) & 0b11111111)];
                    newData[i * 2 + 1] = mapData[((s >> 8) & 0b11111111)];
                }

                mapData = new short[1]; // There is no cache (magic array length 1)
                data = newData;

                // No more compression --> no map --> no index, just the raw value
                yield value;
            }
            default -> throw new IllegalStateException("Unexpected value: " + mapData.length);
        };
    }


    public synchronized void set(int x, int z, short biome) {
        if (mapData.length == 0) {
            // Handle single value for entire section

            if (data[0] == biome) {
                // Nothing to do
            } else if (data[0] == Short.MIN_VALUE) {
                data[0] = biome;
            } else {
                // new value --> expand to first level compression
                short[] newData = new short[(size * size) >> 3];
                Arrays.fill(newData, (short) 0);
                mapData = new short[]{data[0], biome, Short.MIN_VALUE, Short.MIN_VALUE};
                data = newData;
                internalSetData(x, z, (short) 1);
            }
        } else if(mapData.length == 1) {
            // Handle no compression

            data[xzToIdx(x, z)] = biome;
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
        short s;

        for (s = 0; s < mapData.length; s++) {
            if (mapData[s] == Short.MIN_VALUE) {
                return s;
            }
        }

        return s;
    }
}
