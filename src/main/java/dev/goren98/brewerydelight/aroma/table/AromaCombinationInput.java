package dev.goren98.brewerydelight.aroma.table;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record AromaCombinationInput(ItemStack donor, ItemStack receiver) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> donor;
            case 1 -> receiver;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
