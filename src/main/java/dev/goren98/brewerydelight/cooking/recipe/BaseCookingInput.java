package dev.goren98.brewerydelight.cooking.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record BaseCookingInput(List<ItemStack> stacks) implements RecipeInput {
    public BaseCookingInput {
        stacks = List.copyOf(stacks);
    }

    @Override
    public ItemStack getItem(int index) {
        return stacks.get(index);
    }

    @Override
    public int size() {
        return stacks.size();
    }
}
