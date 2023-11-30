package caeruleusTait.world.preview.mixin.client;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
public interface CreateWorldScreenAccessor {

    @Invoker
    Pair<Path, PackRepository> invokeGetDataPackSelectionSettings(WorldDataConfiguration worldDataConfiguration);

    @Invoker
    Path invokeGetTempDataPackDir();

}
