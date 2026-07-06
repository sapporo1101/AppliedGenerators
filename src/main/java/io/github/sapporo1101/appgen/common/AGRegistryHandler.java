package io.github.sapporo1101.appgen.common;

import appeng.api.AECapabilities;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.upgrades.Upgrades;
import appeng.block.AEBaseBlockItem;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.blockentity.ClientTickingBlockEntity;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.powersink.AEBasePoweredBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.items.AEBaseItem;
import appeng.items.parts.PartItem;
import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import com.glodblock.github.glodium.registry.RegistryHandler;
import com.glodblock.github.glodium.registry.token.TileToken;
import io.github.sapporo1101.appgen.AppliedGenerators;
import io.github.sapporo1101.appgen.common.blockentities.FluxCellBaseBlockEntity;
import io.github.sapporo1101.appgen.common.blockentities.GenesisSynthesizerBlockEntity;
import io.github.sapporo1101.appgen.common.blockentities.PatternBufferBlockEntity;
import io.github.sapporo1101.appgen.menu.*;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipe;
import io.github.sapporo1101.appgen.recipe.GenesisSynthesizerRecipeSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AGRegistryHandler extends RegistryHandler {
    public static AGRegistryHandler INSTANCE;

    @SuppressWarnings("UnstableApiUsage")
    public AGRegistryHandler(IEventBus modBus) {
        super(AppliedGenerators.MODID, modBus);

        modBus.addListener(this::onRegisterEvent);

        this.cap(AEBaseInvBlockEntity.class, Capabilities.Item.BLOCK, AEBaseInvBlockEntity::getExposedItemHandler);
        this.cap(AEBasePoweredBlockEntity.class, Capabilities.Energy.BLOCK, AEBasePoweredBlockEntity::getEnergyStorage);
        this.cap(IInWorldGridNodeHost.class, AECapabilities.IN_WORLD_GRID_NODE_HOST, (nodeHost, _) -> nodeHost);
        this.cap(IAEItemPowerStorage.class, Capabilities.Energy.ITEM, (itemStack, itemAccess) -> new PoweredItemCapabilities(itemAccess, itemStack.getItem(), (IAEItemPowerStorage) itemStack.getItem()));
        this.cap(ICraftingMachine.class, AECapabilities.CRAFTING_MACHINE, (craftingMachine, _) -> craftingMachine);
        this.cap(FluxCellBaseBlockEntity.class, Capabilities.Energy.BLOCK, FluxCellBaseBlockEntity::getEnergyStorage);
        this.cap(GenesisSynthesizerBlockEntity.class, AECapabilities.GENERIC_INTERNAL_INV, (be, _) -> be.getTank());
        this.cap(PatternBufferBlockEntity.class, AECapabilities.GENERIC_INTERNAL_INV, (be, _) -> be.getStorageInv());
    }

    public <T extends AEBaseBlockEntity, B extends AEBaseEntityBlock<T>> DeferredBlock<@NotNull B> block(String name, Function<BlockBehaviour.Properties, B> builder, Class<T> clazz, AGRegistryHandler.TileFactory<@NotNull T> supplier, BiFunction<Block, Item.Properties, Item> itemBuilder) {
        DeferredBlock<@NotNull B> aeBlock = this.block(name, builder, BlockBehaviour.Properties.of(), itemBuilder);
        this.tile(name, clazz, supplier, aeBlock);
        return aeBlock;
    }

    public <T extends AEBaseBlockEntity, B extends AEBaseEntityBlock<T>> DeferredBlock<@NotNull B> block(String name, Function<BlockBehaviour.Properties, B> builder, Class<T> clazz, AGRegistryHandler.TileFactory<@NotNull T> supplier) {
        DeferredBlock<@NotNull B> aeBlock = this.block(name, builder, BlockBehaviour.Properties.of(), AEBaseBlockItem::new);
        this.tile(name, clazz, supplier, aeBlock);
        return aeBlock;
    }

    public <P extends IPart> DeferredItem<@NotNull PartItem<P>> item(String name, Class<P> partClass, Function<IPartItem<P>, P> factory) {
        return this.item(name, (properties) -> new PartItem<>(properties, partClass, factory));
    }

    public <T extends AEBaseBlockEntity> void tile(String name, Class<T> tileClass, AGRegistryHandler.TileFactory<@NotNull T> factory, DeferredBlock<? extends @NotNull AEBaseEntityBlock<T>> block) {
        this.tiles.register(name, () -> {
            AtomicReference<BlockEntityType<@NotNull T>> holder = new AtomicReference<>();
            BlockEntityType<@NotNull T> tileType = new BlockEntityType<>((pos, state) -> factory.create(holder.get(), pos, state), Set.of(block.get()));
            holder.set(tileType);
            BlockEntityTicker<@NotNull T> serverTicker = null;
            if (ServerTickingBlockEntity.class.isAssignableFrom(tileClass)) {
                serverTicker = (_, _, _, entity) -> ((ServerTickingBlockEntity) entity).serverTick();
            }

            BlockEntityTicker<@NotNull T> clientTicker = null;
            if (ClientTickingBlockEntity.class.isAssignableFrom(tileClass)) {
                clientTicker = (_, _, _, entity) -> ((ClientTickingBlockEntity) entity).clientTick();
            }

            block.get().setBlockEntity(tileClass, tileType, clientTicker, serverTicker);
            Objects.requireNonNull(holder);
            TileToken token = new TileToken(holder::get, tileClass);
            this.tileTypes.add(token);
            this.tileBind.add(Pair.of(token, Set.of(block.get())));
            return tileType;
        });
    }

    public Collection<Block> getBlocks() {
        return this.blocks.getEntries().stream().map(DeferredHolder::get).map(b -> (Block) b).toList();
    }

    private void onRegisterEvent(RegisterEvent e) {
        if (e.getRegistry().equals(BuiltInRegistries.MENU)) {
            this.onRegisterContainer();
        } else if (e.getRegistry().equals(BuiltInRegistries.RECIPE_TYPE)) {
            this.onRegisterRecipeType();
        } else if (e.getRegistry().equals(BuiltInRegistries.RECIPE_SERIALIZER)) {
            this.onRegisterRecipeSerializer();
        }
    }

    private void onRegisterRecipeType() {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, GenesisSynthesizerRecipe.ID, GenesisSynthesizerRecipe.TYPE);
    }

    private void onRegisterRecipeSerializer() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, GenesisSynthesizerRecipe.ID, GenesisSynthesizerRecipeSerializer.INSTANCE);
    }

    private void onRegisterContainer() {
        Registry.register(BuiltInRegistries.MENU, AppliedGenerators.id("flux_cell"), FluxCellMenu.TYPE);
        Registry.register(BuiltInRegistries.MENU, AppliedGenerators.id("pattern_buffer"), PatternBufferMenu.TYPE);
        Registry.register(BuiltInRegistries.MENU, AppliedGenerators.id("genesis_synthesizer"), GenesisSynthesizerMenu.TYPE);
        Registry.register(BuiltInRegistries.MENU, AppliedGenerators.id("singularity_generator"), SingularityGeneratorMenu.TYPE);
        Registry.register(BuiltInRegistries.MENU, AppliedGenerators.id("flux_generator"), FluxGeneratorMenu.TYPE);
        Registry.register(BuiltInRegistries.MENU, AppliedGenerators.id("smelter"), SmelterMenu.TYPE);
    }

    public void registerTab(Registry<CreativeModeTab> registry) {
        var tab = CreativeModeTab.builder()
                .icon(() -> new ItemStack(AGSingletons.SINGULARITY_GENERATOR_1K))
                .title(Component.translatable("itemGroup." + AppliedGenerators.MODID))
                .displayItems((p, o) -> {
                    for (DeferredHolder<Item, ? extends Item> entry : this.items.getEntries()) {
                        if (entry.value() instanceof AEBaseItem aeItem) {
                            aeItem.addToMainCreativeTab(p, o);
                        } else {
                            o.accept(entry.value());
                        }
                    }
                    for (DeferredHolder<Block, ? extends Block> entry : this.blocks.getEntries()) {
                        o.accept(entry.value());
                    }
                })
                .build();
        Registry.register(registry, AppliedGenerators.id("tab_main"), tab);
    }

    public void init() {
        for (DeferredHolder<Block, ? extends Block> entry : this.blocks.getEntries()) {
            Block block = entry.value();
            if (block instanceof AEBaseEntityBlock<?>) {
                AEBaseBlockEntity.registerBlockEntityItem(
                        ((AEBaseEntityBlock<?>) block).getBlockEntityType(),
                        block.asItem()
                );
            }
        }
        this.registerAEUpgrade();
    }

    public void registerAEUpgrade() {
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.GENESIS_SYNTHESIZER, 4);
        Upgrades.add(AEItems.CAPACITY_CARD, AGSingletons.PATTERN_BUFFER, 4);
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.PATTERN_BUFFER, 1);
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_1K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_1K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_4K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_4K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_16K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_16K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_64K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_64K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_256K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_256K, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_1M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_1M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_4M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_4M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_16M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_16M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_64M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_64M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SINGULARITY_GENERATOR_256M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.ENERGY_CARD, AGSingletons.SINGULARITY_GENERATOR_256M, 3, "upgrade.appgen.singularity_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_1K, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_1K, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_4K, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_4K, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_16K, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_16K, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_64K, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_64K, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_256K, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_256K, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_1M, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_1M, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_4M, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_4M, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_16M, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_16M, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_64M, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_64M, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.FLUX_GENERATOR_256M, 3, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.REDSTONE_CARD, AGSingletons.FLUX_GENERATOR_256M, 1, "upgrade.appgen.flux_generator");
        Upgrades.add(AEItems.SPEED_CARD, AGSingletons.SMELTER, 4);
        Upgrades.add(AGSingletons.STACK_SMELTING_CARD, AGSingletons.SMELTER, 1);
    }

    public interface TileFactory<T extends BlockEntity> {
        T create(BlockEntityType<@NotNull T> type, BlockPos worldPosition, BlockState blockState);
    }
}
