package io.github.sapporo1101.appgen.datagen;

import appeng.core.ConventionTags;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.common.AGRegistryHandler;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.util.AGTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AGBlockTagProvider extends BlockTagsProvider {

    public AGBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AppliedGenerators.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        for (var block : AGRegistryHandler.INSTANCE.getBlocks()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
        }
        tag(AGTags.EMBER_BLOCK_BLOCK)
                .add(AGSingletons.EMBER_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS)
                .addTag(AGTags.EMBER_BLOCK_BLOCK);
        tag(ConventionTags.BUDDING_BLOCKS_BLOCKS)
                .add(AGSingletons.BUDDING_EMBER_DAMAGED.get())
                .add(AGSingletons.BUDDING_EMBER_CHIPPED.get())
                .add(AGSingletons.BUDDING_EMBER_FLAWED.get())
                .add(AGSingletons.BUDDING_EMBER_FLAWLESS.get());
        tag(ConventionTags.BUDS_BLOCKS)
                .add(AGSingletons.EMBER_BUD_SMALL.get())
                .add(AGSingletons.EMBER_BUD_MEDIUM.get())
                .add(AGSingletons.EMBER_BUD_LARGE.get());
        tag(ConventionTags.CLUSTERS_BLOCKS)
                .add(AGSingletons.EMBER_CLUSTER.get());
    }
}
