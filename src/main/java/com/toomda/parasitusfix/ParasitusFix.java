package com.toomda.parasitusfix;

import com.toomda.parasitusfix.Doors.ParasitusDoors;
import com.toomda.parasitusfix.sevendaystomine.CrawlerArmorFix;
import com.toomda.parasitusfix.sevendaystomine.EnchantItemFix;
import com.toomda.parasitusfix.sevendaystomine.SevenDaysDamagePatches;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import nuparu.sevendaystomine.item.ItemClub;
import nuparu.sevendaystomine.item.ItemQualityTool;
import org.apache.logging.log4j.Logger;

@Mod(modid = ParasitusFix.MODID, name = ParasitusFix.NAME, version = ParasitusFix.VERSION, dependencies = "required-after:sevendaystomine")
public class ParasitusFix
{
    public static final String MODID = "parasitusfix";
    public static final String NAME = "ParasitusFix";
    public static final String VERSION = "1.0";

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
        SevenDaysDamagePatches.apply();
    }



}
