package com.toomda.parasitusfix.techguns;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.entities.npcs.ZombieFarmer;
import techguns.entities.npcs.ZombieMiner;
import techguns.entities.npcs.ZombiePigmanSoldier;
import techguns.entities.npcs.ZombiePoliceman;
import techguns.entities.npcs.ZombieSoldier;

public class TechgunsZombieEndSpawnRestrict {
    private static final int END_DIMENSION_ID = 1;

    @SubscribeEvent
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!isTechgunsZombie(event.getEntity())) return;

        World world = event.getWorld();
        if (world.provider.getDimension() == END_DIMENSION_ID) {
            event.setResult(Event.Result.DENY);
            ParasitusFix.getLogger().debug("Blocked Techguns zombie spawn in The End");
        }
    }

    @SubscribeEvent
    public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (!isTechgunsZombie(event.getEntity())) return;

        World world = event.getWorld();
        if (world.provider.getDimension() == END_DIMENSION_ID) {
            event.setCanceled(true);
            ParasitusFix.getLogger().debug("Blocked Techguns zombie special spawn in The End");
        }
    }

    private boolean isTechgunsZombie(Entity entity) {
        return entity instanceof ZombieSoldier
            || entity instanceof ZombiePoliceman
            || entity instanceof ZombieFarmer
            || entity instanceof ZombieMiner
            || entity instanceof ZombiePigmanSoldier;
    }
}
