package com.toomda.parasitusfix.sevendaystomine;

import com.toomda.parasitusfix.config.ParasitusFixConfig;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import nuparu.sevendaystomine.item.ItemClub;
import nuparu.sevendaystomine.item.ItemQualitySword;
import nuparu.sevendaystomine.item.ItemQualityTool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public final class SevenDaysDamagePatches {
    private SevenDaysDamagePatches() {}

    private static final String[] ATTACK_FIELD_NAMES = {
            "attackDamage",
            "field_150934_a"
    };

    public static void apply() {
        if (!Loader.isModLoaded("sevendaystomine")) return;

        setMeleeDamage("wrench", 3.0F);
        setMeleeDamage("clawhammer", 5.5F);
        setMeleeDamage("iron_pickaxe", 5.5F);
        setMeleeDamage("iron_axe", 5.5F);
        setMeleeDamage("iron_shovel", 4.5F);
        setMeleeDamage("iron_hoe", 3.5F);

        ParasitusFixConfig.SevenDaysTools cfg = ParasitusFixConfig.TOOLS;
        setMeleeDamage("scrap_pickaxe", cfg.scrapPickaxeDamage);
        setMeleeDamage("scrap_axe", cfg.scrapAxeDamage);
        setMeleeDamage("scrap_shovel", cfg.scrapShovelDamage);
        setMeleeDamage("scrap_hoe", cfg.scrapHoeDamage);

        setMeleeDamage("copper_pickaxe", cfg.copperPickaxeDamage);
        setMeleeDamage("copper_axe", cfg.copperAxeDamage);
        setMeleeDamage("copper_shovel", cfg.copperShovelDamage);
        setMeleeDamage("copper_hoe", cfg.copperHoeDamage);
        setMeleeDamage("copper_sword", cfg.copperSwordDamage);

        setMeleeDamage("bronze_pickaxe", cfg.bronzePickaxeDamage);
        setMeleeDamage("bronze_axe", cfg.bronzeAxeDamage);
        setMeleeDamage("bronze_shovel", cfg.bronzeShovelDamage);
        setMeleeDamage("bronze_hoe", cfg.bronzeHoeDamage);
        setMeleeDamage("bronze_sword", cfg.bronzeSwordDamage);
        setMeleeDamage("auger", ParasitusFixConfig.COMBAT.augerDamage);

        setMeleeDamage("armyknife", cfg.armyKnifeDamage);
        setMeleeDamage("kitchenknife", cfg.kitchenKnifeDamage);

        Map<String, Float> clubBase = new HashMap<>();
        clubBase.put("woodenclub", 4.0F);
        clubBase.put("crudeclub", 3.5F);
        clubBase.put("ironreinforcedclub", 5.0F);
        clubBase.put("barbedclub", ParasitusFixConfig.COMBAT.barbedClubDamage);
        clubBase.put("spikedclub", 7.0F);

        for (Item it : ForgeRegistries.ITEMS.getValuesCollection()) {
            ResourceLocation id = it.getRegistryName();
            if (id == null || !"sevendaystomine".equals(id.getResourceDomain())) continue;

            Float base = clubBase.get(id.getResourcePath());
            if (base != null && it instanceof ItemClub) {
                boolean ok = patchSwordBase((ItemQualitySword) it, base);
                Float after = readSwordBase((ItemQualitySword) it);
                System.out.println("[ParasitusFix] Club patch "
                        + id + " -> ok=" + ok + " baseNow=" + after);
            }
        }
    }

    private static void setMeleeDamage(String path, float value) {
        Item it = findSevenDaysItem(path);
        if (it instanceof ItemQualitySword) {
            boolean ok = patchSwordBase((ItemQualitySword) it, value);
            Float after = readSwordBase((ItemQualitySword) it);
            System.out.println("[ParasitusFix] Sword patch " + path + " -> ok=" + ok + " baseNow=" + after);
            return;
        }
        if (it instanceof ItemQualityTool) {
            ((ItemQualityTool) it).setAttackDamage(value);
            System.out.println("[ParasitusFix] Tool patch " + path + " -> " + value);
        } else {
            System.out.println("[ParasitusFix] Item NOT patched (unsupported melee item?): " + path + " -> " + it);
        }
    }

    private static boolean patchSwordBase(ItemQualitySword target, float newVal) {
        boolean ok = false;
        try {
            ok |= setFinalFloat(target, ItemQualitySword.class, ATTACK_FIELD_NAMES, newVal);
        } catch (Throwable ignored) {}
        try {
            ok |= setFinalFloat(target, net.minecraft.item.ItemSword.class, ATTACK_FIELD_NAMES, newVal);
        } catch (Throwable ignored) {}
        return ok;
    }

    private static Float readSwordBase(ItemQualitySword target) {
        try {
            Field f = ItemQualitySword.class.getDeclaredField("attackDamage");
            f.setAccessible(true);
            return f.getFloat(target);
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean setFinalFloat(Object target, Class<?> declaring, String[] names, float val) throws Exception {
        for (String n : names) {
            try {
                Field f = declaring.getDeclaredField(n);
                f.setAccessible(true);
                removeFinal(f);
                f.setFloat(target, val);
                return true;
            } catch (NoSuchFieldException ignored) {}
        }
        for (Field f : declaring.getDeclaredFields()) {
            if (f.getType() == float.class && Modifier.isFinal(f.getModifiers())) {
                f.setAccessible(true);
                float cur = f.getFloat(target);
                if (cur > 0 && cur < 50) {
                    removeFinal(f);
                    f.setFloat(target, val);
                    return true;
                }
            }
        }
        return false;
    }

    private static void removeFinal(Field f) throws Exception {
        Field mods = Field.class.getDeclaredField("modifiers");
        mods.setAccessible(true);
        mods.setInt(f, f.getModifiers() & ~Modifier.FINAL);
    }

    private static Item findSevenDaysItem(String path) {
        ResourceLocation exactId = new ResourceLocation("sevendaystomine", path);
        Item exact = ForgeRegistries.ITEMS.getValue(exactId);
        if (exact != null) return exact;

        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            ResourceLocation id = item.getRegistryName();
            if (id == null || !"sevendaystomine".equals(id.getResourceDomain())) continue;
            if (path.equalsIgnoreCase(id.getResourcePath())) {
                return item;
            }
        }
        return null;
    }
}
