package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** One produce -> one seed while preserving the produce stack's current Aroma. */
public final class AromaSeedRecipe extends CustomRecipe {
    public AromaSeedRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findCropId(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        String cropId = findCropId(input);
        if (cropId == null) return ItemStack.EMPTY;

        ItemStack produce = onlyItem(input);
        ItemStack seed = new ItemStack(ModItems.SEEDS.get(cropId).get());
        String aroma = produce.get(ModComponents.CROP_AROMA.get());
        if (aroma != null && !aroma.isBlank()) {
            seed.set(ModComponents.CROP_AROMA.get(), aroma);
        }
        return seed;
    }

    private static String findCropId(CraftingInput input) {
        ItemStack item = onlyItem(input);
        if (item.isEmpty()) return null;
        for (var entry : ModItems.CROP_ITEMS.entrySet()) {
            if (item.is(entry.getValue().get()) && ModItems.SEEDS.containsKey(entry.getKey())) return entry.getKey();
        }
        return null;
    }

    private static ItemStack onlyItem(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (!found.isEmpty()) return ItemStack.EMPTY;
            found = stack;
        }
        return found;
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.AROMA_SEED_SERIALIZER.get(); }
}
