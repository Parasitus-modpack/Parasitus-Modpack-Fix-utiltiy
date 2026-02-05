package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EnchantItemFix {
    private static final String MOD_7DTM = "sevendaystomine";

    private static final String CANNED_PREFIX = "canned";

    public EnchantItemFix() {}

    @SubscribeEvent
    public void onEnchantmentLevelSet(EnchantmentLevelSetEvent e) {
        ItemStack stack = e.getItem();
        if (isBlocked(stack)) {
            e.setLevel(0);
            ParasitusFix.getLogger().info("[EnchantBlock] Blocked enchanting table on {}", idOf(stack));
        }
    }

    @SubscribeEvent
    public void onAnvil(AnvilUpdateEvent e) {
        if (isBlocked(e.getLeft()) || isBlocked(e.getRight())) {
            e.setCanceled(true);
            ParasitusFix.getLogger().info("[EnchantBlock] Blocked anvil op: left={}, right={}", idOf(e.getLeft()), idOf(e.getRight()));
        }
    }

    private static boolean isBlocked(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null || !MOD_7DTM.equals(id.getResourceDomain())) return false;

        return id.getResourcePath().startsWith(CANNED_PREFIX) || id.getResourcePath().equals("barbedclub") || id.getResourcePath().equals("wrench");
    }

    private static String idOf(ItemStack s) {
        if (s == null || s.isEmpty()) return "empty";
        ResourceLocation id = s.getItem().getRegistryName();
        return id == null ? "unknown" : id.toString();
    }
}
