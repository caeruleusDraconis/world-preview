package caeruleusTait.world.preview.mixin.client;

import net.minecraft.client.gui.components.Checkbox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Checkbox.class)
public interface CheckboxAccessor {

    @Accessor
    void setSelected(boolean value);

}
