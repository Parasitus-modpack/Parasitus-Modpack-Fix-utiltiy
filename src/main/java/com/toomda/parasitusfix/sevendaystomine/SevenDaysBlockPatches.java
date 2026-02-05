package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class SevenDaysBlockPatches {
    private static final String MODID = "sevendaystomine";
    private static final float OBSIDIAN_HARDNESS = 50.0F;

    private SevenDaysBlockPatches() {}

    public static void apply() {
        if (!Loader.isModLoaded(MODID)) return;
        setHardness("reinforcedconcrete", OBSIDIAN_HARDNESS);
    }

    private static void setHardness(String path, float hardness) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(MODID, path));
        if (block == null) {
            ParasitusFix.getLogger().warn("[7DTM] Block not found for hardness patch: {}", path);
            return;
        }
        block.setHardness(hardness);
        ParasitusFix.getLogger().info("[7DTM] Block hardness patched: {} -> {}", path, hardness);
    }
}
