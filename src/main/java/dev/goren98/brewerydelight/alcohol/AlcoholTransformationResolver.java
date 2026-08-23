package dev.goren98.brewerydelight.alcohol;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.Comparator;

/** Shared transformation entry point for Core creation, completed Aging and Blending. */
public final class AlcoholTransformationResolver {
    private static final Comparator<RecipeHolder<AlcoholTransformationRecipe>> ORDER =
            Comparator.<RecipeHolder<AlcoholTransformationRecipe>>comparingInt(holder -> holder.value().priority())
                    .thenComparing(holder -> holder.id().toString());

    public static boolean resolve(Level level, ItemStack stack) {
        if (level.isClientSide || stack.isEmpty()
                || stack.getOrDefault(ModComponents.CORE_ALCOHOL_ID.get(), "").isEmpty()) return false;

        SingleRecipeInput input = new SingleRecipeInput(stack);
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.ALCOHOL_TRANSFORMATION_TYPE.get()).stream()
                .filter(holder -> holder.value().matches(input, level))
                .max(ORDER)
                .map(holder -> apply(holder.value(), stack))
                .orElse(false);
    }

    private static boolean apply(AlcoholTransformationRecipe recipe, ItemStack stack) {
        String before = stack.getOrDefault(ModComponents.PRODUCT_ID.get(), "");
        recipe.applyTo(stack);
        return !before.equals(stack.getOrDefault(ModComponents.PRODUCT_ID.get(), ""));
    }

    private AlcoholTransformationResolver() {}
}
