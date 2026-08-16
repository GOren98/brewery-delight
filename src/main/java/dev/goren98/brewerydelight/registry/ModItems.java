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
            () -> new TestBrewItem(new Item.Properties(), "test_brew", 0, "Test Brew", "test", 0, 4));

    public static final Supplier<Item> TEST_SPIRIT = ITEMS.register("test_spirit",
            () -> new TestBrewItem(new Item.Properties(), "test_spirit", 2, "Test Spirit", "test", 5, 5));

    public static final Supplier<Item> NEUTRAL_BASE = ITEMS.register("neutral_base",
            () -> new TestBrewItem(new Item.Properties(), "neutral_base", 0, "Neutral", "", 0, 0));

    public static final Supplier<Item> NEUTRAL_SPIRIT = ITEMS.register("neutral_spirit",
            () -> new TestBrewItem(new Item.Properties(), "neutral_spirit", 2, "Neutral Spirit", "", 0, 0));

    public static final Supplier<Item> TEST_LIQUEUR = ITEMS.register("test_liqueur",
            () -> new TestBrewItem(new Item.Properties(), "test_liqueur", 3, "Test Liqueur", "test", 1, 5));

    private ModItems() {}
}
