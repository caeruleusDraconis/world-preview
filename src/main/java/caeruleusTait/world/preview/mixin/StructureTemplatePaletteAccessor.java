package caeruleusTait.world.preview.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Map;

@Mixin(StructureTemplate.Palette.class)
public interface StructureTemplatePaletteAccessor {

    @Accessor
    Map<Block, List<StructureTemplate.StructureBlockInfo>> getCache();

    @Accessor
    @Mutable
    void setCache(Map<Block, List<StructureTemplate.StructureBlockInfo>> cache);

}
