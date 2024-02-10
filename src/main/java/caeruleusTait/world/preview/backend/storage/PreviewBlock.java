package caeruleusTait.world.preview.backend.storage;

import caeruleusTait.world.preview.WorldPreview;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_HEIGHT;
import static caeruleusTait.world.preview.backend.storage.PreviewStorage.FLAG_STRUCT_START;

public class PreviewBlock implements Serializable {

    @Serial
    private static final long serialVersionUID = -6140310220242894115L;

    public static final int PREVIEW_BLOCK_SHIFT = 5;
    public static final int PREVIEW_BLOCK_SIZE = 1 << PREVIEW_BLOCK_SHIFT;
    public static final int PREVIEW_BLOCK_MASK = 0b11111;

    private final long flags;
    private final PreviewSection[] sections = new PreviewSection[PREVIEW_BLOCK_SIZE * PREVIEW_BLOCK_SIZE];

    public PreviewBlock(long flags) {
        this.flags = flags;
    }

    public synchronized @NotNull PreviewSection get(int quartX, int quartZ) {
        final int idx = (((quartX >> PreviewSection.SHIFT) & PREVIEW_BLOCK_MASK) * PREVIEW_BLOCK_SIZE) + ((quartZ >> PreviewSection.SHIFT) & PREVIEW_BLOCK_MASK);
        PreviewSection section = sections[idx];
        if (section == null) {
            section = sections[idx] = sectionFactory(quartX, quartZ);
        }
        return section;
    }

    private PreviewSection sectionFactory(int quartX, int quartZ) {
        if (flags == FLAG_STRUCT_START) {
            return new PreviewSectionStructure(quartX, quartZ);
        }
        final int quartStride = WorldPreview.get().renderSettings().quartStride();
        if (WorldPreview.get().cfg().enableCompression) {
            return switch (quartStride) {
                case 1 -> new PreviewSectionCompressed.Full(quartX, quartZ);
                case 2 -> new PreviewSectionCompressed.Half(quartX, quartZ);
                case 4 -> new PreviewSectionCompressed.Quarter(quartX, quartZ);
                default -> throw new IllegalStateException("Unexpected quartStride value: " + quartStride);
            };
        }
        return switch (quartStride) {
            case 1 -> new PreviewSectionFull(quartX, quartZ);
            case 2 -> new PreviewSectionHalf(quartX, quartZ);
            case 4 -> new PreviewSectionQuarter(quartX, quartZ);
            default -> throw new IllegalStateException("Unexpected quartStride value: " + quartStride);
        };
    }

    public PreviewSection[] sections() {
        return Arrays.copyOf(sections, sections.length);
    }
}
