package com.toomda.parasitusfix.config;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraftforge.common.config.Config;

@Config(modid = ParasitusFix.MODID)
public final class ParasitusFixConfig {

    @Config.Name("7DTM Tools")
    public static final SevenDaysTools TOOLS = new SevenDaysTools();

    @Config.Name("7DTM Combat")
    public static final SevenDaysCombat COMBAT = new SevenDaysCombat();

    @Config.Name("Bleeding")
    public static final Bleeding BLEEDING = new Bleeding();

    @Config.Name("Hostile Worlds Invasions")
    public static final HostileWorldsInvasions HOSTILE_WORLDS_INVASIONS = new HostileWorldsInvasions();

    public final static class SevenDaysTools {

        @Config.Comment("Scrap pickaxe base damage")
        public float scrapPickaxeDamage = 3.0F;

        @Config.Comment("Scrap axe base damage")
        public float scrapAxeDamage = 3.5F;

        @Config.Comment("Scrap shovel base damage")
        public float scrapShovelDamage = 2.5F;

        @Config.Comment("Scrap hoe base damage")
        public float scrapHoeDamage = 2.0F;

        @Config.Comment("Copper pickaxe base damage")
        public float copperPickaxeDamage = 4.0F;

        @Config.Comment("Copper axe base damage")
        public float copperAxeDamage = 4.5F;

        @Config.Comment("Copper shovel base damage")
        public float copperShovelDamage = 3.5F;

        @Config.Comment("Copper hoe base damage")
        public float copperHoeDamage = 3.0F;

        @Config.Comment("Copper sword base damage")
        public float copperSwordDamage = 5.0F;

        @Config.Comment("Bronze pickaxe base damage")
        public float bronzePickaxeDamage = 4.5F;

        @Config.Comment("Bronze axe base damage")
        public float bronzeAxeDamage = 5.0F;

        @Config.Comment("Bronze shovel base damage")
        public float bronzeShovelDamage = 4.0F;

        @Config.Comment("Bronze hoe base damage")
        public float bronzeHoeDamage = 3.5F;

        @Config.Comment("Bronze sword base damage")
        public float bronzeSwordDamage = 5.5F;
    }

    public final static class SevenDaysCombat {

        @Config.Comment("Wooden spikes (and blooded/broken variants) contact damage")
        public float woodenSpikesDamage = 2.5F;

        @Config.Comment("Metal spikes contact damage")
        public float metalSpikesDamage = 3.5F;

        @Config.Comment("Barbed wire (razor wire) contact damage")
        public float barbedWireDamage = 2.5F;

        @Config.Comment("Flamethrower trap burn duration in seconds")
        public int flameTurretFireSeconds = 4;

        @Config.Comment("Auger base melee damage")
        public float augerDamage = 6.0F;
    }

    public final static class Bleeding {

        @Config.Comment("Absolute damage needed for a single hit to possibly cause bleeding")
        public float singleHitAbs = 6.0F;

        @Config.Comment("Ratio of max health needed for a single hit to possibly cause bleeding")
        public float singleHitRatio = 0.2199999988079071F;

        @Config.Comment("Chance for bleeding on a big single hit (0.0 - 1.0)")
        public double singleHitChance = 0.10D;

        @Config.Comment("Time window (in ticks) to accumulate damage pressure")
        public int windowTicks = 40;

        @Config.Comment("Total accumulated damage needed to trigger bleeding")
        public float sumThreshold = 8.0F;

        @Config.Comment("Number of hits in window needed to trigger bleeding")
        public int hitsThreshold = 3;

        @Config.Comment("Chance for bleeding when pressure threshold is met (0.0 - 1.0)")
        public double pressureChance = 0.30D;

        @Config.Comment("Minimum bleed duration in seconds")
        public int minDurationSeconds = 5;

        @Config.Comment("Maximum bleed duration in seconds")
        public int maxDurationSeconds = 7;

        @Config.Comment("Damage value that maps to maximum bleeding duration")
        public float maxDurationDamage = 10.0F;

        @Config.Comment("Minimum ticks between bleeding damage applications (1 = no throttling)")
        public int damageIntervalTicks = 20;

        @Config.Comment("Use a random interval between min/max for bleeding damage ticks")
        public boolean nondeterministicBleedingRate = false;

        @Config.Comment("Minimum ticks between bleeding damage when nondeterministic rate is enabled")
        public int minDamageIntervalTicks = 15;

        @Config.Comment("Maximum ticks between bleeding damage when nondeterministic rate is enabled")
        public int maxDamageIntervalTicks = 25;

        @Config.Comment("Bleeding potion amplifier")
        public int amplifier = 0;
    }

    public static final class HostileWorldsInvasions {

        @Config.Comment("Enable anti-skybase warning and air-wave fallback logic for Hostile Worlds Invasions")
        public boolean enableAntiSkybase = true;

        @Config.Comment("Enable forced air-wave fallback when player is too high and no valid ground/water spawn area is found")
        public boolean enableAirInvasionFallback = true;

        @Config.Comment("Y level threshold for anti-skybase checks")
        public int skyYLevel = 163;

        @Config.Comment("Message shown when player goes above Y threshold")
        public String warningMessage = "Parasites become MORE dangerous if you build a home too far in the sky.";

        @Config.Comment("Maximum number of times the warning message can be shown per player")
        public int maxWarningMessages = 3;

        @Config.Comment("Primary air invasion template name to force")
        public String airInvasionTemplateName = "invasion_stage_air";

        @Config.Comment("Fallback air invasion template names tried if primary template is missing")
        public String[] airInvasionTemplateFallbackNames = new String[] { "invasion_stage_3_air" };

        @Config.Comment("Fail-count threshold before forcing air-wave fallback (uses HW invasion runtime spawn-failure counters)")
        public int failedSpawnTriesForAirFallback = 320;

        @Config.Comment("Spawn-scan step size used for initial ground/water availability check (lower = more accurate)")
        public int scanStep = 8;

        @Config.Comment("Fallback min spawn range used only if HW invasion config reflection is unavailable")
        public int scanRangeMinFallback = 24;

        @Config.Comment("Fallback max spawn range used only if HW invasion config reflection is unavailable")
        public int scanRangeMaxFallback = 64;
    }
}
