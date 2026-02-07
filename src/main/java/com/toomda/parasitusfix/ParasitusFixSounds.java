package com.toomda.parasitusfix;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// TODO: Temporarily disabled (bunkerdoor sounds).
// @Mod.EventBusSubscriber(modid = ParasitusFix.MODID)
@Deprecated
public class ParasitusFixSounds {
    public static SoundEvent BUNKERDOOR_OPEN;
    public static SoundEvent BUNKERDOOR_CLOSE;

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        // Disabled for now.
        if (true) return;
        BUNKERDOOR_OPEN = register(event, "bunkerdoor_open");
        BUNKERDOOR_CLOSE = register(event, "bunkerdoor_close");
    }

    private static SoundEvent register(RegistryEvent.Register<SoundEvent> event, String name) {
        ResourceLocation id = new ResourceLocation(ParasitusFix.MODID, name);
        SoundEvent sound = new SoundEvent(id).setRegistryName(id);
        event.getRegistry().register(sound);
        return sound;
    }
}
