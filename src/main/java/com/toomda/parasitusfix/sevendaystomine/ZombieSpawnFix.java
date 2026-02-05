package com.toomda.parasitusfix.sevendaystomine;


import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.entity.EntityZombieBase;

public class ZombieSpawnFix {
    private static final int MAX_BLOCK_LIGHT = 7;

    public ZombieSpawnFix() {}

    @SubscribeEvent
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn e) {
        if (!(e.getEntityLiving() instanceof EntityZombieBase)) return;

        if (!isValidSpawn(e.getWorld(), new BlockPos(e.getX(), e.getY(), e.getZ()))) {
            e.setResult(net.minecraftforge.fml.common.eventhandler.Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn e) {
        if (!(e.getEntityLiving() instanceof EntityZombieBase)) return;

        if (!isValidSpawn(e.getWorld(), e.getEntity().getPosition())) {
            e.setCanceled(true);
        }
    }

    private static boolean isValidSpawn(World w, BlockPos pos) {
        int blockLight = w.getLightFor(EnumSkyBlock.BLOCK, pos);
        if (blockLight > MAX_BLOCK_LIGHT) return false;

        BlockPos belowPos = pos.down();
        IBlockState below = w.getBlockState(belowPos);
        Block belowBlock = below.getBlock();

        if (!below.isOpaqueCube() || !below.isFullCube() || !below.isSideSolid(w, belowPos, EnumFacing.UP)) return false;
        if (belowBlock instanceof BlockSlab && !below.isFullCube()) return false;

        IBlockState at = w.getBlockState(pos);
        if (at.getMaterial().blocksMovement()) return false;

        return true;
    }
}

