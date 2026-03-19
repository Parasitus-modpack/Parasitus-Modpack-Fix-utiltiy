package com.toomda.parasitusfix.buildcraft;

import com.toomda.parasitusfix.ParasitusFix;
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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuartzKinesisPipeCapRemoval {
    private static final int BOOSTED_TRANSFER = Integer.MAX_VALUE / 2;

    private QuartzKinesisPipeCapRemoval() {}

    public static void apply() {
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
                return;
            }

            PipeApi.RedstoneFluxTransferInfo boostedInfo =
                    new PipeApi.RedstoneFluxTransferInfo(BOOSTED_TRANSFER, false);

            for (Map.Entry<String, PipeDefinition> entry : rfPipes.entrySet()) {
                PipeApi.rfTransferData.put(entry.getValue(), boostedInfo);
            }

            ParasitusFix.getLogger().info(
                    "Boosted BuildCraft RF pipe transfer for {} variants to {} RF/t: {}",
                    rfPipes.size(),
                    BOOSTED_TRANSFER,
                    rfPipes.keySet());
        } catch (Throwable t) {
            ParasitusFix.getLogger().error("Failed to boost BuildCraft RF pipe transfer limits", t);
        }
    }

    private static void addPipe(Map<String, PipeDefinition> pipes, String itemId, PipeDefinition definition) {
        if (definition != null) {
            pipes.put(itemId, definition);
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
            if (itemId == null || !isBoostedRfPipe(itemId)) return;

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
                if (stripped.contains("rf")
                        || stripped.contains("redstone flux")
                        || stripped.contains("limit")
                        || stripped.contains("max")
                        || stripped.contains("/t")
                        || stripped.contains("per tick")) {
                    iterator.remove();
                }
            }

            tooltip.add("No limit in redstone flux sent");
        }

        private static boolean isBoostedRfPipe(ResourceLocation itemId) {
            if (!"buildcrafttransport".equals(itemId.getResourceDomain())) return false;
            String path = itemId.getResourcePath();
            return path.startsWith("pipe_") && path.endsWith("_rf");
        }
    }
}
