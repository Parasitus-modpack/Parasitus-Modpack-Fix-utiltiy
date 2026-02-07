package com.toomda.parasitusfix.techguns;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.entities.npcs.AttackHelicopter;

import java.util.ArrayList;
import java.util.List;

public class TechgunsAttackHelicopterTargetFix {
    private static final int TARGET_PRIORITY = 1;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        World world = event.getWorld();
        if (world.isRemote) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof AttackHelicopter)) return;

        AttackHelicopter heli = (AttackHelicopter) entity;

        removeVanillaFindNearestPlayerTask(heli.targetTasks);
        if (!hasCustomTargetTask(heli.targetTasks)) {
            heli.targetTasks.addTask(TARGET_PRIORITY, new FindNearestPlayer3D(heli));
        }
    }

    private static void removeVanillaFindNearestPlayerTask(EntityAITasks tasks) {
        List<EntityAITasks.EntityAITaskEntry> remove = new ArrayList<>();
        for (EntityAITasks.EntityAITaskEntry entry : tasks.taskEntries) {
            if (entry.action != null && entry.action.getClass().getName().equals("net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer")) {
                remove.add(entry);
            }
        }
        tasks.taskEntries.removeAll(remove);
    }

    private static boolean hasCustomTargetTask(EntityAITasks tasks) {
        for (EntityAITasks.EntityAITaskEntry entry : tasks.taskEntries) {
            if (entry.action instanceof FindNearestPlayer3D) {
                return true;
            }
        }
        return false;
    }

    private static final class FindNearestPlayer3D extends EntityAIBase {
        private final EntityLiving owner;
        private final EntityAINearestAttackableTarget.Sorter sorter;
        private final Predicate<EntityPlayer> selector;
        private EntityPlayer target;

        private FindNearestPlayer3D(EntityLiving owner) {
            this.owner = owner;
            this.sorter = new EntityAINearestAttackableTarget.Sorter(owner);
            this.selector = player -> player != null
                    && player.isEntityAlive()
                    && !player.isSpectator()
                    && !player.capabilities.isCreativeMode
                    && owner.getDistanceSq(player) <= getTargetDistanceSq();
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            double range = getTargetDistance();
            AxisAlignedBB bounds = owner.getEntityBoundingBox().grow(range, range, range);
            List<EntityPlayer> list = owner.world.getEntitiesWithinAABB(EntityPlayer.class, bounds, selector);
            if (list.isEmpty()) {
                return false;
            }
            list.sort(sorter);
            this.target = list.get(0);
            return true;
        }

        @Override
        public void startExecuting() {
            owner.setAttackTarget(target);
            super.startExecuting();
        }

        private double getTargetDistance() {
            IAttributeInstance attr = owner.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
            return attr == null ? 16.0D : attr.getAttributeValue();
        }

        private double getTargetDistanceSq() {
            double d = getTargetDistance();
            return d * d;
        }
    }
}
