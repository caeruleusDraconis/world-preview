package caeruleusTait.world.preview.mixin.client;

import caeruleusTait.world.preview.WorldPreview;
import caeruleusTait.world.preview.client.gui.screens.PreviewTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @Shadow private @Nullable TabNavigationBar tabNavigationBar;

    private PreviewTab previewTab;

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;",
                    shift = At.Shift.BEFORE
            ),
            slice = @Slice(
                    from = @At("HEAD"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;columnSpacing(I)Lnet/minecraft/client/gui/layouts/GridLayout;")
            )
    )
    private void appendPreviewTab(CallbackInfo ci) {
        previewTab = new PreviewTab((CreateWorldScreen) (Object) this, ((ScreenAccessor) this).getMinecraft());

        final TabNavigationBar originalRaw = tabNavigationBar;
        final TabNavigationBarAccessor original = (TabNavigationBarAccessor)originalRaw;

        tabNavigationBar = TabNavigationBar
                .builder(original.getTabManager(), original.getWidth())
                .addTabs(original.getTabs().toArray(new Tab[0]))
                .addTabs(previewTab)
                .build();
    }

    @Inject(method = "popScreen", at = @At("HEAD"))
    private void saveConfigOnClose(CallbackInfo ci) {
        previewTab.close();
        WorldPreview.get().saveConfig();
    }

    @Inject(method = "onCreate", at = @At("HEAD"))
    private void saveConfigOnCreate(CallbackInfo ci) {
        previewTab.close();
        WorldPreview.get().saveConfig();
    }

}
