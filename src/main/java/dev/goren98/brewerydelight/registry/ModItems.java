package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.item.TestBrewItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BreweryDelight.MOD_ID);
    public static final Supplier<Item> TEST_BREW = ITEMS.register("test_brew", () -> new TestBrewItem(new Item.Properties()));
    private ModItems() {}
}
