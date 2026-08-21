package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.crop.AromaProduceItem;
import dev.goren98.brewerydelight.crop.AromaSeedItem;
import dev.goren98.brewerydelight.item.TestBrewItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BreweryDelight.MOD_ID);
    public static final Map<String, Supplier<Item>> CROP_ITEMS = new LinkedHashMap<>();
    public static final Map<String, Supplier<Item>> SEEDS = new LinkedHashMap<>();

    private static void registerCrop(String id) {
        Supplier<Item> produce = ITEMS.register(id, () -> new AromaProduceItem(new Item.Properties(), id));
        Supplier<Item> seed = ITEMS.register(id + "_seeds", () -> new AromaSeedItem(ModBlocks.PLANTS.get(id).get(), new Item.Properties(), id));
        CROP_ITEMS.put(id, produce); SEEDS.put(id, seed);
    }
    static { ModBlocks.PLANTS.keySet().forEach(ModItems::registerCrop); }

    public static final Supplier<Item> COOKING_POT_ITEM = ITEMS.register("cooking_pot", () -> new BlockItem(ModBlocks.COOKING_POT.get(), new Item.Properties()));
    public static final Supplier<Item> TEST_BREW = ITEMS.register("test_brew", () -> new TestBrewItem(new Item.Properties(), "test_brew", 0, "Test Brew", "test", 0, 4));
    public static final Supplier<Item> TEST_SPIRIT = ITEMS.register("test_spirit", () -> new TestBrewItem(new Item.Properties(), "test_spirit", 2, "Test Spirit", "test", 5, 5));
    public static final Supplier<Item> NEUTRAL_BASE = ITEMS.register("neutral_base", () -> new TestBrewItem(new Item.Properties(), "neutral_base", 0, "Neutral", "", 0, 0));
    public static final Supplier<Item> NEUTRAL_SPIRIT = ITEMS.register("neutral_spirit", () -> new TestBrewItem(new Item.Properties(), "neutral_spirit", 2, "Neutral Spirit", "", 0, 0));
    public static final Supplier<Item> TEST_LIQUEUR = ITEMS.register("test_liqueur", () -> new TestBrewItem(new Item.Properties(), "test_liqueur", 3, "Test Liqueur", "test", 1, 5));
    public static final Supplier<Item> BREW_BOTTLE = ITEMS.register("brew_bottle", () -> new TestBrewItem(new Item.Properties(), "brew", 0, "Brew", "", 0, 4));
    public static final Supplier<Item> SPIRIT_BOTTLE = ITEMS.register("spirit_bottle", () -> new TestBrewItem(new Item.Properties(), "spirit", 2, "Spirit", "", 0, 0));
    public static final Supplier<Item> LIQUEUR_BOTTLE = ITEMS.register("liqueur_bottle", () -> new TestBrewItem(new Item.Properties(), "liqueur", 3, "Liqueur", "", 1, 5));
    private ModItems() {}
}
