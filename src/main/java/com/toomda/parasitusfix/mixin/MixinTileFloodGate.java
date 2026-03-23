package com.toomda.parasitusfix.mixin;

import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.FluidUtilBC;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(targets = "buildcraft.factory.tile.TileFloodGate", remap = false)
public abstract class MixinTileFloodGate {

    @Shadow(remap = false)
    @Final
    private Tank tank;

    /**
     * @author Codex
     * @reason Replaces the transformed Cleanroom version with the original BuildCraft logic.
     */
    @Overwrite(remap = false)
    private boolean canFill(BlockPos pos) {
        TileEntity self = (TileEntity) (Object) this;
        World world = self.getWorld();

        if (world.isAirBlock(pos)) {
            return true;
        }

        Fluid fluid = BlockUtil.getFluidWithFlowing(world, pos);
        return fluid != null
                && FluidUtilBC.areFluidsEqual(fluid, this.tank.getFluidType())
                && BlockUtil.getFluidWithoutFlowing(world.getBlockState(pos)) == null;
    }

    /**
     * @author Codex
     * @reason Replaces the transformed Cleanroom version with the original BuildCraft logic.
     */
    @Overwrite(remap = false)
    private boolean canSearch(BlockPos pos) {
        TileEntity self = (TileEntity) (Object) this;
        if (this.canFill(pos)) {
            return true;
        }

        Fluid fluid = BlockUtil.getFluid(self.getWorld(), pos);
        return FluidUtilBC.areFluidsEqual(fluid, this.tank.getFluidType());
    }

    /**
     * @author Codex
     * @reason Replaces the transformed Cleanroom version with the original BuildCraft logic.
     */
    @Overwrite(remap = false)
    private boolean canFillThrough(BlockPos pos) {
        World world = ((TileEntity) (Object) this).getWorld();
        if (world.isAirBlock(pos)) {
            return false;
        }

        Fluid fluid = BlockUtil.getFluidWithFlowing(world, pos);
        return FluidUtilBC.areFluidsEqual(fluid, this.tank.getFluidType());
    }
}
