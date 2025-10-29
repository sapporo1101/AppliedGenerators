package io.github.sapporo1101.appgen.datagen;

import appeng.api.ids.AETags;
import appeng.api.util.AEColor;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.handlers.ChargerRecipeBuilder;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;
import com.glodblock.github.appflux.common.AFSingletons;
import com.glodblock.github.extendedae.recipe.CircuitCutterRecipeBuilder;
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipeBuilder;
import com.glodblock.github.extendedae.recipe.CrystalFixerRecipeBuilder;
import com.glodblock.github.glodium.util.GlodUtil;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.common.AGSingletons;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipeBuilder;
import io.github.sapporo1101.appgen.util.AGTags;
import io.github.sapporo1101.appgen.xmod.ModConstants;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.pedroksl.advanced_ae.recipes.ReactionChamberRecipeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AGRecipeProvider extends RecipeProvider {

    private static final String HAS_ITEM = "has_item";

    public AGRecipeProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> lookup) {
        super(out, lookup);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {

        // Crafting Table Recipes
        // Pattern Buffer
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.PATTERN_BUFFER)
                .pattern("IGI")
                .pattern("APF")
                .pattern("IGI")
                .define('I', Items.IRON_INGOT)
                .define('G', AEBlocks.QUARTZ_GLASS)
                .define('A', AEItems.ANNIHILATION_CORE)
                .define('P', AEItems.BLANK_PATTERN)
                .define('F', AEItems.FORMATION_CORE)
                .unlockedBy(HAS_ITEM, has(AEItems.BLANK_PATTERN))
                .save(recipeOutput, AppliedGenerators.id("crafting/pattern_buffer"));
        // Ember Block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.EMBER_BLOCK)
                .pattern("EE")
                .pattern("EE")
                .define('E', AGSingletons.EMBER_CRYSTAL)
                .unlockedBy(HAS_ITEM, has(AGSingletons.EMBER_CRYSTAL))
                .save(recipeOutput, AppliedGenerators.id("crafting/ember_block"));
        // Ember Crystal
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, AGSingletons.EMBER_CRYSTAL, 4)
                .requires(AGTags.EMBER_BLOCK)
                .unlockedBy(HAS_ITEM, has(AGTags.EMBER_BLOCK))
                .save(recipeOutput, AppliedGenerators.id("crafting/ember_crystal"));
        // Flux Cell
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_CELL)
                .pattern("RDR")
                .pattern("DCD")
                .pattern("RDR")
                .define('R', AFSingletons.HARDEN_INSULATING_RESIN)
                .define('D', AEItems.SKY_DUST)
                .define('C', AFSingletons.CORE_1k)
                .unlockedBy(HAS_ITEM, has(AEItems.CALCULATION_PROCESSOR))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_cell"));
        // Dense Flux Cell
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.DENSE_FLUX_CELL)
                .pattern("CCC")
                .pattern("COC")
                .pattern("CCC")
                .define('C', AGSingletons.FLUX_CELL)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.FLUX_CELL))
                .save(recipeOutput, AppliedGenerators.id("crafting/dense_flux_cell"));
        // 1k Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_1K)
                .pattern("DED")
                .pattern("ELE")
                .pattern("DED")
                .define('D', Items.GUNPOWDER)
                .define('E', AGSingletons.EMBER_CRYSTAL)
                .define('L', AEItems.LOGIC_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AEItems.LOGIC_PROCESSOR))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_1k"));
        // 4k Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_4K)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', Items.GUNPOWDER)
                .define('C', AGSingletons.COMPONENT_1K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_1K))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_4k"));
        // 16k Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_16K)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', AGTags.COPPER_DUST)
                .define('C', AGSingletons.COMPONENT_4K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_4K))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_16k"));
        // 64k Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_64K)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', AGTags.COPPER_DUST)
                .define('C', AGSingletons.COMPONENT_16K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_16K))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_64k"));
        // 256k Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_256K)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', AGTags.GOLD_DUST)
                .define('C', AGSingletons.COMPONENT_64K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_64K))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_256k"));
        // 1M Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_1M)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', AGTags.GOLD_DUST)
                .define('C', AGSingletons.COMPONENT_256K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_256K))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_1m"));
        // 4M Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_4M)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', AGTags.DIAMOND_DUST)
                .define('C', AGSingletons.COMPONENT_1M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_1M))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_4m"));
        // 16M Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_16M)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', AGTags.DIAMOND_DUST)
                .define('C', AGSingletons.COMPONENT_4M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_4M))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_16m"));
        // 64M Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_64M)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', AGTags.NETHERITE_DUST)
                .define('C', AGSingletons.COMPONENT_16M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_16M))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_64m"));
        // 256M Generating Component
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.COMPONENT_256M)
                .pattern("DOD")
                .pattern("CQC")
                .pattern("DCD")
                .define('D', AGTags.NETHERITE_DUST)
                .define('C', AGSingletons.COMPONENT_64M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('Q', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_64M))
                .save(recipeOutput, AppliedGenerators.id("crafting/component_256m"));
        // 1k Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_1K)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AEBlocks.FLUIX_BLOCK)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_1K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_1K))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_1k"));
        // 4k Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_4K)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_1K)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_4K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_4K))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_4k"));
        // 16k Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_16K)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_4K)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_16K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_16K))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_16k"));
        // 64k Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_64K)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_16K)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_64K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_64K))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_64k"));
        // 256k Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_256K)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_64K)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_256K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_256K))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_256k"));
        // 1M Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_1M)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_256K)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_1M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_1M))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_1m"));
        // 4M Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_4M)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_1M)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_4M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_4M))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_4m"));
        // 16M Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_16M)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_4M)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_16M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_16M))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_16m"));
        // 64M Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_64M)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_16M)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_64M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_64M))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_64m"));
        // 256M Singularity Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SINGULARITY_GENERATOR_256M)
                .pattern("IQI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.SINGULARITY_GENERATOR_64M)
                .define('Q', AEBlocks.QUANTUM_RING)
                .define('C', AGSingletons.COMPONENT_256M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_256M))
                .save(recipeOutput, AppliedGenerators.id("crafting/singularity_generator_256m"));
        // 1k Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_1K)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AEBlocks.FLUIX_BLOCK)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_1K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_1K))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_1k"));
        // 4k Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_4K)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_1K)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_4K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_4K))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_4k"));
        // 16k Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_16K)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_4K)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_16K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_16K))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_16k"));
        // 64k Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_64K)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_16K)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_64K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_64K))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_64k"));
        // 256k Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_256K)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_64K)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_256K)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_256K))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_256k"));
        // 1M Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_1M)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_256K)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_1M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_1M))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_1m"));
        // 4M Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_4M)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_1M)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_4M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_4M))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_4m"));
        // 16M Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_16M)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_4M)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_16M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_16M))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_16m"));
        // 64M Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_64M)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_16M)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_64M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_64M))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_64m"));
        // 256M Flux Generator
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.FLUX_GENERATOR_256M)
                .pattern("IFI")
                .pattern("GCG")
                .pattern("IOI")
                .define('I', Items.IRON_INGOT)
                .define('G', AGSingletons.FLUX_GENERATOR_64M)
                .define('F', AGSingletons.FLUX_CELL)
                .define('C', AGSingletons.COMPONENT_256M)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .unlockedBy(HAS_ITEM, has(AGSingletons.COMPONENT_256M))
                .save(recipeOutput, AppliedGenerators.id("crafting/flux_generator_256m"));
        // Genesis Synthesizer
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.GENESIS_SYNTHESIZER)
                .pattern("IEI")
                .pattern("OMO")
                .pattern("ITI")
                .define('I', Items.IRON_INGOT)
                .define('E', AGSingletons.EMBER_CRYSTAL_CHARGED)
                .define('O', AGSingletons.ORIGINATION_PROCESSOR)
                .define('M', AEBlocks.NOT_SO_MYSTERIOUS_CUBE)
                .define('T', AEBlocks.SKY_STONE_TANK)
                .unlockedBy(HAS_ITEM, has(AEBlocks.NOT_SO_MYSTERIOUS_CUBE))
                .save(recipeOutput, AppliedGenerators.id("crafting/genesis_synthesizer"));
        // Smelter
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AGSingletons.SMELTER)
                .pattern("IEI")
                .pattern("PFP")
                .pattern("IEI")
                .define('I', Items.IRON_INGOT)
                .define('P', AEItems.CALCULATION_PROCESSOR)
                .define('E', AGSingletons.EMBER_CRYSTAL_CHARGED)
                .define('F', Blocks.FURNACE)
                .unlockedBy(HAS_ITEM, has(Blocks.FURNACE))
                .save(recipeOutput, AppliedGenerators.id("crafting/smelter"));

        // Smelting Recipes
        // Copper Ingot
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(AGSingletons.COPPER_DUST),
                        RecipeCategory.MISC,
                        new ItemStack(Items.COPPER_INGOT),
                        0.35F, 200
                )
                .unlockedBy(HAS_ITEM, has(AGSingletons.COPPER_DUST))
                .save(recipeOutput, AppliedGenerators.id("smelting/copper_ingot"));
        // Gold Ingot
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(AGSingletons.GOLD_DUST),
                        RecipeCategory.MISC,
                        new ItemStack(Items.GOLD_INGOT),
                        0.5F, 200
                )
                .unlockedBy(HAS_ITEM, has(AGSingletons.GOLD_DUST))
                .save(recipeOutput, AppliedGenerators.id("smelting/gold_ingot"));
        // Netherite Ingot
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(AGSingletons.NETHERITE_DUST),
                        RecipeCategory.MISC,
                        new ItemStack(Items.NETHERITE_INGOT),
                        1F, 200
                )
                .unlockedBy(HAS_ITEM, has(AGSingletons.NETHERITE_DUST))
                .save(recipeOutput, AppliedGenerators.id("smelting/netherite_ingot"));

        // Blasting Recipes
        // Copper Ingot
        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(AGSingletons.COPPER_DUST),
                        RecipeCategory.MISC,
                        new ItemStack(Items.COPPER_INGOT),
                        0.35F, 100
                )
                .unlockedBy(HAS_ITEM, has(AGSingletons.COPPER_DUST))
                .save(recipeOutput, AppliedGenerators.id("blasting/copper_ingot"));
        // Gold Ingot
        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(AGSingletons.GOLD_DUST),
                        RecipeCategory.MISC,
                        new ItemStack(Items.GOLD_INGOT),
                        0.5F, 100
                )
                .unlockedBy(HAS_ITEM, has(AGSingletons.GOLD_DUST))
                .save(recipeOutput, AppliedGenerators.id("blasting/gold_ingot"));
        // Netherite Ingot
        SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(AGSingletons.NETHERITE_DUST),
                        RecipeCategory.MISC,
                        new ItemStack(Items.NETHERITE_INGOT),
                        1F, 100
                )
                .unlockedBy(HAS_ITEM, has(AGSingletons.NETHERITE_DUST))
                .save(recipeOutput, AppliedGenerators.id("blasting/netherite_ingot"));


        // Inscriber Recipes
        // Inscriber Origination Press
        InscriberRecipeBuilder.inscribe(AEItems.FORMATION_CORE, AGSingletons.ORIGINATION_PRESS, 1)
                .setTop(Ingredient.of(AEItems.CALCULATION_PROCESSOR_PRESS))
                .setBottom(Ingredient.of(AEItems.ENGINEERING_PROCESSOR_PRESS))
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, AppliedGenerators.id("inscriber/origination_press"));
        // Ember Dust
        InscriberRecipeBuilder.inscribe(AGSingletons.EMBER_CRYSTAL, AGSingletons.EMBER_DUST, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, AppliedGenerators.id("inscriber/ember_dust"));
        // Origination Circuit
        InscriberRecipeBuilder.inscribe(AGSingletons.EMBER_CRYSTAL, AGSingletons.ORIGINATION_PRINT, 1)
                .setTop(Ingredient.of(AGSingletons.ORIGINATION_PRESS))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, AppliedGenerators.id("inscriber/printed_origination_processor"));
        // Origination Processor
        InscriberRecipeBuilder.inscribe(ConventionTags.REDSTONE, AGSingletons.ORIGINATION_PROCESSOR, 1)
                .setTop(Ingredient.of(AGSingletons.ORIGINATION_PRINT))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(recipeOutput, AppliedGenerators.id("inscriber/origination_processor"));
        // Inscriber Origination Press (Duplicate)
        InscriberRecipeBuilder.inscribe(Items.IRON_BLOCK, AGSingletons.ORIGINATION_PRESS, 1)
                .setTop(Ingredient.of(AGSingletons.ORIGINATION_PRESS))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, AppliedGenerators.id("inscriber/origination_press_duplicate"));
        // Gold Dust
        InscriberRecipeBuilder.inscribe(Items.GOLD_INGOT, AGSingletons.GOLD_DUST, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, AppliedGenerators.id("inscriber/gold_dust"));
        // Copper Dust
        InscriberRecipeBuilder.inscribe(Items.COPPER_INGOT, AGSingletons.COPPER_DUST, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, AppliedGenerators.id("inscriber/copper_dust"));
        // Netherite Dust
        InscriberRecipeBuilder.inscribe(Items.NETHERITE_INGOT, AGSingletons.NETHERITE_DUST, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .save(recipeOutput, AppliedGenerators.id("inscriber/netherite_dust"));

        // Charger Recipes
        // Charged Ember Crystal
        ChargerRecipeBuilder.charge(recipeOutput, AppliedGenerators.id("charger/charged_ember_crystal"), AGSingletons.EMBER_CRYSTAL, AGSingletons.EMBER_CRYSTAL_CHARGED);

        // Crystal Assembler Recipes
        // Ember Crystal
        CrystalAssemblerRecipeBuilder.assemble(AGSingletons.EMBER_CRYSTAL, 8)
                .input(AEItems.FLUIX_CRYSTAL, 4)
                .input(Items.GUNPOWDER, 4)
                .input(Items.BLAZE_POWDER, 4)
                .fluid(Fluids.LAVA, 100)
                .save(recipeOutput, AppliedGenerators.id("assembler/ember_crystal"));
        // Origination Processor
        CrystalAssemblerRecipeBuilder.assemble(AGSingletons.ORIGINATION_PROCESSOR, 4)
                .input(AGSingletons.ORIGINATION_PRINT, 4)
                .input(AEItems.SILICON_PRINT, 4)
                .input(ConventionTags.REDSTONE, 4)
                .save(recipeOutput, AppliedGenerators.id("assembler/origination_processor"));

        // Circuit Slicer Recipes
        // Printed Origination Circuit
        CircuitCutterRecipeBuilder.cut(AGSingletons.ORIGINATION_PRINT, 4)
                .input(AGSingletons.EMBER_BLOCK)
                .save(recipeOutput, AppliedGenerators.id("cutter/printed_origination_processor"));

        // Crystal Fixer Recipes
        // Chipped Budding Ember
        CrystalFixerRecipeBuilder.fixer(AGSingletons.BUDDING_EMBER_DAMAGED, AGSingletons.BUDDING_EMBER_CHIPPED)
                .chance(0.4)
                .fuel(AGSingletons.EMBER_CRYSTAL_CHARGED)
                .save(recipeOutput, AppliedGenerators.id("fixer/chipped_budding_ember"));
        // Flawed Budding Ember
        CrystalFixerRecipeBuilder.fixer(AGSingletons.BUDDING_EMBER_CHIPPED, AGSingletons.BUDDING_EMBER_FLAWED)
                .chance(0.4)
                .fuel(AGSingletons.EMBER_CRYSTAL_CHARGED)
                .save(recipeOutput, AppliedGenerators.id("fixer/flawed_budding_ember"));
        // Flawless Budding Ember
        CrystalFixerRecipeBuilder.fixer(AGSingletons.BUDDING_EMBER_FLAWED, AGSingletons.BUDDING_EMBER_FLAWLESS)
                .chance(0.01)
                .fuel(AGSingletons.EMBER_CRYSTAL_CHARGED)
                .save(recipeOutput, AppliedGenerators.id("fixer/flawless_budding_ember"));

        // Reaction Chamber Recipes
        // Ember Crystal
        ReactionChamberRecipeBuilder.react(AGSingletons.EMBER_CRYSTAL, 64, 200000)
                .input(AEItems.FLUIX_CRYSTAL, 16)
                .input(Items.GUNPOWDER, 16)
                .input(Items.BLAZE_POWDER, 16)
                .fluid(Fluids.LAVA, 500)
                .save(recipeOutput, AppliedGenerators.id("reaction/ember_crystal"));
        // Ember Crystal (Duplicate)
        ReactionChamberRecipeBuilder.react(AGSingletons.EMBER_CRYSTAL, 64, 1000000)
                .input(AEItems.FLUIX_CRYSTAL, 32)
                .input(AGSingletons.EMBER_DUST, 32)
                .fluid(Fluids.LAVA, 500)
                .save(recipeOutput, AppliedGenerators.id("reaction/ember_crystal_duplicate"));
        // Charged Ember Crystal
        ReactionChamberRecipeBuilder.react(AGSingletons.EMBER_CRYSTAL_CHARGED, 64, 1300000)
                .input(AGSingletons.EMBER_CRYSTAL, 64)
                .fluid(Fluids.LAVA, 1000)
                .save(recipeOutput, AppliedGenerators.id("reaction/charged_ember_crystal"));

        // Genesis Synthesizer Recipes
        // Damaged Budding Ember
        GenesisSynthesizerRecipeBuilder.synthesize(AGSingletons.BUDDING_EMBER_DAMAGED, 1, 100000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 2)
                .input(AGSingletons.EMBER_BLOCK)
                .input(AGSingletons.EMBER_CRYSTAL)
                .fluid(Fluids.LAVA, 1000)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/budding_ember_damaged"));
        // Chipped Budding Ember
        GenesisSynthesizerRecipeBuilder.synthesize(AGSingletons.BUDDING_EMBER_CHIPPED, 1, 100000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 2)
                .input(AGSingletons.BUDDING_EMBER_DAMAGED)
                .input(AGSingletons.EMBER_CRYSTAL)
                .fluid(Fluids.LAVA, 1000)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/budding_ember_chipped"));
        // Flawed Budding Ember
        GenesisSynthesizerRecipeBuilder.synthesize(AGSingletons.BUDDING_EMBER_FLAWED, 1, 100000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 2)
                .input(AGSingletons.BUDDING_EMBER_CHIPPED)
                .input(AGSingletons.EMBER_CRYSTAL)
                .fluid(Fluids.LAVA, 1000)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/budding_ember_flawed"));
        // Flawless Budding Ember
        GenesisSynthesizerRecipeBuilder.synthesize(AGSingletons.BUDDING_EMBER_FLAWLESS, 1, 1000000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 100)
                .input(AGSingletons.BUDDING_EMBER_FLAWED)
                .input(AGSingletons.EMBER_CRYSTAL)
                .fluid(Fluids.LAVA, 10000)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/budding_ember_flawless"));
        // Insulating Resin
        GenesisSynthesizerRecipeBuilder.synthesize(AFSingletons.INSULATING_RESIN, 1, 10000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 1)
                .input(AEItems.SKY_DUST)
                .input(AEItems.SILICON)
                .input(Items.SLIME_BALL)
                .fluid(Fluids.WATER, 1000)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/insulating_resin"));
        // Fluix ME Covered Cable
        GenesisSynthesizerRecipeBuilder.synthesize(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT), 16, 10000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 1)
                .input(AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT), 16)
                .input(AEItems.SILICON, 8)
                .fluid(Fluids.WATER, 200)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/covered_cable"));
        // Fluix ME Smart Cable
        GenesisSynthesizerRecipeBuilder.synthesize(AEParts.SMART_CABLE.item(AEColor.TRANSPARENT), 16, 10000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 1)
                .input(AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT), 16)
                .input(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, 8)
                .fluid(Fluids.WATER, 200)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/smart_cable"));
        // Cable Anchor
        GenesisSynthesizerRecipeBuilder.synthesize(AEParts.CABLE_ANCHOR.asItem(), 32, 1000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 1)
                .input(AETags.METAL_INGOTS, 4)
                .input(AEItems.CERTUS_QUARTZ_CRYSTAL, 2)
                .fluid(Fluids.WATER, 100)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/cable_anchor"));
        // Stack Smelting Card
        GenesisSynthesizerRecipeBuilder.synthesize(AGSingletons.STACK_SMELTING_CARD, 1, 500000)
                .input(AGSingletons.EMBER_CRYSTAL_CHARGED, 200)
                .input(AEItems.ADVANCED_CARD)
                .input(Items.NETHERITE_BLOCK)
                .fluid(Fluids.LAVA, 5000)
                .save(recipeOutput, AppliedGenerators.id("synthesizer/stack_smelting_card"));


        // World Transformation Recipes
        // Ember Crystal (Duplicate)
        TransformRecipeBuilder.transform(
                recipeOutput,
                AppliedGenerators.id("transform/ember_crystal_duplicate"),
                AGSingletons.EMBER_CRYSTAL, 1,
                TransformCircumstance.fluid(FluidTags.LAVA),
                Ingredient.of(AEItems.FLUIX_CRYSTAL),
                Ingredient.of(AGSingletons.EMBER_DUST)
        );
        // Ember Crystal
        TransformRecipeBuilder.transform(
                recipeOutput,
                AppliedGenerators.id("transform/ember_crystal"),
                AGSingletons.EMBER_CRYSTAL, 2,
                TransformCircumstance.fluid(FluidTags.LAVA),
                Ingredient.of(AEItems.FLUIX_CRYSTAL),
                Ingredient.of(Items.GUNPOWDER),
                Ingredient.of(Items.BLAZE_POWDER)
        );

        // Mekanism Recipes
        if (GlodUtil.checkMod(ModConstants.MEK)) {
            // Crushing Recipes
            // Ember Dust
            ItemStackToItemStackRecipeBuilder.crushing(ItemStackIngredient.of(SizedIngredient.of(AGSingletons.EMBER_CRYSTAL, 1)), new ItemStack(AGSingletons.EMBER_DUST))
                    .build(recipeOutput.withConditions(new ModLoadedCondition(ModConstants.MEK)), AppliedGenerators.id("mekanism/crushing/ember_dust"));
        }
    }
}
