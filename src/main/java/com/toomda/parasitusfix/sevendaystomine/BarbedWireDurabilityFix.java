package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import nuparu.sevendaystomine.tileentity.TileEntityWoodenSpikes;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public class BarbedWireDurabilityFix {
    private static final String MODID = "sevendaystomine";
    private static final ResourceLocation RAZOR_WIRE_ID = new ResourceLocation(MODID, "razor_wire");

    private static final int RAZOR_WIRE_MAX_HEALTH = 1800;

    private BarbedWireDurabilityFix() {}

    @SubscribeEvent
    public static void onPlace(BlockEvent.PlaceEvent e) {
        if (!Loader.isModLoaded(MODID)) return;
        World w = (World) e.getWorld();
        if (w.isRemote) return;

        BlockPos pos = e.getPos();
        Block placed = e.getPlacedBlock().getBlock();

        if (isRazorWire(placed)) {
            TileEntity te = w.getTileEntity(pos);
            if (te instanceof TileEntityWoodenSpikes) {
                ((TileEntityWoodenSpikes) te).health = RAZOR_WIRE_MAX_HEALTH;
                te.markDirty();
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent e) {
        if (e.side != Side.SERVER || e.phase != TickEvent.Phase.END) return;
        if (!Loader.isModLoaded(MODID)) return;

        World w = e.world;
        if ((w.getTotalWorldTime() % 10L) != 0L) return;

        java.util.List<TileEntity> snapshot = new java.util.ArrayList<>(w.loadedTileEntityList);
        java.util.List<BlockPos> toBreak = new java.util.ArrayList<>();

        for (TileEntity te : snapshot) {
            if (!(te instanceof TileEntityWoodenSpikes)) continue;

            BlockPos pos = te.getPos();
            Block block = w.getBlockState(pos).getBlock();
            if (!isRazorWire(block)) continue;

            TileEntityWoodenSpikes tws = (TileEntityWoodenSpikes) te;

            if (tws.health > RAZOR_WIRE_MAX_HEALTH) {
                tws.health = RAZOR_WIRE_MAX_HEALTH;
                te.markDirty();
            }

            if (tws.health <= 0) {
                toBreak.add(pos);
            }
        }

        for (BlockPos pos : toBreak) {
            w.destroyBlock(pos, false);
        }
    }


    private static boolean isRazorWire(Block b) {
        ResourceLocation id = b.getRegistryName();
        return id != null && id.equals(RAZOR_WIRE_ID);
    }
}
