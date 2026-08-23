package dev.goren98.brewerydelight.integration.jei;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class BreweryDelightJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        ISubtypeInterpreter<ItemStack> interpreter = new ISubtypeInterpreter<>() {
            @Override public Object getSubtypeData(ItemStack stack, UidContext context) {
                String product = stack.getOrDefault(ModComponents.PRODUCT_ID.get(), "");
                int stage = stack.getOrDefault(ModComponents.STAGE.get(), 0);
                return product.isEmpty() ? null : product + "|" + stage;
            }
            @Override public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                Object subtype = getSubtypeData(stack, context);
                return subtype == null ? "" : subtype.toString();
            }
        };
        registration.registerSubtypeInterpreter(ModItems.BASE_BOTTLE.get(), interpreter);
        registration.registerSubtypeInterpreter(ModItems.BREW_BOTTLE.get(), interpreter);
        registration.registerSubtypeInterpreter(ModItems.SPIRIT_BOTTLE.get(), interpreter);
        registration.registerSubtypeInterpreter(ModItems.LIQUEUR_BOTTLE.get(), interpreter);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new BaseCookingRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new LiqueurCookingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        var recipes = minecraft.level.getRecipeManager().getAllRecipesFor(ModRecipes.BASE_COOKING_TYPE.get());
        registration.addRecipes(BreweryDelightJeiRecipeTypes.BASE_COOKING, recipes);
        var liqueurs = minecraft.level.getRecipeManager().getAllRecipesFor(ModRecipes.LIQUEUR_COOKING_TYPE.get());
        registration.addRecipes(BreweryDelightJeiRecipeTypes.LIQUEUR_COOKING, liqueurs);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(ModItems.COOKING_POT_ITEM.get(), BreweryDelightJeiRecipeTypes.BASE_COOKING);
        registration.addRecipeCatalyst(ModItems.COOKING_POT_ITEM.get(), BreweryDelightJeiRecipeTypes.LIQUEUR_COOKING);
    }
}
