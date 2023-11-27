package caeruleusTait.world.preview.mixin;

import caeruleusTait.world.preview.WorldPreview;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(StructureTemplate.Palette.class)
public abstract class StructureTemplatePaletteMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void threadSafeCache(List<StructureTemplate.StructureBlockInfo> list, CallbackInfo ci) {
        if (WorldPreview.get() != null && WorldPreview.get().workManager().isSetup()) {
            ((StructureTemplatePaletteAccessor) this).setCache(new ConcurrentHashMap<>());
        }
    }

}
