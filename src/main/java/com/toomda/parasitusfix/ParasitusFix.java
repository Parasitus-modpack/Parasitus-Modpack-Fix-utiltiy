package com.toomda.parasitusfix;

import com.toomda.parasitusfix.Doors.ParasitusDoors;
import com.toomda.parasitusfix.sevendaystomine.CrawlerArmorFix;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysChanceConfigGuard;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysDamagePatches;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ParasitusFix.MODID, name = ParasitusFix.NAME, version = ParasitusFix.VERSION, dependencies = "required-after:sevendaystomine")
public class ParasitusFix
{
    public static final String MODID = "parasitusfix";
    public static final String NAME = "ParasitusFix";
    public static final String VERSION = "1.1.02";

    private static Logger logger;

    public static org.apache.logging.log4j.Logger getLogger() { return logger; }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        ParasitusDoors.registerAll();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new CrawlerArmorFix());
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        SevenDaysChanceConfigGuard.apply();
        SevenDaysDamagePatches.apply();
    }



}
