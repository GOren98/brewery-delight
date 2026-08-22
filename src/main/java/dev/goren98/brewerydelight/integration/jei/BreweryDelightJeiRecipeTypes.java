package dev.goren98.brewerydelight.integration.jei;

import dev.goren98.brewerydelight.cooking.recipe.BaseCookingRecipe;
import dev.goren98.brewerydelight.registry.ModRecipes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class BreweryDelightJeiRecipeTypes {
    public static final RecipeType<RecipeHolder<BaseCookingRecipe>> BASE_COOKING =
            RecipeType.createFromVanilla(ModRecipes.BASE_COOKING_TYPE.get());

    private BreweryDelightJeiRecipeTypes() {}
}
