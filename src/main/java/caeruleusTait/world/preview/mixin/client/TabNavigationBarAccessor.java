package caeruleusTait.world.preview.mixin.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TabNavigationBar.class)
public interface TabNavigationBarAccessor {

    @Accessor
    int getWidth();

    @Accessor
    TabManager getTabManager();

    @Accessor
    ImmutableList<Tab> getTabs();

}
