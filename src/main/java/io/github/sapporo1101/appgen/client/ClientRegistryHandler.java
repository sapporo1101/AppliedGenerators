package io.github.sapporo1101.appgen.client;


import appeng.client.InitScreens;
import io.github.sapporo1101.appgen.client.gui.*;
import io.github.sapporo1101.appgen.client.render.PatternBufferRenderer;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.menu.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientRegistryHandler {
    public static final ClientRegistryHandler INSTANCE = new ClientRegistryHandler();

    @SubscribeEvent
    public void registerGui(RegisterMenuScreensEvent event) {
        InitScreens.register(event, FluxCellMenu.TYPE, FluxCellScreen::new, "/screens/flux_cell.json");
        InitScreens.register(event, GenesisSynthesizerMenu.TYPE, GenesisSynthesizerScreen::new, "/screens/genesis_synthesizer.json");
        InitScreens.register(event, SingularityGeneratorMenu.TYPE, SingularityGeneratorScreen::new, "/screens/singularity_generator.json");
        InitScreens.register(event, FluxGeneratorMenu.TYPE, FluxGeneratorScreen::new, "/screens/flux_generator.json");
        InitScreens.register(event, PatternBufferMenu.TYPE, PatternBufferScreen::new, "/screens/pattern_buffer.json");
        InitScreens.register(event, SmelterMenu.TYPE, SmelterScreen::new, "/screens/smelter.json");
    }

    @SubscribeEvent
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(AGSingletons.PATTERN_BUFFER.get().getBlockEntityType(), PatternBufferRenderer::new);
    }
}
