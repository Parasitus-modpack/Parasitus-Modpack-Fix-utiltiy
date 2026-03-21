package com.toomda.parasitusfix.mixin;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("ParasitusFixMixinLoader")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions({"com.toomda.parasitusfix.mixin"})
public class ParasitusFixMixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static final String MIXIN_CONFIG = "mixins.parasitusfix.early.json";

    @Override
    public List<String> getMixinConfigs() {
        return FMLLaunchHandler.side() != null && FMLLaunchHandler.side().isClient()
                ? Collections.singletonList(MIXIN_CONFIG)
                : Collections.emptyList();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
