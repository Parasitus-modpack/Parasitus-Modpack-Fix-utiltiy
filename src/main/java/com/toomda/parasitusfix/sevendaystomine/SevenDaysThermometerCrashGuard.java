package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import nuparu.sevendaystomine.tileentity.TileEntityThermometer;

public class SevenDaysThermometerCrashGuard {
    private static final String MODID = "sevendaystomine";
    private static final ResourceLocation THERMOMETER_ID = new ResourceLocation(MODID, "thermometer");

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.side != Side.SERVER || event.phase != TickEvent.Phase.START) return;

        World world = event.world;
        if (world.isRemote || world.loadedTileEntityList.isEmpty()) return;

        List<TileEntity> snapshot = new ArrayList<>(world.loadedTileEntityList);
        for (TileEntity te : snapshot) {
            if (!(te instanceof TileEntityThermometer)) continue;

            BlockPos pos = te.getPos();
            Block block = world.getBlockState(pos).getBlock();
            if (isThermometerBlock(block)) continue;

            world.removeTileEntity(pos);
            te.invalidate();

            ResourceLocation foundId = block.getRegistryName();
            ParasitusFix.getLogger().warn(
                    "[7DTM] Removed mismatched thermometer tile entity at {} (dim {}). Found block: {}",
                    pos,
                    world.provider.getDimension(),
                    foundId != null ? foundId : "<null>");
        }
    }

    private static boolean isThermometerBlock(Block block) {
        ResourceLocation id = block.getRegistryName();
        return THERMOMETER_ID.equals(id);
    }
}
