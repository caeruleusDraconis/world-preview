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
    private final Map<String, ColorEntry> resourceOnlyColorMappingData = new HashMap<>();
    private final Map<String, ColorEntry> colorMappingData = new HashMap<>();

    private final Map<String, StructureEntry> structMappingData = new HashMap<>();

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

    public void update(Map<ResourceLocation, ColorEntry> newData) {
        colorMappingData.putAll(
                newData.entrySet()
                        .stream()
                        .collect(Collectors.toMap(x -> x.getKey().toString(), Map.Entry::getValue))
        );
    }

    public void updateStruct(Map<ResourceLocation, StructureEntry> newData) {
        structMappingData.putAll(
                newData.entrySet()
                        .stream()
                        .collect(Collectors.toMap(x -> x.getKey().toString(), Map.Entry::getValue))
        );
    }

    public void addHeightmapPreset(PreviewData.HeightmapPresetData presetData) {
        heightmapPresets.add(presetData);
    }

    public void addColormap(ColorMap colorMap) {
        colorMaps.add(colorMap);
    }

    public PreviewData generateMapData(
            Set<ResourceLocation> biomesSet,
            Set<ResourceLocation> caveBiomesSet,
            Set<ResourceLocation> structuresSet,
            Set<ResourceLocation> displayByDefaultStructuresSet
    ) {
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

            ColorEntry color = colorMappingData.get(biome);
            if (color == null) {
                color = new ColorEntry();
                color.dataSource = PreviewData.DataSource.MISSING;
                byte[] hash = sha1.digest(biome.getBytes(StandardCharsets.UTF_8));
                ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
                for (int i = 0; i < Integer.BYTES && i < hash.length; ++i) {
                    byteBuffer.put(hash[i]);
                }
                color.color = byteBuffer.getInt(0) & 0xFFFFFF;
                color.name = null;
            }

            ColorEntry resourceOnlyColor = resourceOnlyColorMappingData.get(biome);
            if (resourceOnlyColor == null) {
                resourceOnlyColor = color;
            }

            ResourceLocation biomeRes = new ResourceLocation(biome);
            res.biomeId2BiomeData()[id] = new PreviewData.BiomeData(
                    id,
                    biomeRes,
                    color.color,
                    resourceOnlyColor.color,
                    color.cave.orElse(caveBiomesSet.contains(biomeRes)),
                    resourceOnlyColor.cave.orElse(caveBiomesSet.contains(biomeRes)),
                    color.name,
                    resourceOnlyColor.name,
                    color.dataSource
            );
        }

        for (short id = 0; id < structures.size(); ++id) {
            final String structTag = structures.get(id);
            res.struct2Id().put(structTag, id);

            StructureEntry structure = structMappingData.get(structTag);
            if (structure == null) {
                structure = new StructureEntry();
                structure.dataSource = PreviewData.DataSource.MISSING;
                structure.texture = "world_preview:textures/structure/unknown.png";
                structure.name = structTag;
                structure.showByDefault = Optional.empty();
            }

            ResourceLocation structureRes = new ResourceLocation(structTag);
            res.structId2StructData()[id] = new PreviewData.StructureData(
                    id,
                    structureRes,
                    structure.name,
                    structure.texture == null ? null : new ResourceLocation(structure.texture),
                    structure.item == null ? null : new ResourceLocation(structure.item),
                    structure.showByDefault.orElse(displayByDefaultStructuresSet.contains(structureRes)),
                    structure.dataSource
            );
        }

        return res;
    }

    public static class ColorEntry {
        public PreviewData.DataSource dataSource;
        public int color;
        public Optional<Boolean> cave = Optional.empty();
        public String name = null;

        public ColorEntry() {
        }

        public ColorEntry(PreviewData.DataSource dataSource, int color, boolean cave, String name) {
            this.dataSource = dataSource;
            this.color = color;
            this.cave = Optional.of(cave);
            this.name = name;
        }
    }

    public static class StructureEntry {
        public PreviewData.DataSource dataSource;
        public String name = null;
        public String texture = null;
        public String item = null;
        public Optional<Boolean> showByDefault = Optional.empty();
    }
}
