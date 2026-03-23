package com.toomda.parasitusfix.mixin;

import net.minecraft.launchwrapper.Launch;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ParasitusFixCommonMixinPlugin implements IMixinConfigPlugin {

    private static final String FLOOD_GATE_CLASS = "buildcraft.factory.tile.TileFloodGate";
    private boolean hasBuildCraftFloodGate;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            hasBuildCraftFloodGate = Launch.classLoader.getClassBytes(FLOOD_GATE_CLASS) != null;
        } catch (IOException ignored) {
            hasBuildCraftFloodGate = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !mixinClassName.endsWith("MixinTileFloodGate") || hasBuildCraftFloodGate;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
