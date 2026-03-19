package com.toomda.parasitusfix.general;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public class DoorAiEnabler {
    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent e) {
        if (!(e.getEntity() instanceof EntityLiving)) return;

        EntityLiving mob = (EntityLiving) e.getEntity();

        if (!(mob.getNavigator() instanceof PathNavigateGround)) return;

        PathNavigateGround nav = (PathNavigateGround) mob.getNavigator();
        boolean allowDoorOpening = isAllowedMob(mob);
        nav.setEnterDoors(allowDoorOpening);

        if (nav.getNodeProcessor() instanceof WalkNodeProcessor) {
            nav.getNodeProcessor().setCanEnterDoors(allowDoorOpening);
        }

        if (allowDoorOpening) {
            if (!hasOpenDoorGoal(mob)) {
                mob.tasks.addTask(1, new EntityAIOpenDoor(mob, true));
            }
            mob.setPathPriority(PathNodeType.DOOR_WOOD_CLOSED, 0.0F);
            mob.setPathPriority(PathNodeType.DOOR_OPEN, 0.0F);
        } else {
            removeOpenDoorGoals(mob);
        }
        mob.setPathPriority(PathNodeType.DOOR_IRON_CLOSED, -1.0F);
    }

    private static boolean hasOpenDoorGoal(EntityLiving mob) {
        for (EntityAITasks.EntityAITaskEntry t : mob.tasks.taskEntries) {
            if (t.action instanceof EntityAIOpenDoor) return true;
        }
        return false;
    }

    private static void removeOpenDoorGoals(EntityLiving mob) {
        List<EntityAIBase> toRemove = new ArrayList<>();
        for (EntityAITasks.EntityAITaskEntry task : mob.tasks.taskEntries) {
            if (task.action instanceof EntityAIOpenDoor) {
                toRemove.add(task.action);
            }
        }
        for (EntityAIBase task : toRemove) {
            mob.tasks.removeTask(task);
        }
    }

    private static boolean isAllowedMob(EntityLiving mob) {
        String[] allowed = ParasitusFixConfig.DOOR_OPENING.allowedMobs;
        if (allowed == null || allowed.length == 0) return false;

        ResourceLocation id = EntityList.getKey(mob);
        String entityId = id != null ? id.toString().toLowerCase(Locale.ROOT) : "";
        String className = mob.getClass().getName().toLowerCase(Locale.ROOT);
        String simpleName = mob.getClass().getSimpleName().toLowerCase(Locale.ROOT);

        for (String rawEntry : allowed) {
            if (rawEntry == null) continue;
            String entry = rawEntry.trim().toLowerCase(Locale.ROOT);
            if (entry.isEmpty()) continue;
            if (entry.equals(entityId) || entry.equals(className) || entry.equals(simpleName)) {
                return true;
            }
        }
        return false;
    }
}
