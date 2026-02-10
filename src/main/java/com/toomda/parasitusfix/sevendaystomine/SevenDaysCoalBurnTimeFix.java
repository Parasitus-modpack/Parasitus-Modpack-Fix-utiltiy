package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SevenDaysCoalBurnTimeFix {
    private static final int COAL_BURN_TICKS = 1600;
    private static final int COAL_BLOCK_BURN_TICKS = 16000;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onFurnaceFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Item item = stack.getItem();
        if (item == Items.COAL) {
            int cur = event.getBurnTime();
            if (cur < COAL_BURN_TICKS) {
                event.setBurnTime(COAL_BURN_TICKS);
                ParasitusFix.getLogger().debug("[7DTM] Coal burn time corrected: {} -> {}", cur, COAL_BURN_TICKS);
            }
            return;
        }

        if (item == Item.getItemFromBlock(Blocks.COAL_BLOCK)) {
            int cur = event.getBurnTime();
            if (cur < COAL_BLOCK_BURN_TICKS) {
                event.setBurnTime(COAL_BLOCK_BURN_TICKS);
                ParasitusFix.getLogger().debug("[7DTM] Coal block burn time corrected: {} -> {}", cur, COAL_BLOCK_BURN_TICKS);
            }
        }
    }
}
