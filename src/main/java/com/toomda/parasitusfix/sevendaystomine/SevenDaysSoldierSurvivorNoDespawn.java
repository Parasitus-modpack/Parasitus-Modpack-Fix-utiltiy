package com.toomda.parasitusfix.sevendaystomine;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.entity.EntitySoldier;
import nuparu.sevendaystomine.entity.EntitySurvivor;

public class SevenDaysSoldierSurvivorNoDespawn {
    @SubscribeEvent
    public void onJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;

        if (event.getEntity() instanceof EntitySoldier || event.getEntity() instanceof EntitySurvivor) {
            event.getEntity().enablePersistence();
        }
    }
}
