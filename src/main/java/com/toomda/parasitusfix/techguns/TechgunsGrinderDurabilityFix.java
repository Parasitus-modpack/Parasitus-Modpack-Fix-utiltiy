package com.toomda.parasitusfix.techguns;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import techguns.tileentities.operation.GrinderRecipes;
import techguns.util.ItemStackOreDict;

import java.lang.reflect.Field;

public final class TechgunsGrinderDurabilityFix {
    private TechgunsGrinderDurabilityFix() {
    }

    public static void apply() {
        if (GrinderRecipes.recipes == null || GrinderRecipes.recipes.isEmpty()) {
            return;
        }

        int changed = 0;
        try {
            Field inputField = GrinderRecipes.GrinderRecipe.class.getDeclaredField("input");
            inputField.setAccessible(true);

            for (Object recipe : GrinderRecipes.recipes) {
                ItemStackOreDict input = (ItemStackOreDict) inputField.get(recipe);
                if (input == null || input.item == null || input.item.isEmpty()) {
                    continue;
                }
                ItemStack stack = input.item;
                if (stack.isItemStackDamageable() && stack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
                    stack.setItemDamage(OreDictionary.WILDCARD_VALUE);
                    changed++;
                }
            }
        } catch (Throwable t) {
            ParasitusFix.getLogger().warn("Techguns grinder durability fix failed.", t);
            return;
        }

        if (changed > 0) {
            ParasitusFix.getLogger().info("Techguns grinder durability fix: updated {} recipe inputs.", changed);
        }
    }
}
