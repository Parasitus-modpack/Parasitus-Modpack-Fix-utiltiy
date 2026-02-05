package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import nuparu.sevendaystomine.potions.Potions;

public final class BleedEffectClamp {
    public BleedEffectClamp() {}

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent e) {
        EntityLivingBase target = e.getEntityLiving();
        if (!(target.world instanceof WorldServer)) return;

        PotionEffect pe = target.getActivePotionEffect(Potions.bleeding);
        if (pe == null) return;

        ParasitusFixConfig.Bleeding cfg = ParasitusFixConfig.BLEEDING;
        int maxDuration = Math.max(1, cfg.maxDurationSeconds) * 20;
        int desiredAmp = Math.max(0, cfg.amplifier);

        if (pe.getDuration() > maxDuration || pe.getAmplifier() != desiredAmp) {
            int duration = Math.min(pe.getDuration(), maxDuration);
            target.removePotionEffect(Potions.bleeding);
            target.addPotionEffect(new PotionEffect(Potions.bleeding, duration, desiredAmp, true, true));
            ParasitusFix.getLogger().info("[7DTM] Clamped bleeding effect: duration={} amp={}", duration, desiredAmp);
        }
    }
}
