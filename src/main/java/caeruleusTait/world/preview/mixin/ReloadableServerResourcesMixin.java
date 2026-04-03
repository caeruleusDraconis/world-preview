package caeruleusTait.world.preview.mixin;

import caeruleusTait.world.preview.backend.color.BiomeColorMapReloadListener;
import caeruleusTait.world.preview.backend.color.ColormapReloadListener;
import caeruleusTait.world.preview.backend.color.HeightmapPresetReloadListener;
import caeruleusTait.world.preview.backend.color.StructureMapReloadListener;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void addMyListener(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        List<PreparableReloadListener> listeners = new ArrayList<>(cir.getReturnValue());
        listeners.add(new BiomeColorMapReloadListener());
        listeners.add(new StructureMapReloadListener());
        listeners.add(new HeightmapPresetReloadListener());
        listeners.add(new ColormapReloadListener());
        cir.setReturnValue(listeners);
    }

}
