package caeruleusTait.world.preview.backend.color;

import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class PreviewMappingData {
    private final Map<String, InternalColorEntry> resourceOnlyColorMappingData = new HashMap<>();
    private final Map<String, InternalColorEntry> colorMappingData = new HashMap<>();

    private final Map<String, InternalStructureEntry> structMappingData = new HashMap<>();

    private final List<PreviewData.HeightmapPresetData> heightmapPresets = new ArrayList<>();
    private final List<ColorMap> colorMaps = new ArrayList<>();

    private static final MessageDigest sha1;

    static {
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    void clearBiomes() {
        colorMappingData.clear();
        resourceOnlyColorMappingData.clear();
    }

    void clearStructures() {
        structMappingData.clear();
    }

    void clearColorMappings() {
        colorMaps.clear();
    }

    void clearHeightmapPresets() {
        heightmapPresets.clear();
    }

    public void makeBiomeResourceOnlyBackup() {
        resourceOnlyColorMappingData.putAll(colorMappingData);
    }

    public void update(Map<ResourceLocation, ColorEntry> newData, PreviewData.DataSource dataSource) {
        colorMappingData.putAll(
                newData.entrySet()
                        .stream()
                        .collect(Collectors.toMap(x -> x.getKey().toString(), x -> new InternalColorEntry(x.getValue(), dataSource)))
        );
    }

    public void updateStruct(Map<ResourceLocation, StructureEntry> newData, PreviewData.DataSource dataSource) {
        structMappingData.putAll(
                newData.entrySet()
                        .stream()
                        .collect(Collectors.toMap(x -> x.getKey().toString(), x -> new InternalStructureEntry(x.getValue(), dataSource)))
        );
    }

    public void addHeightmapPreset(PreviewData.HeightmapPresetData presetData) {
        heightmapPresets.add(presetData);
    }

    public void addColormap(ColorMap colorMap) {
        colorMaps.add(colorMap);
    }

    public PreviewData generateMapData(Set<ResourceLocation> biomesSet, Set<ResourceLocation> structuresSet) {
        List<String> biomes = biomesSet.stream().map(ResourceLocation::toString).sorted().toList();
        List<String> structures = structuresSet.stream().map(ResourceLocation::toString).sorted().toList();

        final PreviewData res = new PreviewData(
                new PreviewData.BiomeData[biomes.size()],
                new PreviewData.StructureData[structures.size()],
                new Object2ShortOpenHashMap<>(),
                new Object2ShortOpenHashMap<>(),
                heightmapPresets,
                colorMaps.stream().collect(Collectors.toMap(x -> x.key().toString(), x -> x))
        );

        for (short id = 0; id < biomes.size(); ++id) {
            final String biome = biomes.get(id);
            res.biome2Id().put(biome, id);

            InternalColorEntry color = colorMappingData.get(biome);
            if (color == null) {
                color = new InternalColorEntry(new ColorEntry(), PreviewData.DataSource.MISSING);
                byte[] hash = sha1.digest(biome.getBytes(StandardCharsets.UTF_8));
                ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
                for (int i = 0; i < Integer.BYTES && i < hash.length; ++i) {
                    byteBuffer.put(hash[i]);
                }
                color.color = byteBuffer.getInt(0) & 0xFFFFFF;
                color.name = null;
            }

            InternalColorEntry resourceOnlyColor = resourceOnlyColorMappingData.get(biome);
            if (resourceOnlyColor == null) {
                resourceOnlyColor = color;
            }

            res.biomeId2BiomeData()[id] = new PreviewData.BiomeData(
                    id,
                    new ResourceLocation(biome),
                    color.color,
                    resourceOnlyColor.color,
                    color.cave,
                    resourceOnlyColor.cave,
                    color.name,
                    resourceOnlyColor.name,
                    color.dataSource
            );
        }

        for (short id = 0; id < structures.size(); ++id) {
            final String structTag = structures.get(id);
            res.struct2Id().put(structTag, id);

            InternalStructureEntry structure = structMappingData.get(structTag);
            if (structure == null) {
                structure = new InternalStructureEntry(new StructureEntry(), PreviewData.DataSource.MISSING);
                structure.icon = "world_preview:textures/structure/unknown.png";
                structure.name = structTag;
                structure.showByDefault = false;
            }

            res.structId2StructData()[id] = new PreviewData.StructureData(
                    id,
                    new ResourceLocation(structTag),
                    structure.name,
                    new ResourceLocation(structure.icon),
                    structure.showByDefault,
                    structure.dataSource
            );
        }

        return res;
    }

    public static class ColorEntry {
        public int color;
        public boolean cave = false;
        public String name = null;

        public ColorEntry() {
        }

        public ColorEntry(int color, boolean cave) {
            this.color = color;
            this.cave = cave;
        }
    }

    public static class StructureEntry {
        public String name;
        public String icon;
        public boolean showByDefault;
    }

    private static class InternalColorEntry extends ColorEntry {
        public PreviewData.DataSource dataSource;

        private InternalColorEntry(ColorEntry base, PreviewData.DataSource dataSource) {
            this.color = base.color;
            this.cave = base.cave;
            this.name = base.name;
            this.dataSource = dataSource;
        }
    }

    private static class InternalStructureEntry extends StructureEntry {
        public PreviewData.DataSource dataSource;

        private InternalStructureEntry(StructureEntry base, PreviewData.DataSource dataSource) {
            this.name = base.name;
            this.icon = base.icon;
            this.showByDefault = base.showByDefault;
            this.dataSource = dataSource;
        }
    }
}
