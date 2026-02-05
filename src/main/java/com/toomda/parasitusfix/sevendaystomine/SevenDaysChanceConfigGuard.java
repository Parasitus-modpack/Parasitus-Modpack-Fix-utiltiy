package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraftforge.fml.common.Loader;
import nuparu.sevendaystomine.config.ModConfig;

public final class SevenDaysChanceConfigGuard {
    private static final String MOD_7DTM = "sevendaystomine";
    private static final int ARMOR_COUNT_MAX = 8;
    private static final int ARMOR_MULTIPLIER_MAX = ARMOR_COUNT_MAX + 1;
    private static final int SAFE_MAX_MODIFIER = Integer.MAX_VALUE / ARMOR_MULTIPLIER_MAX;

    private SevenDaysChanceConfigGuard() {}

    public static void apply() {
        if (!Loader.isModLoaded(MOD_7DTM)) return;
        if (ModConfig.players == null || ModConfig.mobs == null) return;

        clampPlayerModifier();
        clampMobModifier();
    }

    private static void clampPlayerModifier() {
        int cur = ModConfig.players.infectionChanceModifier;
        int fixed = clampChanceModifier(cur);
        if (cur != fixed) {
            ParasitusFix.getLogger().warn(
                    "[7DTM] infectionChanceModifier={} is invalid for Random.nextInt; clamped to {} (safe-disable).",
                    cur, fixed
            );
            ModConfig.players.infectionChanceModifier = fixed;
        }
    }

    private static void clampMobModifier() {
        int cur = ModConfig.mobs.bleedingChanceModifier;
        int fixed = clampChanceModifier(cur);
        if (cur != fixed) {
            ParasitusFix.getLogger().warn(
                    "[7DTM] bleedingChanceModifier={} is invalid for Random.nextInt; clamped to {} (safe-disable).",
                    cur, fixed
            );
            ModConfig.mobs.bleedingChanceModifier = fixed;
        }
    }

    private static int clampChanceModifier(int value) {
        if (value <= 0) return SAFE_MAX_MODIFIER;
        if (value > SAFE_MAX_MODIFIER) return SAFE_MAX_MODIFIER;
        return value;
    }
}
