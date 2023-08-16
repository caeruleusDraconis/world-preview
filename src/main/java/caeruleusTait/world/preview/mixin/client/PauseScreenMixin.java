package caeruleusTait.world.preview.mixin.client;

import caeruleusTait.world.preview.client.WorldPreviewComponents;
import caeruleusTait.world.preview.client.gui.screens.InGamePreviewScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {

    @Shadow
    @Final
    private static int BUTTON_WIDTH_FULL;

    @Inject(
            method = "createPauseMenu",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.BEFORE,
                    target = "Lnet/minecraft/client/Minecraft;isLocalServer()Z"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void addWorldPreviewButton(CallbackInfo ci, GridLayout gridLayout, GridLayout.RowHelper rowHelper) {
        Minecraft minecraft = ((ScreenAccessor) this).getMinecraft();
        if (minecraft.getSingleplayerServer() == null) {
            return;
        }

        rowHelper.addChild(
                Button
                        .builder(WorldPreviewComponents.TITLE_FULL, this::onPressWorldPreview)
                        .width(BUTTON_WIDTH_FULL)
                        .build(),
                2
        );
    }

    @Unique
    private void onPressWorldPreview(Button btn) {
        Minecraft minecraft = ((ScreenAccessor) this).getMinecraft();
        minecraft.setScreen(new InGamePreviewScreen());
    }
}
