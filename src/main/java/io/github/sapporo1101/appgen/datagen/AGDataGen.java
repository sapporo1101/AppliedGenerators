package io.github.sapporo1101.appgen.datagen;

import io.github.sapporo1101.appgen.AppliedGenerators;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.util.Util;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@EventBusSubscriber(modid = AppliedGenerators.MODID)
public class AGDataGen {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        DataGenerator.PackGenerator pack = event.getGenerator().getVanillaPack(true);
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        CompletableFuture<HolderLookup.Provider> registries = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        pack.addProvider(bindRegistries(AGRecipeProvider.Runner::new, registries));
        pack.addProvider(output -> new AGLootTableProvider(output, lookup));
        pack.addProvider(output -> new AGBlockTagProvider(output, lookup));
        pack.addProvider(output -> new AGItemTagProvider(output, lookup));
    }

    private static <T extends DataProvider> DataProvider.Factory<@NotNull T> bindRegistries(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> target, CompletableFuture<HolderLookup.Provider> registries) {
        return output -> target.apply(output, registries);
    }
}
