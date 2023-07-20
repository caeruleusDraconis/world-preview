package caeruleusTait.world.preview.mixin;

import caeruleusTait.world.preview.backend.color.BiomeColorMapReloadListener;
import caeruleusTait.world.preview.backend.color.ColormapReloadListener;
import caeruleusTait.world.preview.backend.color.HeightmapPresetReloadListener;
import caeruleusTait.world.preview.backend.color.StructureMapReloadListener;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @ModifyArg(
            method = "loadResources",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"
            ),
            index = 1
    )
    private static List<PreparableReloadListener> addMyListener(List<PreparableReloadListener> listeners) {
        listeners = new ArrayList<>(listeners);
        listeners.add(new BiomeColorMapReloadListener());
        listeners.add(new StructureMapReloadListener());
        listeners.add(new HeightmapPresetReloadListener());
        listeners.add(new ColormapReloadListener());
        return listeners;
    }

}
