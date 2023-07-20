package caeruleusTait.world.preview.mixin.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
public interface CreateWorldScreenAccessor {

    @Accessor
    WorldCreationUiState getUiState();

    @Invoker
    Pair<Path, PackRepository> invokeGetDataPackSelectionSettings(WorldDataConfiguration worldDataConfiguration);

    @Invoker
    Path invokeGetTempDataPackDir();

}
