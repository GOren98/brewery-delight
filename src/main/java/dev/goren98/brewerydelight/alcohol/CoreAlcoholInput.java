package dev.goren98.brewerydelight.alcohol;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Single Base bottle plus the production process that is consuming it. */
public record CoreAlcoholInput(ItemStack base, String process) implements RecipeInput {
    @Override public ItemStack getItem(int index) {
        if (index != 0) throw new IllegalArgumentException("CoreAlcoholInput only has one item");
        return base;
    }

    @Override public int size() { return 1; }
}
