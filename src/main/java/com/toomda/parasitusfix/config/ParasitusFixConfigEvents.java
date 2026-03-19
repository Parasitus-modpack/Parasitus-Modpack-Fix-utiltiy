package com.toomda.parasitusfix.config;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.buildcraft.QuartzKinesisPipeCapRemoval;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysBlockPatches;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysChanceConfigGuard;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysDamagePatches;
import com.toomda.parasitusfix.techguns.TechgunsZombieSoldierSpawnWeightPatch;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public final class ParasitusFixConfigEvents {
    private ParasitusFixConfigEvents() {}

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!ParasitusFix.MODID.equals(event.getModID())) return;

        ConfigManager.sync(ParasitusFix.MODID, Config.Type.INSTANCE);
        SevenDaysChanceConfigGuard.apply();
        SevenDaysDamagePatches.apply();
        SevenDaysBlockPatches.apply();
        if (Loader.isModLoaded("techguns")) {
            TechgunsZombieSoldierSpawnWeightPatch.apply();
        }
        if (Loader.isModLoaded("buildcrafttransport")) {
            QuartzKinesisPipeCapRemoval.apply();
        }
    }
}
