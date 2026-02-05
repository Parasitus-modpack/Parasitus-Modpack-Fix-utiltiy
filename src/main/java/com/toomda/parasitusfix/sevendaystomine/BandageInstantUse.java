package com.toomda.parasitusfix.sevendaystomine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.potions.Potions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BandageInstantUse {
    private static final String MODID = "sevendaystomine";
    private static final String BANDAGE = "bandage";
    private static final String ADV_BANDAGE = "advancedbandage";
    private static final String ADV_BANDAGE_ALT = "advanced_bandage";
    private static final int COOLDOWN_TICKS = 30;
    private static final Map<UUID, Integer> LAST_USE_TICK = new HashMap<>();

    public BandageInstantUse() {}

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem e) {
        ItemStack stack = e.getItemStack();
        BandageType type = getBandageType(stack);
        if (type == BandageType.NONE) return;
        if (e.getWorld().isRemote) {
            cancel(e);
            return;
        }

        WorldServer ws = (WorldServer) e.getWorld();
        EntityPlayer player = e.getEntityPlayer();
        if (isOnCooldown(player, ws)) {
            cancel(e);
            return;
        }

        applyBandage(player, stack, type == BandageType.ADVANCED);
        markUsed(player, ws);
        cancel(e);
    }

    private static BandageType getBandageType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return BandageType.NONE;
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null || !MODID.equals(id.getResourceDomain())) return BandageType.NONE;

        String path = id.getResourcePath();
        switch (path) {
            case BANDAGE:
                return BandageType.BASIC;
            case ADV_BANDAGE:
            case ADV_BANDAGE_ALT:
                return BandageType.ADVANCED;
            default:
                return BandageType.NONE;
        }
    }

    private static void applyBandage(EntityPlayer player, ItemStack stack, boolean advanced) {
        if (player == null) return;

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                player.inventory.deleteStack(stack);
            }
        }

        player.removePotionEffect(Potions.bleeding);
        if (advanced) {
            player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 1));
        }
    }

    private static boolean isOnCooldown(EntityPlayer player, WorldServer ws) {
        if (player == null) return true;
        int now = ws.getMinecraftServer().getTickCounter();
        Integer last = LAST_USE_TICK.get(player.getUniqueID());
        return last != null && now - last < COOLDOWN_TICKS;
    }

    private static void markUsed(EntityPlayer player, WorldServer ws) {
        if (player == null) return;
        LAST_USE_TICK.put(player.getUniqueID(), ws.getMinecraftServer().getTickCounter());
    }

    private static void cancel(PlayerInteractEvent.RightClickItem e) {
        e.setCancellationResult(EnumActionResult.SUCCESS);
        e.setCanceled(true);
    }

    private enum BandageType {
        NONE,
        BASIC,
        ADVANCED
    }
}
