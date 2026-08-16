package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.item.TestBrewItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BreweryDelight.MOD_ID);

    // Stable MVP/test items kept untouched for regression testing.
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

    // Generic bottles used by future content. Product identity, display name, aroma and
    // distillation target are supplied by item-stack data components instead of Java constants.
    public static final Supplier<Item> BREW_BOTTLE = ITEMS.register("brew_bottle",
            () -> new TestBrewItem(new Item.Properties(), "brew", 0, "Brew", "", 0, 4));
    public static final Supplier<Item> SPIRIT_BOTTLE = ITEMS.register("spirit_bottle",
            () -> new TestBrewItem(new Item.Properties(), "spirit", 2, "Spirit", "", 0, 0));
    public static final Supplier<Item> LIQUEUR_BOTTLE = ITEMS.register("liqueur_bottle",
            () -> new TestBrewItem(new Item.Properties(), "liqueur", 3, "Liqueur", "", 1, 5));

    private ModItems() {}
}
