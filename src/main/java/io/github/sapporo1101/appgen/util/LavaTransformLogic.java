package io.github.sapporo1101.appgen.util;

import appeng.crafting.RecipeAccess;
import appeng.recipes.AERecipeTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.HashSet;
import java.util.Set;

public final class LavaTransformLogic {
    private static final Set<Item> lavaCache = new HashSet<>();

    static {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent _) -> lavaCache.clear());
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) lavaCache.clear();
        });
    }

    public static boolean canTransformInLava(ItemEntity entity) {
        return getLavaTransformableItems(entity.level())
                .contains(entity.getItem().getItem());
    }

    public static boolean allIngredientsPresent(ItemEntity entity) {
        var x = entity.getX();
        var y = entity.getY();
        var z = entity.getZ();
        var level = entity.level();

        var items = level.getEntities(null, new AABB(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1)).stream()
                .filter(e -> e instanceof ItemEntity && !e.isRemoved())
                .map(e -> ((ItemEntity) e).getItem().getItem())
                .toList();

        for (var recipe : RecipeAccess.byType(level, AERecipeTypes.TRANSFORM)) {
            if (recipe.value().circumstance.isFluidTag(FluidTags.LAVA)) {
                return recipe.value().ingredients().stream().noneMatch(ingredient -> {
                    for (var stack : ingredient.getValues()) {
                        if (items.contains(stack.value())) return false;
                    }

                    return true;
                });
            }
        }

        return false;
    }

    @SuppressWarnings("SameReturnValue")
    private static Set<Item> getLavaTransformableItems(Level level) {
        if (lavaCache.isEmpty()) {
            for (var recipe : RecipeAccess.byType(level, AERecipeTypes.TRANSFORM)) {
                if (!recipe.value().circumstance.isFluidTag(FluidTags.LAVA)) continue;

                lavaCache.add(recipe.value().output.item().value());

                for (var ingredient : recipe.value().ingredients) {
                    for (var stack : ingredient.getValues()) lavaCache.add(stack.value());

                    // Don't break here unlike AE2's TransformLogic, otherwise unprocessed items will burn up
                }
            }
        }

        return lavaCache;
    }
}