package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.potions.Potions;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
public final class BleedingTamer {

    private static final String MODID = "sevendaystomine";

    private static final float SINGLE_HIT_ABS   = 6.0F;
    private static final float SINGLE_HIT_RATIO = 0.22F;
    private static final double SINGLE_HIT_CHANCE = 0.25;

    private static final int   WINDOW_TICKS   = 40;
    private static final float SUM_THRESHOLD  = 6.0F;
    private static final int   HITS_THRESHOLD = 3;
    private static final double PRESSURE_CHANCE = 0.60;

    private static final int DURATION_TICKS = 20 * 10;
    private static final int AMPLIFIER      = 1;

    private static final Map<Integer, Window> ACC = new HashMap<>();

    private static final class Window {
        int lastTick;
        int hits;
        float sum;
        void decayTo(int now) {
            if (now - lastTick > WINDOW_TICKS) {
                hits = 0; sum = 0F;
            }
            lastTick = now;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalDamage(LivingDamageEvent e) {
        if (!Loader.isModLoaded(MODID)) return;
        if (e.isCanceled()) return;

        EntityLivingBase target = e.getEntityLiving();
        if (target.world.isRemote) return;
        if (!(target.world instanceof WorldServer)) return;

        final float finalAmt = e.getAmount();
        if (finalAmt <= 0) return;
        if (!isBleedEligible(e.getSource())) return;

        boolean bigSingle =
                finalAmt >= SINGLE_HIT_ABS ||
                        finalAmt >= target.getMaxHealth() * SINGLE_HIT_RATIO;

        boolean trigger = false;
        if (bigSingle && chance(SINGLE_HIT_CHANCE)) {
            trigger = true;
        } else {
            WorldServer ws = (WorldServer) target.world;
            int now = ws.getMinecraftServer().getTickCounter();

            Window w = ACC.computeIfAbsent(target.getEntityId(), k -> new Window());
            w.decayTo(now);
            w.hits++;
            w.sum += finalAmt;

            if ((w.hits >= HITS_THRESHOLD || w.sum >= SUM_THRESHOLD) && chance(PRESSURE_CHANCE)) {
                trigger = true;
                w.hits = 0; w.sum = 0F;
            }
        }

        if (trigger) {
            PotionEffect pe = new PotionEffect(Potions.bleeding, DURATION_TICKS, AMPLIFIER, true, true);
            target.addPotionEffect(pe);
        }
    }

    private static boolean isBleedEligible(DamageSource src) {
        if (src == null) return false;
        if (src.isFireDamage() || src.isExplosion() || src.isMagicDamage()) return false;

        String t = src.getDamageType();
        if ("player".equals(t) || "mob".equals(t) || "arrow".equals(t) ||
                "generic".equals(t) || "cactus".equals(t) || "thorns".equals(t)) return true;

        return src.getTrueSource() != null;
    }

    private static boolean chance(double p) {
        return Math.random() < p;
    }
}
