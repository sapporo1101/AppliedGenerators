package io.github.sapporo1101.appgen.datagen;

import io.github.sapporo1101.appgen.common.AGRegistryHandler;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.common.blocks.interfaces.ISpecialDrop;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class AGLootTableProvider extends LootTableProvider {

    public AGLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Collections.emptySet(), Collections.singletonList(new LootTableProvider.SubProviderEntry(AGSubProvider::new, LootContextParamSets.BLOCK)), provider);
    }

    public static class AGSubProvider extends BlockLootSubProvider {


        protected AGSubProvider(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            for (var block : AGRegistryHandler.INSTANCE.getBlocks()) {
                if (!(block instanceof ISpecialDrop)) {
                    add(block, createSingleItemTable(block));
                }
            }
            add(AGSingletons.EMBER_BUD_SMALL.get(), createSingleItemTableWithSilkTouch(AGSingletons.EMBER_BUD_SMALL.get(), AGSingletons.EMBER_DUST));
            add(AGSingletons.EMBER_BUD_MEDIUM.get(), createSingleItemTableWithSilkTouch(AGSingletons.EMBER_BUD_MEDIUM.get(), AGSingletons.EMBER_DUST));
            add(AGSingletons.EMBER_BUD_LARGE.get(), createSingleItemTableWithSilkTouch(AGSingletons.EMBER_BUD_LARGE.get(), AGSingletons.EMBER_DUST));
            add(AGSingletons.EMBER_CLUSTER.get(), createSilkTouchDispatchTable(AGSingletons.EMBER_CLUSTER.get(),
                    LootItem.lootTableItem(AGSingletons.EMBER_CRYSTAL)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                            .apply(ApplyBonusCount.addUniformBonusCount(getEnchantment(Enchantments.FORTUNE)))
                            .apply(ApplyExplosionDecay.explosionDecay()))
            );
            add(AGSingletons.BUDDING_EMBER_DAMAGED.get(), createSingleItemTableWithSilkTouch(AGSingletons.BUDDING_EMBER_DAMAGED.get(), AGSingletons.EMBER_BLOCK));
            add(AGSingletons.BUDDING_EMBER_CHIPPED.get(), createSingleItemTableWithSilkTouch(AGSingletons.BUDDING_EMBER_CHIPPED.get(), AGSingletons.BUDDING_EMBER_DAMAGED));
            add(AGSingletons.BUDDING_EMBER_FLAWED.get(), createSingleItemTableWithSilkTouch(AGSingletons.BUDDING_EMBER_FLAWED.get(), AGSingletons.BUDDING_EMBER_CHIPPED));
            add(AGSingletons.BUDDING_EMBER_FLAWLESS.get(), createSingleItemTable(AGSingletons.BUDDING_EMBER_FLAWED));
        }

        @SuppressWarnings("SameParameterValue")
        protected final Holder<Enchantment> getEnchantment(ResourceKey<Enchantment> key) {
            return registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        }

        @Override
        protected @NotNull Iterable<Block> getKnownBlocks() {
            return AGRegistryHandler.INSTANCE.getBlocks();
        }
    }
}