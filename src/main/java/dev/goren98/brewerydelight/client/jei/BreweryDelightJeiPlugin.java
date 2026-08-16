package dev.goren98.brewerydelight.client.jei;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Minimal JEI integration for the data-driven Base bottles.
 *
 * Brewery Delight intentionally uses one generic brew_bottle item with data components for all products.
 * JEI therefore needs to be told that stage-0 product ids are distinct subtypes, and it needs representative
 * stacks for those variants in its ingredient list. Farmer's Delight already displays the actual cooking
 * recipes, so this plugin does not register or replace any recipe logic.
 */
@JeiPlugin
public final class BreweryDelightJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(BreweryDelight.MOD_ID, "base_recipes");

    private static final List<BaseEntry> BASES = List.of(
            new BaseEntry("red_wine", "Red Wine"),
            new BaseEntry("white_wine", "White Wine"),
            new BaseEntry("cider", "Cider"),
            new BaseEntry("perry", "Perry"),
            new BaseEntry("apricot_wine", "Apricot Wine"),
            new BaseEntry("cherry_wine", "Cherry Wine"),
            new BaseEntry("fig_wine", "Fig Wine"),
            new BaseEntry("kiwi_wine", "Kiwi Wine"),
            new BaseEntry("peach_wine", "Peach Wine"),
            new BaseEntry("persimmon_wine", "Persimmon Wine"),
            new BaseEntry("pineapple_wine", "Pineapple Wine"),
            new BaseEntry("plum_wine", "Plum Wine"),
            new BaseEntry("pomegranate_wine", "Pomegranate Wine"),
            new BaseEntry("blackberry_wine", "Blackberry Wine"),
            new BaseEntry("blueberry_wine", "Blueberry Wine"),
            new BaseEntry("cranberry_wine", "Cranberry Wine"),
            new BaseEntry("raspberry_wine", "Raspberry Wine"),
            new BaseEntry("strawberry_wine", "Strawberry Wine"),

            new BaseEntry("ale", "Ale"),
            new BaseEntry("lager", "Lager"),

            new BaseEntry("malt_whiskey", "Malt Whiskey"),
            new BaseEntry("corn_whiskey", "Corn Whiskey"),
            new BaseEntry("rye_whiskey", "Rye Whiskey"),

            new BaseEntry("takju", "Takju"),
            new BaseEntry("cheongju", "Cheongju"),
            new BaseEntry("sake", "Sake"),
            new BaseEntry("huangjiu", "Huangjiu"),
            new BaseEntry("shochu", "Shochu"),

            new BaseEntry("mead", "Mead"),
            new BaseEntry("airag", "Airag"),
            new BaseEntry("pulque", "Pulque"),
            new BaseEntry("vodka", "Vodka"),
            new BaseEntry("rum", "Rum"),
            new BaseEntry("arrack", "Arrack")
    );

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.BREW_BOTTLE.get(), new ISubtypeInterpreter<>() {
            @Override
            public Object getSubtypeData(ItemStack stack, UidContext context) {
                String product = stack.getOrDefault(ModComponents.PRODUCT_ID.get(), "");
                int stage = stack.getOrDefault(ModComponents.STAGE.get(), 0);
                return product.isEmpty() ? null : product + "|" + stage;
            }

            @Override
            public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                String product = stack.getOrDefault(ModComponents.PRODUCT_ID.get(), "");
                int stage = stack.getOrDefault(ModComponents.STAGE.get(), 0);
                return product.isEmpty() ? "" : product + "|" + stage;
            }
        });
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> bases = BASES.stream().map(BreweryDelightJeiPlugin::makeBaseStack).toList();
        registration.addExtraItemStacks(bases);
    }

    private static ItemStack makeBaseStack(BaseEntry entry) {
        ItemStack stack = new ItemStack(ModItems.BREW_BOTTLE.get());
        stack.set(ModComponents.PRODUCT_ID.get(), entry.productId());
        stack.set(ModComponents.DISPLAY_NAME.get(), entry.displayName());
        stack.set(ModComponents.STAGE.get(), 0);
        stack.set(ModComponents.AGE.get(), 0);
        return stack;
    }

    private record BaseEntry(String productId, String displayName) {}
}
