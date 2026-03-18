package com.toomda.parasitusfix.srparasites;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SRPVanillaUndeadSpawnWeight {
    private static final int UNKNOWN_PHASE = -1;

    private final IdentityHashMap<Biome.SpawnListEntry, Integer> baseWeights = new IdentityHashMap<>();
    private Field loadedDataMapField;
    private long nextCheckTick = 0L;
    private Boolean boostedMode;

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) return;
        if (!(event.world instanceof WorldServer)) return;

        WorldServer world = (WorldServer) event.world;
        if (world.provider.getDimension() != 0) return;

        ParasitusFixConfig.SRPVanillaUndeadSpawns cfg = ParasitusFixConfig.SRP_VANILLA_UNDEAD_SPAWNS;
        if (!cfg.enable) {
            applyWeights(false, cfg);
            return;
        }

        long now = world.getTotalWorldTime();
        if (now < nextCheckTick) return;

        int interval = Math.max(20, cfg.checkIntervalTicks);
        nextCheckTick = now + interval;

        int phase = resolveSrpPhase(world);
        int revertAt = Math.max(0, cfg.revertAtPhase);

        // If phase cannot be detected yet, keep boosted mode so early-game threat still ramps up.
        boolean shouldBoost = phase == UNKNOWN_PHASE || phase < revertAt;
        applyWeights(shouldBoost, cfg);

        if (phase != UNKNOWN_PHASE && now % 1200L == 0L) {
            ParasitusFix.getLogger().debug("[SRP] Current phase detected: {} (boosted mode: {})", phase, shouldBoost);
        }
    }

    private void applyWeights(boolean boosted, ParasitusFixConfig.SRPVanillaUndeadSpawns cfg) {
        if (boostedMode != null && boostedMode.booleanValue() == boosted) return;

        int zombieBonus = Math.max(0, cfg.zombieWeightBonus);
        int huskBonus = Math.max(0, cfg.huskWeightBonus);

        int touchedZombie = 0;
        int touchedHusk = 0;

        Collection<Biome> biomes = ForgeRegistries.BIOMES.getValuesCollection();
        for (Biome biome : biomes) {
            for (Biome.SpawnListEntry entry : biome.getSpawnableList(EnumCreatureType.MONSTER)) {
                if (entry.entityClass == EntityZombie.class) {
                    int base = getOrCaptureBaseWeight(entry);
                    entry.itemWeight = boosted ? base + zombieBonus : base;
                    touchedZombie++;
                } else if (entry.entityClass == EntityHusk.class) {
                    int base = getOrCaptureBaseWeight(entry);
                    entry.itemWeight = boosted ? base + huskBonus : base;
                    touchedHusk++;
                }
            }
        }

        boostedMode = Boolean.valueOf(boosted);
        ParasitusFix.getLogger().info(
                "[SRP] Vanilla undead spawn weights set to {} mode (Zombie entries: {}, Husk entries: {}, bonuses: +{}/+{}).",
                boosted ? "boosted" : "normal",
                touchedZombie,
                touchedHusk,
                zombieBonus,
                huskBonus);
    }

    private int getOrCaptureBaseWeight(Biome.SpawnListEntry entry) {
        Integer saved = baseWeights.get(entry);
        if (saved != null) return saved.intValue();

        int captured = entry.itemWeight;
        baseWeights.put(entry, Integer.valueOf(captured));
        return captured;
    }

    @SuppressWarnings("unchecked")
    private int resolveSrpPhase(World world) {
        try {
            Field field = getLoadedDataMapField();
            Map<String, WorldSavedData> loadedDataMap = (Map<String, WorldSavedData>) field.get(world.getPerWorldStorage());
            if (loadedDataMap == null || loadedDataMap.isEmpty()) return UNKNOWN_PHASE;

            int best = UNKNOWN_PHASE;
            for (WorldSavedData data : loadedDataMap.values()) {
                if (data == null) continue;

                String className = data.getClass().getName().toLowerCase(Locale.ROOT);
                if (!(className.contains("scape") || className.contains("srparasites") || className.contains("parasite"))) {
                    continue;
                }

                int found = extractPhaseFromObject(data, world.provider.getDimension());
                if (found > best) best = found;
            }
            return best;
        } catch (Throwable ignored) {
            return UNKNOWN_PHASE;
        }
    }

    private int extractPhaseFromObject(Object data, int dim) {
        int best = UNKNOWN_PHASE;

        Class<?> c = data.getClass();
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                String name = f.getName().toLowerCase(Locale.ROOT);
                if (!name.contains("phase")) continue;

                try {
                    f.setAccessible(true);
                    Object value = f.get(data);
                    int found = extractPhaseValue(value, dim);
                    if (found > best) best = found;
                } catch (Throwable ignored) {
                }
            }
            c = c.getSuperclass();
        }

        return best;
    }

    @SuppressWarnings("rawtypes")
    private int extractPhaseValue(Object value, int dim) {
        if (value == null) return UNKNOWN_PHASE;

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof int[]) {
            int[] arr = (int[]) value;
            if (dim >= 0 && dim < arr.length) return arr[dim];

            int best = UNKNOWN_PHASE;
            for (int v : arr) {
                if (v > best) best = v;
            }
            return best;
        }

        if (value instanceof Map) {
            int best = UNKNOWN_PHASE;
            for (Object entryObj : ((Map) value).entrySet()) {
                Map.Entry entry = (Map.Entry) entryObj;

                Object key = entry.getKey();
                Object v = entry.getValue();

                if (key instanceof Number && ((Number) key).intValue() == dim && v instanceof Number) {
                    return ((Number) v).intValue();
                }

                if (v instanceof Number) {
                    int candidate = ((Number) v).intValue();
                    if (candidate > best) best = candidate;
                }
            }
            return best;
        }

        return UNKNOWN_PHASE;
    }

    private Field getLoadedDataMapField() throws NoSuchFieldException {
        if (loadedDataMapField != null) return loadedDataMapField;

        loadedDataMapField = worldStorageClass().getDeclaredField("loadedDataMap");
        loadedDataMapField.setAccessible(true);
        return loadedDataMapField;
    }

    private static Class<?> worldStorageClass() {
        return net.minecraft.world.storage.MapStorage.class;
    }
}
