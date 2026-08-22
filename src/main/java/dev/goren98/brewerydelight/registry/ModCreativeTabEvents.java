package dev.goren98.brewerydelight.registry;

import dev.goren98.brewerydelight.BreweryDelight;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabEvents {
    @SubscribeEvent
    public static void buildTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            ModItems.CROP_ITEMS.values().forEach(item -> event.accept(item.get()));
            ModItems.SEEDS.values().forEach(item -> event.accept(item.get()));
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.COOKING_POT_ITEM.get());
            event.accept(ModItems.BREWING_STATION_ITEM.get());
            event.accept(ModItems.AROMA_TABLE_ITEM.get());
        }
    }

    private ModCreativeTabEvents() {}
}
