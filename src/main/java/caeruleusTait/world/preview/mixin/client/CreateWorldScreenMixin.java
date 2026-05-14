package caeruleusTait.world.preview.mixin.client;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.client.gui.screens.PreviewTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Unique
    private PreviewTab world_preview$previewTab;

    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/tabs/TabNavigationBar$Builder;addTabs([Lnet/minecraft/client/gui/components/tabs/Tab;)Lnet/minecraft/client/gui/components/tabs/TabNavigationBar$Builder;"
            ),
            index = 0
    )
    private Tab[] appendPreviewTab(Tab[] originalTabs) {
        world_preview$previewTab = new PreviewTab((CreateWorldScreen) (Object) this, ((ScreenAccessor) this).getMinecraft());
        final Tab[] withPreview = Arrays.copyOf(originalTabs, originalTabs.length + 1);
        withPreview[originalTabs.length] = world_preview$previewTab;
        return withPreview;
    }

    @Inject(method = "popScreen", at = @At("HEAD"))
    private void saveConfigOnClose(CallbackInfo ci) {
        if (world_preview$previewTab != null) {
            world_preview$previewTab.close();
        }
        WorldPreview.get().saveConfig();
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void saveConfigOnCreate(CallbackInfo ci) {
        if (world_preview$previewTab != null) {
            world_preview$previewTab.close();
        }
        WorldPreview.get().saveConfig();
    }

}
