package dev.goren98.brewerydelight.integration.jei;

import dev.goren98.brewerydelight.cooking.recipe.LiqueurCookingRecipe;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/** JEI view of the Brewing Pot's alcohol + four materials -> Liqueur Core recipes. */
public final class LiqueurCookingRecipeCategory implements IRecipeCategory<RecipeHolder<LiqueurCookingRecipe>> {
    private static final int WIDTH = 126;
    private static final int HEIGHT = 54;
    private final IDrawable icon;

    public LiqueurCookingRecipeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableItemLike(ModItems.LIQUEUR_BOTTLE.get());
    }

    @Override public RecipeType<RecipeHolder<LiqueurCookingRecipe>> getRecipeType() {
        return BreweryDelightJeiRecipeTypes.LIQUEUR_COOKING;
    }
    @Override public Component getTitle() { return Component.literal("Brewing Pot - Liqueur"); }
    @Override public IDrawable getIcon() { return icon; }
    @Override public int getWidth() { return WIDTH; }
    @Override public int getHeight() { return HEIGHT; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<LiqueurCookingRecipe> holder, IFocusGroup focuses) {
        LiqueurCookingRecipe recipe = holder.value();

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19)
                .addItemStacks(alcoholInputs(recipe));

        int index = 0;
        for (Ingredient ingredient : recipe.ingredients()) {
            int x = 28 + (index % 2) * 18;
            int y = 10 + (index / 2) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y).addIngredients(ingredient);
            index++;
        }

        ItemStack output = new ItemStack(ModItems.LIQUEUR_BOTTLE.get());
        output.set(ModComponents.CORE_ALCOHOL_ID.get(), recipe.coreAlcoholId());
        output.set(ModComponents.PRODUCT_ID.get(), recipe.coreAlcoholId());
        output.set(ModComponents.DISPLAY_NAME.get(), recipe.displayName());
        output.set(ModComponents.STAGE.get(), 3);
        output.set(ModComponents.AGE.get(), 0);
        output.set(ModComponents.COLOR.get(), recipe.color());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 103, 19).addItemStack(output);
    }

    private static List<ItemStack> alcoholInputs(LiqueurCookingRecipe recipe) {
        List<ItemStack> inputs = new ArrayList<>();
        if (!"spirit".equals(recipe.requiredAlcoholType())) inputs.add(alcohol(ModItems.BREW_BOTTLE.get(), 1, recipe.requiredCore()));
        if (!"brew".equals(recipe.requiredAlcoholType())) inputs.add(alcohol(ModItems.SPIRIT_BOTTLE.get(), 2, recipe.requiredCore()));
        return inputs;
    }

    private static ItemStack alcohol(net.minecraft.world.item.Item item, int stage, String core) {
        ItemStack stack = new ItemStack(item);
        stack.set(ModComponents.STAGE.get(), stage);
        stack.set(ModComponents.AGE.get(), 0);
        stack.set(ModComponents.BARREL_LEVEL.get(), 0);
        if (!core.isEmpty()) {
            stack.set(ModComponents.CORE_ALCOHOL_ID.get(), core);
            stack.set(ModComponents.PRODUCT_ID.get(), core);
        }
        return stack;
    }
}
