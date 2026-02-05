package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.potions.Potions;
import nuparu.sevendaystomine.util.DamageSources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public final class BleedingTamer {

    private static final String MODID = "sevendaystomine";
    private static final Map<Integer, Window> ACC = new HashMap<>();
    private static final Map<UUID, Integer> LAST_BLEED_TICK = new HashMap<>();
    private static final Map<UUID, Integer> ALLOW_BLEED_TICK = new HashMap<>();

    private static final class Window {
        int lastTick;
        int hits;
        float sum;

        void decayTo(int now, int windowTicks) {
            if (now - lastTick > windowTicks) {
                hits = 0;
                sum = 0F;
            }
            lastTick = now;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalDamage(LivingDamageEvent e) {
        if (!shouldProcessDamageEvent(e.getAmount(), e.isCanceled())) return;

        EntityLivingBase target = e.getEntityLiving();
        WorldServer ws = getServerWorld(target);
        if (ws == null) return;

        float damage = e.getAmount();
        if (!isBleedEligible(e.getSource())) return;

        ParasitusFixConfig.Bleeding cfg = ParasitusFixConfig.BLEEDING;

        boolean bigSingle =
                damage >= cfg.singleHitAbs ||
                        damage >= target.getMaxHealth() * cfg.singleHitRatio;

        boolean trigger = false;
        float effectiveDamage = damage;

        if (bigSingle && chance(cfg.singleHitChance)) {
            trigger = true;
        } else {
            int now = ws.getMinecraftServer().getTickCounter();

            Window w = ACC.computeIfAbsent(target.getEntityId(), k -> new Window());
            w.decayTo(now, cfg.windowTicks);

            w.hits++;
            w.sum += damage;
            effectiveDamage = w.sum;

            if ((w.hits >= cfg.hitsThreshold || w.sum >= cfg.sumThreshold)
                    && chance(cfg.pressureChance)) {
                trigger = true;
                w.hits = 0;
                w.sum = 0F;
            }
        }

        if (trigger) {
            int durationTicks = calculateDurationTicks(effectiveDamage, cfg);
            PotionEffect pe = new PotionEffect(
                    Potions.bleeding,
                    durationTicks,
                    cfg.amplifier,
                    true,
                    true
            );
            target.addPotionEffect(pe);
        }
    }

    private static int calculateDurationTicks(float damage, ParasitusFixConfig.Bleeding cfg) {
        float t = Math.min(damage / cfg.maxDurationDamage, 1.0F);

        int seconds = Math.round(
                cfg.minDurationSeconds +
                        t * (cfg.maxDurationSeconds - cfg.minDurationSeconds)
        );

        return seconds * 20;
    }

    private static boolean isBleedEligible(DamageSource src) {
        if (src == null) return false;
        if (isBleedDamageSource(src)) return false;
        if (src.isFireDamage() || src.isExplosion() || src.isMagicDamage()) return false;

        String t = src.getDamageType();
        if ("player".equals(t) || "mob".equals(t) || "arrow".equals(t) ||
                "generic".equals(t) || "cactus".equals(t) || "thorns".equals(t))
            return true;

        return src.getTrueSource() != null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBleedAttack(LivingAttackEvent e) {
        if (shouldCancelBleedEvent(e.getEntityLiving(), e.getSource(), e.getAmount(), e.isCanceled())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBleedHurt(LivingHurtEvent e) {
        if (shouldCancelBleedEvent(e.getEntityLiving(), e.getSource(), e.getAmount(), e.isCanceled())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBleedDamage(LivingDamageEvent e) {
        if (shouldCancelBleedEvent(e.getEntityLiving(), e.getSource(), e.getAmount(), e.isCanceled())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBleedTick(LivingEvent.LivingUpdateEvent e) {
        if (!isModLoaded()) return;

        EntityLivingBase target = e.getEntityLiving();
        WorldServer ws = getServerWorld(target);
        if (ws == null || !(target instanceof EntityPlayer)) return;

        int interval = ParasitusFixConfig.BLEEDING.damageIntervalTicks;
        if (interval <= 1) return;

        if (!target.isPotionActive(Potions.bleeding)) {
            clearBleedState(target.getUniqueID());
            return;
        }

        int now = ws.getMinecraftServer().getTickCounter();
        UUID id = target.getUniqueID();
        Integer last = LAST_BLEED_TICK.get(id);
        if (last == null) {
            LAST_BLEED_TICK.put(id, now);
            return;
        }
        if (now - last < interval) return;

        LAST_BLEED_TICK.put(id, now);
        ALLOW_BLEED_TICK.put(id, now);
        target.attackEntityFrom(DamageSources.bleeding, 1.0F);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        EntityLivingBase target = e.getEntityLiving();
        if (target instanceof EntityPlayer) {
            clearBleedState(target.getUniqueID());
        }
    }

    private static boolean isBleedDamageSource(DamageSource src) {
        if (src == DamageSources.bleeding) return true;
        String t = src.getDamageType();
        if (t == null) return false;
        return t.toLowerCase(Locale.ROOT).contains("bleed");
    }

    private static boolean shouldCancelBleedEvent(EntityLivingBase target, DamageSource src, float amount, boolean canceled) {
        if (!shouldProcessDamageEvent(amount, canceled)) return false;
        if (src == null || !isBleedDamageSource(src)) return false;
        if (!(target instanceof EntityPlayer)) return false;
        if (!target.isPotionActive(Potions.bleeding)) return false;

        WorldServer ws = getServerWorld(target);
        if (ws == null) return false;

        int interval = ParasitusFixConfig.BLEEDING.damageIntervalTicks;
        if (interval <= 1) return false;

        int now = ws.getMinecraftServer().getTickCounter();
        UUID id = target.getUniqueID();
        Integer allowed = ALLOW_BLEED_TICK.get(id);
        if (allowed != null && allowed == now) {
            ALLOW_BLEED_TICK.remove(id);
            return false;
        }

        return true;
    }

    private static void clearBleedState(UUID id) {
        LAST_BLEED_TICK.remove(id);
        ALLOW_BLEED_TICK.remove(id);
    }

    private static WorldServer getServerWorld(EntityLivingBase target) {
        if (target == null) return null;
        if (target.world.isRemote) return null;
        if (!(target.world instanceof WorldServer)) return null;
        return (WorldServer) target.world;
    }

    private static boolean shouldProcessDamageEvent(float amount, boolean canceled) {
        return isModLoaded() && !canceled && amount > 0;
    }

    private static boolean isModLoaded() {
        return Loader.isModLoaded(MODID);
    }

    private static boolean chance(double p) {
        return Math.random() < p;
    }
}
