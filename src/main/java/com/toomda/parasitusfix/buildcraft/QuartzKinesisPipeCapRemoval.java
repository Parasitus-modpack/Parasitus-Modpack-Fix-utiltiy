package com.toomda.parasitusfix.buildcraft;

import com.toomda.parasitusfix.ParasitusFix;
import com.toomda.parasitusfix.config.ParasitusFixConfig;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.transport.BCTransportPipes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class QuartzKinesisPipeCapRemoval {
    private static final String LEGACY_TARGET_PIPE_CLASS = "buildcraft.transport.pipe.PipePowerQuartz";
    private static final int BOOSTED_TRANSFER = Integer.MAX_VALUE / 2;
    private static final Map<PipeDefinition, PipeApi.RedstoneFluxTransferInfo> ORIGINAL_RF_TRANSFER_INFO =
            new IdentityHashMap<>();
    private static final Set<PipeDefinition> CAPTURED_RF_PIPE_DEFINITIONS =
            Collections.newSetFromMap(new IdentityHashMap<PipeDefinition, Boolean>());
    private static Integer originalLegacyTransfer;

    private QuartzKinesisPipeCapRemoval() {}

    public static void apply() {
        if (ParasitusFixConfig.BUILDCRAFT_TRANSPORT.enableBoostedRfKinesisPipes) {
            if (applyRfPipePatch()) {
                return;
            }
            applyLegacyQuartzPowerPatch();
            return;
        }

        if (restoreRfPipePatch()) {
            return;
        }
        restoreLegacyQuartzPowerPatch();
    }

    private static boolean applyRfPipePatch() {
        try {
            Map<String, PipeDefinition> rfPipes = new LinkedHashMap<>();
            addPipe(rfPipes, "buildcrafttransport:pipe_quartz_rf", BCTransportPipes.quartzRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_wood_rf", BCTransportPipes.woodRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_stone_rf", BCTransportPipes.stoneRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_cobblestone_rf", BCTransportPipes.cobbleRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_gold_rf", BCTransportPipes.goldRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_sandstone_rf", BCTransportPipes.sandstoneRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_iron_rf", BCTransportPipes.ironRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_diamond_rf", BCTransportPipes.diamondRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_diamond_wood_rf", BCTransportPipes.diaWoodRf);

            if (rfPipes.isEmpty()) {
                ParasitusFix.getLogger().info("No BuildCraft RF pipe definitions were available to patch.");
                return false;
            }

            PipeApi.RedstoneFluxTransferInfo boostedInfo =
                    new PipeApi.RedstoneFluxTransferInfo(BOOSTED_TRANSFER, false);

            for (Map.Entry<String, PipeDefinition> entry : rfPipes.entrySet()) {
                PipeDefinition definition = entry.getValue();
                captureOriginalRfTransferInfo(definition);
                PipeApi.rfTransferData.put(definition, boostedInfo);
            }

            ParasitusFix.getLogger().info(
                    "Boosted BuildCraft RF pipe transfer for {} variants to {} RF/t: {}",
                    rfPipes.size(),
                    BOOSTED_TRANSFER,
                    rfPipes.keySet());
            return true;
        } catch (Throwable t) {
            if (t instanceof NoClassDefFoundError || t instanceof ExceptionInInitializerError) {
                ParasitusFix.getLogger().info(
                    "BuildCraft RF pipe definitions not found - " +
                    "falling back to legacy quartz power pipe patch."
                );
                return false;
            }
            ParasitusFix.getLogger().error("Failed to boost BuildCraft RF pipe transfer limits", t);
            return false;
        }
    }

    private static boolean restoreRfPipePatch() {
        try {
            Map<String, PipeDefinition> rfPipes = new LinkedHashMap<>();
            addPipe(rfPipes, "buildcrafttransport:pipe_quartz_rf", BCTransportPipes.quartzRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_wood_rf", BCTransportPipes.woodRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_stone_rf", BCTransportPipes.stoneRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_cobblestone_rf", BCTransportPipes.cobbleRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_gold_rf", BCTransportPipes.goldRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_sandstone_rf", BCTransportPipes.sandstoneRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_iron_rf", BCTransportPipes.ironRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_diamond_rf", BCTransportPipes.diamondRf);
            addPipe(rfPipes, "buildcrafttransport:pipe_diamond_wood_rf", BCTransportPipes.diaWoodRf);

            if (rfPipes.isEmpty()) {
                ParasitusFix.getLogger().info("No BuildCraft RF pipe definitions were available to restore.");
                return false;
            }

            int restored = 0;
            for (Map.Entry<String, PipeDefinition> entry : rfPipes.entrySet()) {
                PipeDefinition definition = entry.getValue();
                if (!CAPTURED_RF_PIPE_DEFINITIONS.contains(definition)) {
                    continue;
                }

                PipeApi.RedstoneFluxTransferInfo original = ORIGINAL_RF_TRANSFER_INFO.get(definition);
                if (original == null) {
                    PipeApi.rfTransferData.remove(definition);
                } else {
                    PipeApi.rfTransferData.put(definition, original);
                }
                restored++;
            }

            ParasitusFix.getLogger().info(
                    "Restored BuildCraft RF pipe transfer limits for {} variants: {}",
                    restored,
                    rfPipes.keySet());
            return restored > 0;
        } catch (Throwable t) {
            if (t instanceof NoClassDefFoundError || t instanceof ExceptionInInitializerError) {
                ParasitusFix.getLogger().info(
                    "BuildCraft RF pipe definitions not found while restoring - " +
                    "falling back to legacy quartz power pipe restore."
                );
                return false;
            }
            ParasitusFix.getLogger().error("Failed to restore BuildCraft RF pipe transfer limits", t);
            return false;
        }
    }

    private static void applyLegacyQuartzPowerPatch() {
        try {
            Class<?> pipeClass = Class.forName(LEGACY_TARGET_PIPE_CLASS);

            ParasitusFix.getLogger().info("Found legacy BuildCraft PipePowerQuartz class: {}", pipeClass.getName());

            Field maxPowerField = findMaxPowerField(pipeClass);
            if (maxPowerField == null) {
                ParasitusFix.getLogger().warn("Could not find power limit field in PipePowerQuartz");
                return;
            }

            Class<?> declaringClass = maxPowerField.getDeclaringClass();
            String fieldName = maxPowerField.getName();

            if (!declaringClass.getName().equals(LEGACY_TARGET_PIPE_CLASS)) {
                ParasitusFix.getLogger().warn(
                    "Field '{}' is declared in parent class '{}' instead of '{}'. " +
                    "Modifying it would affect all power pipes! Aborting for safety.",
                    fieldName, declaringClass.getName(), LEGACY_TARGET_PIPE_CLASS
                );
                return;
            }

            maxPowerField.setAccessible(true);
            removeFinal(maxPowerField);

            int oldValue = maxPowerField.getInt(null);
            if (originalLegacyTransfer == null) {
                originalLegacyTransfer = oldValue;
            }

            if (oldValue != 10240 && oldValue != 1024 && oldValue > 0 && oldValue < 1000000) {
                ParasitusFix.getLogger().warn(
                    "Unexpected power limit value: {} (expected 10240 for Quartz Kinesis Pipe). " +
                    "Proceeding with caution...", oldValue
                );
            }

            maxPowerField.setInt(null, BOOSTED_TRANSFER);

            int newValue = maxPowerField.getInt(null);
            if (newValue == BOOSTED_TRANSFER) {
                ParasitusFix.getLogger().info(
                    "Successfully removed legacy Quartz Kinesis Pipe power limit: {} RF/t -> {} RF/t",
                    oldValue, newValue
                );
            } else {
                ParasitusFix.getLogger().error(
                    "Failed to update legacy power limit! Expected: {}, Got: {}",
                    BOOSTED_TRANSFER, newValue
                );
            }
        } catch (ClassNotFoundException e) {
            ParasitusFix.getLogger().info(
                "Legacy BuildCraft PipePowerQuartz not found - no legacy quartz power patch applied."
            );
        } catch (Throwable t) {
            ParasitusFix.getLogger().error("Failed to remove legacy Quartz Kinesis Pipe power limit", t);
        }
    }

    private static void restoreLegacyQuartzPowerPatch() {
        if (originalLegacyTransfer == null) {
            ParasitusFix.getLogger().info("Legacy BuildCraft quartz power pipe limit was never overridden; nothing to restore.");
            return;
        }

        try {
            Class<?> pipeClass = Class.forName(LEGACY_TARGET_PIPE_CLASS);
            Field maxPowerField = findMaxPowerField(pipeClass);
            if (maxPowerField == null) {
                ParasitusFix.getLogger().warn("Could not find power limit field in PipePowerQuartz while restoring.");
                return;
            }

            maxPowerField.setAccessible(true);
            removeFinal(maxPowerField);
            maxPowerField.setInt(null, originalLegacyTransfer);
            ParasitusFix.getLogger().info(
                "Restored legacy Quartz Kinesis Pipe power limit to {} RF/t",
                originalLegacyTransfer
            );
        } catch (ClassNotFoundException e) {
            ParasitusFix.getLogger().info(
                "Legacy BuildCraft PipePowerQuartz not found while restoring - nothing to do."
            );
        } catch (Throwable t) {
            ParasitusFix.getLogger().error("Failed to restore legacy Quartz Kinesis Pipe power limit", t);
        }
    }

    private static Field findMaxPowerField(Class<?> pipeClass) {
        String[] possibleNames = {"maxPower", "MAX_POWER", "powerCapacity", "maxPowerFlow"};
        
        for (String name : possibleNames) {
            try {
                Field field = pipeClass.getDeclaredField(name);
                if (field.getType() == int.class || field.getType() == long.class) {
                    ParasitusFix.getLogger().info("Found power field by name: {}", name);
                    return field;
                }
            } catch (NoSuchFieldException ignored) {}
        }
        
        for (Field field : pipeClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (field.getType() != int.class && field.getType() != long.class) continue;
            
            String name = field.getName().toLowerCase();
            if (name.contains("max") || name.contains("capacity") || 
                name.contains("limit") || name.contains("power")) {
                
                ParasitusFix.getLogger().info("Found potential power field: {}", field.getName());
                return field;
            }
        }
        
        return null;
    }

    private static void removeFinal(Field field) throws Exception {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
    }

    private static void addPipe(Map<String, PipeDefinition> pipes, String itemId, PipeDefinition definition) {
        if (definition != null) {
            pipes.put(itemId, definition);
        }
    }

    private static void captureOriginalRfTransferInfo(PipeDefinition definition) {
        if (CAPTURED_RF_PIPE_DEFINITIONS.add(definition)) {
            ORIGINAL_RF_TRANSFER_INFO.put(definition, PipeApi.rfTransferData.get(definition));
        }
    }

    @Mod.EventBusSubscriber(modid = ParasitusFix.MODID, value = Side.CLIENT)
    public static class TooltipHandler {
        @SideOnly(Side.CLIENT)
        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.isEmpty()) return;

            Item item = stack.getItem();
            ResourceLocation itemId = item.getRegistryName();
            if (itemId == null) return;

            if (isBoostedRfPipe(itemId)) {
                List<String> tooltip = event.getToolTip();
                
                Iterator<String> iterator = tooltip.iterator();
                boolean first = true;
                while (iterator.hasNext()) {
                    String line = iterator.next();
                    if (first) {
                        first = false;
                        continue;
                    }
                    
                    String stripped = line.replaceAll("§.", "").toLowerCase();
                    if (stripped.contains("rf") || 
                        stripped.contains("redstone flux") ||
                        stripped.contains("limit") ||
                        stripped.contains("max") ||
                        stripped.contains("/t") ||
                        stripped.contains("per tick")) {
                        iterator.remove();
                    }
                }

                tooltip.add("No limit in redstone flux sent");
            }
        }

        private static boolean isBoostedRfPipe(ResourceLocation itemId) {
            if (!ParasitusFixConfig.BUILDCRAFT_TRANSPORT.enableBoostedRfKinesisPipes) return false;
            if (!"buildcrafttransport".equals(itemId.getResourceDomain())) return false;
            String path = itemId.getResourcePath();
            return path.startsWith("pipe_") && path.endsWith("_rf");
        }
    }
}
