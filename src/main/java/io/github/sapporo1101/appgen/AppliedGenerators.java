package io.github.sapporo1101.appgen;

import appeng.init.InitCapabilityProviders;
import io.github.sapporo1101.appgen.api.AGComponents;
import io.github.sapporo1101.appgen.client.ClientRegistryHandler;
import io.github.sapporo1101.appgen.common.AGRegistryHandler;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.network.AGNetworkHandler;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(AppliedGenerators.MODID)
public class AppliedGenerators {
    public static final String MODID = "appgen";
    public static AppliedGenerators INSTANCE;

    public AppliedGenerators(IEventBus bus, ModContainer container) {
        assert INSTANCE == null;
        INSTANCE = this;
        if (!container.getModId().equals(MODID)) {
            throw new IllegalArgumentException("Invalid ID: " + MODID);
        }
        AGRegistryHandler.INSTANCE = new AGRegistryHandler(bus);
        AGSingletons.init(AGRegistryHandler.INSTANCE);
        bus.addListener((RegisterEvent e) -> {
            if (e.getRegistryKey().equals(Registries.CREATIVE_MODE_TAB)) {
                AGRegistryHandler.INSTANCE.registerTab(e.getRegistry(Registries.CREATIVE_MODE_TAB));
            }
        });
        if (FMLEnvironment.getDist().isClient()) {
            bus.register(ClientRegistryHandler.INSTANCE);
        }

        bus.addListener(this::commonSetup);
        bus.addListener(InitCapabilityProviders::register);
        bus.addListener(AGNetworkHandler.INSTANCE::onRegister);

        AGComponents.DR.register(bus);
        NeoForge.EVENT_BUS.addListener(this::sendSyncRecipe);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        AGRegistryHandler.INSTANCE.init();
    }

    public void sendSyncRecipe(OnDatapackSyncEvent event) {
        event.sendRecipes(GenesisSynthesizerRecipe.TYPE);
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MODID, id);
    }

    public static String stringId(String id) {
        return id(id).toString();
    }
}
