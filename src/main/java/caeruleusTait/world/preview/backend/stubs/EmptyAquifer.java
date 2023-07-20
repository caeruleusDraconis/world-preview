package caeruleusTait.world.preview.backend.stubs;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public class EmptyAquifer implements Aquifer {
    @Nullable
    @Override
    public BlockState computeSubstance(DensityFunction.FunctionContext context, double substance) {
        return substance > 0.0 ? null : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean shouldScheduleFluidUpdate() {
        return false;
    }
}
