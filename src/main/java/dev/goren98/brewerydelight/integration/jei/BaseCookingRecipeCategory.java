package dev.goren98.brewerydelight.integration.jei;

import dev.goren98.brewerydelight.cooking.recipe.BaseCookingRecipe;
import dev.goren98.brewerydelight.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class BaseCookingRecipeCategory implements IRecipeCategory<RecipeHolder<BaseCookingRecipe>> {
    private static final int WIDTH = 126;
    private static final int HEIGHT = 54;

    private final IDrawable icon;

    public BaseCookingRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModItems.COOKING_POT_ITEM.get());
    }

    @Override
    public RecipeType<RecipeHolder<BaseCookingRecipe>> getRecipeType() {
        return BreweryDelightJeiRecipeTypes.BASE_COOKING;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Brewing Pot - Alcohol Base");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<BaseCookingRecipe> holder, IFocusGroup focuses) {
        BaseCookingRecipe recipe = holder.value();
        int index = 0;
        for (Ingredient ingredient : recipe.ingredients()) {
            int x = 1 + (index % 3) * 18;
            int y = 1 + (index / 3) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredients(ingredient);
            index++;
        }

        // Brewing Pot batches must be served into bottles before they can be removed.
        builder.addSlot(RecipeIngredientRole.INPUT, 64, 19).addItemStack(new ItemStack(Items.GLASS_BOTTLE));

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            ItemStack output = recipe.getResultItem(minecraft.level.registryAccess());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 19).addItemStack(output);
        } else {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 19).addItemStack(new ItemStack(ModItems.BASE_BOTTLE.get()));
        }
    }
}
