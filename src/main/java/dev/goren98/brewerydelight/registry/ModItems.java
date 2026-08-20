package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.crop.AromaProduceItem;
import dev.goren98.brewerydelight.crop.AromaSeedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BreweryDelight.MOD_ID);

    public static final Map<String, DeferredItem<AromaProduceItem>> PRODUCE = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<AromaSeedItem>> SEEDS = new LinkedHashMap<>();

    public static final DeferredItem<BlockItem> COOKING_POT = ITEMS.registerSimpleBlockItem("cooking_pot", ModBlocks.COOKING_POT);

    public static final DeferredItem<Item> NEUTRAL_BASE = ITEMS.registerSimpleItem("neutral_base");
    public static final DeferredItem<Item> NEUTRAL_SPIRIT = ITEMS.registerSimpleItem("neutral_spirit");
    public static final DeferredItem<Item> TEST_BREW = ITEMS.registerSimpleItem("test_brew");
    public static final DeferredItem<Item> TEST_SPIRIT = ITEMS.registerSimpleItem("test_spirit");
    public static final DeferredItem<Item> TEST_LIQUEUR = ITEMS.registerSimpleItem("test_liqueur");
    public static final DeferredItem<Item> BREW_BOTTLE = ITEMS.registerSimpleItem("brew_bottle");
    public static final DeferredItem<Item> SPIRIT_BOTTLE = ITEMS.registerSimpleItem("spirit_bottle");
    public static final DeferredItem<Item> LIQUEUR_BOTTLE = ITEMS.registerSimpleItem("liqueur_bottle");

    private ModItems() {}

    public static void registerCropItems() {
        for (String crop : ModBlocks.CROP_NAMES) {
            PRODUCE.put(crop, ITEMS.register(crop, () -> new AromaProduceItem(new Item.Properties())));
            SEEDS.put(crop, ITEMS.register(crop + "_seeds", () -> new AromaSeedItem(ModBlocks.CROPS.get(crop).get(), new Item.Properties())));
        }
    }
}
