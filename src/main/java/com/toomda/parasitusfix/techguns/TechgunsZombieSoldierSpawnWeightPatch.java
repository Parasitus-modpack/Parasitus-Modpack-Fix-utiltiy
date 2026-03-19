package com.toomda.parasitusfix.techguns;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import techguns.TGConfig;
import techguns.entities.npcs.ZombieSoldier;
import techguns.entities.spawn.TGNpcSpawnTable;
import techguns.entities.spawn.TGSpawnManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TechgunsZombieSoldierSpawnWeightPatch {
    private static Field spawnListField;
    private static Field npcTypeField;
    private static Field spawnWeightField;
    private static Integer originalZombieSoldierWeight;

    private TechgunsZombieSoldierSpawnWeightPatch() {}

    public static void apply() {
        if (!Loader.isModLoaded("techguns")) return;
        if (originalZombieSoldierWeight == null) {
            originalZombieSoldierWeight = Integer.valueOf(TGConfig.spawnWeightZombieSoldier);
        }

        ParasitusFixConfig.TechgunsSpawns cfg = ParasitusFixConfig.TECHGUNS_SPAWNS;
        int weight = cfg.overrideZombieSoldierSpawnWeight
                ? Math.max(0, cfg.zombieSoldierSpawnWeight)
                : originalZombieSoldierWeight.intValue();
        TGConfig.spawnWeightZombieSoldier = weight;

        int biomeEntries = patchBiomeSpawnEntries(weight);
        int tableEntries = patchSpawnTable(TGSpawnManager.spawnTableOverworld, weight);

        ParasitusFix.getLogger().info(
                "[Techguns] Zombie Soldier spawn weight now {} (override enabled: {}, biome entries: {}, spawn table entries: {}).",
                weight,
                cfg.overrideZombieSoldierSpawnWeight,
                biomeEntries,
                tableEntries);
    }

    private static int patchBiomeSpawnEntries(int weight) {
        int touched = 0;
        Collection<Biome> biomes = ForgeRegistries.BIOMES.getValuesCollection();
        for (Biome biome : biomes) {
            for (Biome.SpawnListEntry entry : biome.getSpawnableList(EnumCreatureType.MONSTER)) {
                if (entry.entityClass == ZombieSoldier.class) {
                    entry.itemWeight = weight;
                    touched++;
                }
            }
        }
        return touched;
    }

    @SuppressWarnings("unchecked")
    private static int patchSpawnTable(TGNpcSpawnTable table, int weight) {
        if (table == null) return 0;
        try {
            Field listField = getSpawnListField();
            Field typeField = getNpcTypeField();
            Field weightField = getSpawnWeightField();

            List<ArrayList<?>> buckets = (List<ArrayList<?>>) listField.get(table);
            int touched = 0;
            for (List<?> bucket : buckets) {
                for (Object entry : bucket) {
                    Class<?> type = (Class<?>) typeField.get(entry);
                    if (type == ZombieSoldier.class) {
                        weightField.setInt(entry, weight);
                        touched++;
                    }
                }
            }
            return touched;
        } catch (Throwable t) {
            ParasitusFix.getLogger().warn("[Techguns] Failed to patch Zombie Soldier spawn table weight.", t);
            return 0;
        }
    }

    private static Field getSpawnListField() throws ReflectiveOperationException {
        if (spawnListField != null) return spawnListField;
        spawnListField = TGNpcSpawnTable.class.getDeclaredField("spawnlist");
        spawnListField.setAccessible(true);
        return spawnListField;
    }

    private static Field getNpcTypeField() throws ReflectiveOperationException {
        if (npcTypeField != null) return npcTypeField;
        npcTypeField = Class.forName("techguns.entities.spawn.TGNpcSpawn").getDeclaredField("type");
        npcTypeField.setAccessible(true);
        return npcTypeField;
    }

    private static Field getSpawnWeightField() throws ReflectiveOperationException {
        if (spawnWeightField != null) return spawnWeightField;
        spawnWeightField = Class.forName("techguns.entities.spawn.TGNpcSpawn").getDeclaredField("spawnWeight");
        spawnWeightField.setAccessible(true);
        return spawnWeightField;
    }
}
