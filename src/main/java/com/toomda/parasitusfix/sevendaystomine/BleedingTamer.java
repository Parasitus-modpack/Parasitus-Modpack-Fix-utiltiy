package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
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

    private static final Map<Integer, Window> ACC = new HashMap<>();

    private static final class Window {
        int lastTick;
        int hits;
        float sum;

        void decayTo(int now) {
            if (now - lastTick > ParasitusFixConfig.bleeding.windowTicks) {
                hits = 0;
                sum = 0F;
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
                finalAmt >= ParasitusFixConfig.bleeding.singleHitAbs ||
                        finalAmt >= target.getMaxHealth() * ParasitusFixConfig.bleeding.singleHitRatio;

        boolean trigger = false;

        if (bigSingle && chance(ParasitusFixConfig.bleeding.singleHitChance)) {
            trigger = true;
        }
        else {
            WorldServer ws = (WorldServer) target.world;
            int now = ws.getMinecraftServer().getTickCounter();

            Window w = ACC.computeIfAbsent(target.getEntityId(), k -> new Window());
            w.decayTo(now);

            w.hits++;
            w.sum += finalAmt;

            if ((w.hits >= ParasitusFixConfig.bleeding.hitsThreshold ||
                    w.sum >= ParasitusFixConfig.bleeding.sumThreshold)
                    && chance(ParasitusFixConfig.bleeding.pressureChance)) {

                trigger = true;
                w.hits = 0;
                w.sum = 0F;
            }
        }

        if (trigger) {
            PotionEffect pe = new PotionEffect(
                    Potions.bleeding,
                    ParasitusFixConfig.bleeding.durationTicks,
                    ParasitusFixConfig.bleeding.amplifier,
                    true,
                    true
            );
            target.addPotionEffect(pe);
        }
    }

    private static boolean isBleedEligible(DamageSource src) {
        if (src == null) return false;
        if (src.isFireDamage() || src.isExplosion() || src.isMagicDamage()) return false;

        String t = src.getDamageType();
        if ("player".equals(t) || "mob".equals(t) || "arrow".equals(t) ||
                "generic".equals(t) || "cactus".equals(t) || "thorns".equals(t)) {
            return true;
        }

        return src.getTrueSource() != null;
    }

    private static boolean chance(double p) {
        return Math.random() < p;
    }
}
