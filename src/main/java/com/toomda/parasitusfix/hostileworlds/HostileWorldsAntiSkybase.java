package com.toomda.parasitusfix.hostileworlds;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class HostileWorldsAntiSkybase {
    private static final String NBT_KEY_WARN_COUNT = "parasitusfix:hwSkyWarnCount";
    private static final String NBT_KEY_WAS_ABOVE = "parasitusfix:hwSkyWasAbove";
    private static final String NBT_KEY_PREV_ACTIVE = "parasitusfix:hwInvPrevActive";
    private static final String NBT_KEY_LAST_AIR_WAVE = "parasitusfix:hwInvLastAirWave";
    private static final String NBT_PERSISTED = "PlayerPersisted";

    private static final HwInvReflection HW_INV = new HwInvReflection();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        EntityPlayer player = event.player;
        if (player == null || player.world.isRemote) return;

        handleHeightWarning(player);
        handleAirWaveFallback(player);
    }

    private void handleHeightWarning(EntityPlayer player) {
        ParasitusFixConfig.HostileWorldsInvasions cfg = ParasitusFixConfig.HOSTILE_WORLDS_INVASIONS;
        if (!cfg.enableAntiSkybase) return;

        boolean above = player.posY > cfg.skyYLevel;
        NBTTagCompound runtime = player.getEntityData();
        boolean wasAbove = runtime.getBoolean(NBT_KEY_WAS_ABOVE);
        if (!above) {
            if (wasAbove) runtime.setBoolean(NBT_KEY_WAS_ABOVE, false);
            return;
        }
        if (wasAbove) return;

        NBTTagCompound persisted = getPersisted(player);
        int shown = persisted.getInteger(NBT_KEY_WARN_COUNT);
        if (shown < cfg.maxWarningMessages) {
            player.sendMessage(new TextComponentString(cfg.warningMessage));
            persisted.setInteger(NBT_KEY_WARN_COUNT, shown + 1);
        }
        runtime.setBoolean(NBT_KEY_WAS_ABOVE, true);
    }

    private void handleAirWaveFallback(EntityPlayer player) {
        ParasitusFixConfig.HostileWorldsInvasions cfg = ParasitusFixConfig.HOSTILE_WORLDS_INVASIONS;
        if (!cfg.enableAirInvasionFallback) return;
        if (player.dimension != 0) return;
        if (player.posY <= cfg.skyYLevel) return;

        Object playerData = HW_INV.getPlayerData(player);
        if (playerData == null) return;

        boolean active = HW_INV.getBoolean(playerData, "dataPlayerInvasionActive", false);
        NBTTagCompound runtime = player.getEntityData();
        boolean wasActive = runtime.getBoolean(NBT_KEY_PREV_ACTIVE);
        runtime.setBoolean(NBT_KEY_PREV_ACTIVE, active);
        if (!active) return;

        int wave = HW_INV.getInt(playerData, "lastWaveNumber", -1);
        if (wave <= 0) return;

        NBTTagCompound persisted = getPersisted(player);
        if (persisted.getInteger(NBT_KEY_LAST_AIR_WAVE) == wave) return;

        boolean shouldSwitch = false;
        if (!wasActive) {
            int rangeMin = HW_INV.getSpawnRangeMinOr(cfg.scanRangeMinFallback);
            int rangeMax = HW_INV.getSpawnRangeMaxOr(cfg.scanRangeMaxFallback);
            shouldSwitch = !hasAnyGroundOrWaterSpawnSurface(player.world, player.getPosition(), rangeMin, rangeMax, cfg.scanStep);
        }

        if (!shouldSwitch) {
            int triesAny = HW_INV.getInt(playerData, "triesSinceWorkingAnySpawn", 0);
            int triesSolid = HW_INV.getInt(playerData, "triesSinceWorkingSolidGroundSpawn", 0);
            if (triesAny >= cfg.failedSpawnTriesForAirFallback && triesSolid >= cfg.failedSpawnTriesForAirFallback) {
                shouldSwitch = true;
            }
        }

        if (!shouldSwitch) return;

        Object template = HW_INV.findTemplate(cfg.airInvasionTemplateName, cfg.airInvasionTemplateFallbackNames);
        if (template == null) {
            ParasitusFix.getLogger().warn(
                    "[HW_INV] Could not find air invasion template '{}' or configured fallbacks.",
                    cfg.airInvasionTemplateName
            );
            return;
        }

        if (!HW_INV.replaceInvasionProfile(playerData, template)) return;

        persisted.setInteger(NBT_KEY_LAST_AIR_WAVE, wave);

        String waveMessage = HW_INV.getTemplateWaveMessage(template);
        if (waveMessage != null && !waveMessage.isEmpty() && !"<NULL>".equals(waveMessage)) {
            player.sendMessage(new TextComponentString(waveMessage));
        }

        ParasitusFix.getLogger().info(
                "[HW_INV] Forced air invasion template '{}' for player '{}' (wave {}, y={}).",
                HW_INV.getTemplateName(template), player.getName(), wave, (int) player.posY
        );
    }

    private static NBTTagCompound getPersisted(EntityPlayer player) {
        NBTTagCompound entityData = player.getEntityData();
        if (!entityData.hasKey(NBT_PERSISTED, 10)) {
            entityData.setTag(NBT_PERSISTED, new NBTTagCompound());
        }
        return entityData.getCompoundTag(NBT_PERSISTED);
    }

    private static boolean hasAnyGroundOrWaterSpawnSurface(World world, BlockPos center, int minRange, int maxRange, int stepRaw) {
        int step = Math.max(1, stepRaw);
        int minSq = Math.max(0, minRange * minRange);
        int maxSq = Math.max(1, maxRange * maxRange);
        int cx = center.getX();
        int cz = center.getZ();

        for (int dx = -maxRange; dx <= maxRange; dx += step) {
            for (int dz = -maxRange; dz <= maxRange; dz += step) {
                int distSq = dx * dx + dz * dz;
                if (distSq < minSq || distSq > maxSq) continue;

                int x = cx + dx;
                int z = cz + dz;
                int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() - 1;
                if (y <= 0) continue;

                BlockPos surface = new BlockPos(x, y, z);
                if (isGroundSurfaceSpawnable(world, surface) || isWaterSurfaceSpawnable(world, surface)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isGroundSurfaceSpawnable(World world, BlockPos surface) {
        IBlockState ground = world.getBlockState(surface);
        if (!ground.getMaterial().blocksMovement()) return false;
        BlockPos spawnPos = surface.up();
        return world.isAirBlock(spawnPos) && world.isAirBlock(spawnPos.up());
    }

    private static boolean isWaterSurfaceSpawnable(World world, BlockPos surface) {
        IBlockState at = world.getBlockState(surface);
        IBlockState below = world.getBlockState(surface.down());
        if (at.getMaterial() != Material.WATER || below.getMaterial() != Material.WATER) return false;
        return !world.getBlockState(surface.up()).isTopSolid();
    }

    private static final class HwInvReflection {
        private boolean resolved;
        private boolean available;

        private Capability<?> playerDataCapability;
        private Field playerDataCapabilityField;
        private Field fieldListMobSpawnTemplates;
        private Field fieldTemplateName;
        private Field fieldTemplateWaveMessage;
        private Field fieldSpawnRangeMin;
        private Field fieldSpawnRangeMax;
        private Method methodDifficultyDataGet;
        private Method methodResetInvasion;
        private Method methodInitNewInvasion;

        Object getPlayerData(EntityPlayer player) {
            ensureResolved();
            if (!available) return null;
            try {
                @SuppressWarnings("unchecked")
                Capability<Object> cap = (Capability<Object>) playerDataCapability;
                return player.getCapability(cap, null);
            } catch (Throwable t) {
                ParasitusFix.getLogger().warn("[HW_INV] Failed to read player invasion capability.", t);
                available = false;
                return null;
            }
        }

        int getSpawnRangeMinOr(int fallback) {
            ensureResolved();
            if (!available) return fallback;
            try {
                return fieldSpawnRangeMin.getInt(null);
            } catch (Throwable ignored) {
                return fallback;
            }
        }

        int getSpawnRangeMaxOr(int fallback) {
            ensureResolved();
            if (!available) return fallback;
            try {
                return fieldSpawnRangeMax.getInt(null);
            } catch (Throwable ignored) {
                return fallback;
            }
        }

        int getInt(Object instance, String fieldName, int fallback) {
            ensureResolved();
            if (!available || instance == null) return fallback;
            try {
                Field f = instance.getClass().getField(fieldName);
                return f.getInt(instance);
            } catch (Throwable ignored) {
                return fallback;
            }
        }

        boolean getBoolean(Object instance, String fieldName, boolean fallback) {
            ensureResolved();
            if (!available || instance == null) return fallback;
            try {
                Field f = instance.getClass().getField(fieldName);
                return f.getBoolean(instance);
            } catch (Throwable ignored) {
                return fallback;
            }
        }

        Object findTemplate(String primaryName, String[] fallbackNames) {
            ensureResolved();
            if (!available) return null;

            List<String> candidates = new ArrayList<>();
            if (primaryName != null && !primaryName.trim().isEmpty()) {
                candidates.add(primaryName.trim());
            }
            if (fallbackNames != null) {
                for (String name : fallbackNames) {
                    if (name == null) continue;
                    String n = name.trim();
                    if (!n.isEmpty()) candidates.add(n);
                }
            }
            if (candidates.isEmpty()) return null;

            try {
                Object difficultyData = methodDifficultyDataGet.invoke(null);
                @SuppressWarnings("unchecked")
                List<Object> templates = (List<Object>) fieldListMobSpawnTemplates.get(difficultyData);
                for (String wanted : candidates) {
                    for (Object template : templates) {
                        String name = String.valueOf(fieldTemplateName.get(template));
                        if (wanted.equals(name)) return template;
                    }
                }
            } catch (Throwable t) {
                ParasitusFix.getLogger().warn("[HW_INV] Failed to read invasion templates.", t);
                available = false;
            }
            return null;
        }

        String getTemplateName(Object template) {
            ensureResolved();
            if (!available || template == null) return "<unknown>";
            try {
                return String.valueOf(fieldTemplateName.get(template));
            } catch (Throwable ignored) {
                return "<unknown>";
            }
        }

        String getTemplateWaveMessage(Object template) {
            ensureResolved();
            if (!available || template == null) return null;
            try {
                Object value = fieldTemplateWaveMessage.get(template);
                return value == null ? null : String.valueOf(value);
            } catch (Throwable ignored) {
                return null;
            }
        }

        boolean replaceInvasionProfile(Object playerData, Object template) {
            ensureResolved();
            if (!available || playerData == null || template == null) return false;
            try {
                methodResetInvasion.invoke(playerData);
                methodInitNewInvasion.invoke(playerData, template);
                return true;
            } catch (Throwable t) {
                ParasitusFix.getLogger().warn("[HW_INV] Failed to replace invasion profile.", t);
                available = false;
                return false;
            }
        }

        private void ensureResolved() {
            if (resolved) return;
            resolved = true;
            try {
                Class<?> invasionClass = Class.forName("com.corosus.inv.Invasion");
                playerDataCapabilityField = invasionClass.getField("PLAYER_DATA_INSTANCE");
                Object capabilityObj = playerDataCapabilityField.get(null);
                if (!(capabilityObj instanceof Capability)) {
                    throw new IllegalStateException("Invasion.PLAYER_DATA_INSTANCE is not a Capability");
                }
                playerDataCapability = (Capability<?>) capabilityObj;

                Class<?> playerDataClass = Class.forName("com.corosus.inv.capabilities.PlayerDataInstance");
                methodResetInvasion = playerDataClass.getMethod("resetInvasion");
                for (Method method : playerDataClass.getMethods()) {
                    if ("initNewInvasion".equals(method.getName()) && method.getParameterCount() == 1) {
                        methodInitNewInvasion = method;
                        break;
                    }
                }
                if (methodInitNewInvasion == null) {
                    throw new NoSuchMethodException("PlayerDataInstance.initNewInvasion");
                }

                Class<?> difficultyDataReaderClass = Class.forName("CoroUtil.difficulty.data.DifficultyDataReader");
                methodDifficultyDataGet = difficultyDataReaderClass.getMethod("getData");
                Object difficultyData = methodDifficultyDataGet.invoke(null);
                fieldListMobSpawnTemplates = difficultyData.getClass().getField("listMobSpawnTemplates");

                Class<?> templateClass = Class.forName("CoroUtil.difficulty.data.spawns.DataMobSpawnsTemplate");
                fieldTemplateName = templateClass.getField("name");
                fieldTemplateWaveMessage = templateClass.getField("wave_message");

                Class<?> cfgAdvClass = Class.forName("com.corosus.inv.config.ConfigAdvancedOptions");
                fieldSpawnRangeMin = cfgAdvClass.getField("spawnRangeMin");
                fieldSpawnRangeMax = cfgAdvClass.getField("spawnRangeMax");

                available = true;
            } catch (Throwable t) {
                available = false;
                ParasitusFix.getLogger().warn("[HW_INV] Anti-skybase integration unavailable.", t);
            }
        }
    }
}
