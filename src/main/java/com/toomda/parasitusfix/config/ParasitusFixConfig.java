package com.toomda.parasitusfix.config;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraftforge.common.config.Config;

@Config(
        modid = ParasitusFix.MODID,
        name = "parasitusfix"
)
public class ParasitusFixConfig {

    @Config.Comment("Bleeding behavior tuning")
    public static Bleeding bleeding = new Bleeding();

    public static class Bleeding {

        @Config.Comment("Absolute damage required for a single hit to qualify as 'big'")
        public float singleHitAbs = 6.0F;

        @Config.Comment("Percentage of max health required for a single hit to qualify as 'big'")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public float singleHitRatio = 0.22F;

        @Config.Comment("Chance to apply bleeding from a big single hit")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double singleHitChance = 0.25;

        @Config.Comment("Time window (in ticks) for sustained damage accumulation")
        @Config.RangeInt(min = 1)
        public int windowTicks = 40;

        @Config.Comment("Total damage required within the window to trigger pressure bleeding")
        public float sumThreshold = 6.0F;

        @Config.Comment("Number of hits required within the window to trigger pressure bleeding")
        @Config.RangeInt(min = 1)
        public int hitsThreshold = 3;

        @Config.Comment("Chance to apply bleeding once pressure thresholds are met")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double pressureChance = 0.60;

        @Config.Comment("Bleeding duration in ticks")
        @Config.RangeInt(min = 1)
        public int durationTicks = 20 * 10;

        @Config.Comment("Bleeding amplifier level (0 = level 1)")
        @Config.RangeInt(min = 0)
        public int amplifier = 1;
    }
}
