package io.github.sapporo1101.appgen.util;

import io.github.sapporo1101.appgen.AppliedGenerators;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class AGTags {
    public static final TagKey<Item> GENERATORS = TagKey.create(Registries.ITEM, AppliedGenerators.id("generators"));
    public static final TagKey<Item> SINGULARITY_GENERATORS = TagKey.create(Registries.ITEM, AppliedGenerators.id("generators/singularity"));
    public static final TagKey<Item> FLUX_GENERATORS = TagKey.create(Registries.ITEM, AppliedGenerators.id("generators/flux"));
    public static final TagKey<Item> EMBER_DUST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/ember"));
    public static final TagKey<Item> GOLD_DUST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/gold"));
    public static final TagKey<Item> COPPER_DUST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/copper"));
    public static final TagKey<Item> DIAMOND_DUST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/diamond"));
    public static final TagKey<Item> NETHERITE_DUST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "dusts/netherite"));
    public static final TagKey<Item> EMBER_BLOCK = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "storage_blocks/ember"));
    public static final TagKey<Item> EMBER_CRYSTAL = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "gems/ember"));

    public static final TagKey<Block> EMBER_BLOCK_BLOCK = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", "storage_blocks/ember"));
}
