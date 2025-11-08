package com.toomda.parasitusfix.general;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public class DoorAiEnabler {
    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent e) {
        if (!(e.getEntity() instanceof EntityLiving)) return;

        EntityLiving mob = (EntityLiving) e.getEntity();

        if (mob.getNavigator() instanceof PathNavigateGround) {
            PathNavigateGround nav = (PathNavigateGround) mob.getNavigator();
            nav.setEnterDoors(true);

            if (nav.getNodeProcessor() instanceof WalkNodeProcessor) {
                nav.getNodeProcessor().setCanEnterDoors(true);
            }

            if (!hasOpenDoorGoal(mob)) {
                mob.tasks.addTask(1, new EntityAIOpenDoor(mob, true));
            }
            mob.setPathPriority(PathNodeType.DOOR_WOOD_CLOSED, 0.0F);
            mob.setPathPriority(PathNodeType.DOOR_OPEN,        0.0F);
            mob.setPathPriority(PathNodeType.DOOR_IRON_CLOSED, -1.0F);
        }
    }

    private static boolean hasOpenDoorGoal(EntityLiving mob) {
        for (EntityAITasks.EntityAITaskEntry t : mob.tasks.taskEntries) {
            if (t.action instanceof EntityAIOpenDoor) return true;
        }
        return false;
    }
}
