package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.crop.AromaProduceItem;
import dev.goren98.brewerydelight.crop.AromaSeedItem;
import dev.goren98.brewerydelight.item.BaseBottleItem;
import dev.goren98.brewerydelight.item.AlcoholBottleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BreweryDelight.MOD_ID);
    public static final Map<String, Supplier<Item>> CROP_ITEMS = new LinkedHashMap<>();
    public static final Map<String, Supplier<Item>> SEEDS = new LinkedHashMap<>();

    private static void registerCrop(String id) {
        String defaultAroma = id.equals("red_grape") || id.equals("white_grape") ? "grape" : id;
        Supplier<Item> produce = ITEMS.register(id, () -> new AromaProduceItem(new Item.Properties(), defaultAroma));
        Supplier<Item> seed = ITEMS.register(id + "_seeds", () -> new AromaSeedItem(ModBlocks.PLANTS.get(id).get(), new Item.Properties(), defaultAroma));
        CROP_ITEMS.put(id, produce); SEEDS.put(id, seed);
    }
    static { ModBlocks.PLANTS.keySet().forEach(ModItems::registerCrop); }

    public static final Supplier<Item> COOKING_POT_ITEM = ITEMS.register("cooking_pot", () -> new BlockItem(ModBlocks.COOKING_POT.get(), new Item.Properties()) {
        @Override public Component getName(ItemStack stack) { return Component.literal("Brewing Pot"); }
    });
    public static final Supplier<Item> BREWING_STATION_ITEM = ITEMS.register("brewing_station", () -> new BlockItem(ModBlocks.BREWING_STATION.get(), new Item.Properties()));
    public static final Supplier<Item> AROMA_TABLE_ITEM = ITEMS.register("aroma_table", () -> new BlockItem(ModBlocks.AROMA_TABLE.get(), new Item.Properties()));

    public static final Supplier<Item> BASE_BOTTLE = ITEMS.register("base_bottle", () -> new BaseBottleItem(new Item.Properties()));

    public static final Supplier<Item> BREW_BOTTLE = ITEMS.register("brew_bottle", () -> new AlcoholBottleItem(new Item.Properties(), 1, "Brew"));
    public static final Supplier<Item> SPIRIT_BOTTLE = ITEMS.register("spirit_bottle", () -> new AlcoholBottleItem(new Item.Properties(), 2, "Spirit"));
    public static final Supplier<Item> LIQUEUR_BOTTLE = ITEMS.register("liqueur_bottle", () -> new AlcoholBottleItem(new Item.Properties(), 3, "Liqueur"));
    private ModItems() {}
}
