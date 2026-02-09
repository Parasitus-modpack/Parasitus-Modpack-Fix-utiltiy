package com.toomda.parasitusfix.techguns;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.lang.reflect.Field;

public final class TechgunsBlockHardnessCap {
    private static final String MODID = "techguns";
    private static final float MAX_HARDNESS = 4.0F;

    private TechgunsBlockHardnessCap() {
    }

    private static Field resolveHardnessField() throws NoSuchFieldException {
        try {
            Field field = Block.class.getDeclaredField("field_149782_v");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            Field field = Block.class.getDeclaredField("blockHardness");
            field.setAccessible(true);
            return field;
        }
    }

    public static void apply() {
        int capped = 0;
        try {
            Field hardnessField = resolveHardnessField();

            for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
                ResourceLocation id = block.getRegistryName();
                if (id == null || !MODID.equals(id.getResourceDomain())) {
                    continue;
                }
                float hardness = hardnessField.getFloat(block);
                if (hardness > MAX_HARDNESS) {
                    block.setHardness(MAX_HARDNESS);
                    capped++;
                }
            }
        } catch (Throwable t) {
            ParasitusFix.getLogger().warn("Techguns block hardness cap failed.", t);
            return;
        }

        if (capped > 0) {
            ParasitusFix.getLogger().info("Techguns block hardness cap: updated {} blocks.", capped);
        }
    }
}
