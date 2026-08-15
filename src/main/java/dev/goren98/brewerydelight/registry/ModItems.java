package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.item.TestBrewItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BreweryDelight.MOD_ID);

    public static final Supplier<Item> TEST_BREW = ITEMS.register("test_brew",
            () -> new TestBrewItem(new Item.Properties(), "test_brew", 0, "Test Brew", "test", true));

    // Distillation mechanics come in the next MVP. This finished spirit exists now so
    // the longer spirit-aging path and aroma rules can be tested independently.
    public static final Supplier<Item> TEST_SPIRIT = ITEMS.register("test_spirit",
            () -> new TestBrewItem(new Item.Properties(), "test_spirit", 2, "Test Spirit", "test", false));

    private ModItems() {}
}
