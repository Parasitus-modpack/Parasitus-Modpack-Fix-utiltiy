package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public class CrawlerArmorFix {

    private static final ResourceLocation ZOMBIE_CRAWLER_ID =
            new ResourceLocation("sevendaystomine", "zombie_crawler");
    private static final String TAG_CHECKED = "parasitusfix_cleared_armor";

    private boolean isCrawler(EntityLivingBase ent) {
        ResourceLocation id = EntityList.getKey(ent);
        return id != null && id.equals(ZOMBIE_CRAWLER_ID);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn e) {
        if (e.getWorld().isRemote) return;
        if (!isCrawler(e.getEntityLiving())) return;
        clearArmor(e.getEntityLiving());
        ParasitusFix.getLogger().info("Cleared armor for zombie crawler");
        e.getEntityLiving().getEntityData().setBoolean(TAG_CHECKED, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onJoinWorld(EntityJoinWorldEvent e) {
        if (e.getWorld().isRemote) return;
        if (!(e.getEntity() instanceof EntityLivingBase)) return;

        EntityLivingBase ent = (EntityLivingBase) e.getEntity();

        if (!isCrawler(ent)) return;

        clearArmor(ent);
        ent.getEntityData().setBoolean(TAG_CHECKED, true);
    }

    @SubscribeEvent
    public void onFirstUpdate(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntityLiving().world.isRemote) return;
        if (!isCrawler(e.getEntityLiving())) return;

        boolean checked = e.getEntityLiving().getEntityData().getBoolean(TAG_CHECKED);
        if (!checked || hasAnyArmor(e.getEntityLiving())) {
            clearArmor(e.getEntityLiving());
            e.getEntityLiving().getEntityData().setBoolean(TAG_CHECKED, true);
        }
    }

    private void clearArmor(EntityLivingBase ent) {
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                ent.setItemStackToSlot(slot, ItemStack.EMPTY);
            }
        }
        ent.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
        ent.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    private boolean hasAnyArmor(EntityLivingBase ent) {
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                if (!ent.getItemStackFromSlot(slot).isEmpty()) return true;
            }
        }
        return false;
    }
}
