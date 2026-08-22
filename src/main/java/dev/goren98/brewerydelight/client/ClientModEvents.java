package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.registry.ModBlocks;
import dev.goren98.brewerydelight.registry.ModMenus;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COOKING_POT.get(), CookingPotScreen::new);
        event.register(ModMenus.BREWING_STATION.get(), BrewingStationScreen::new);
        event.register(ModMenus.AROMA_TABLE.get(), AromaTableScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        Block[] leaves = ModBlocks.CROPTOPIA_LEAF_BLOCKS.stream().map(java.util.function.Supplier::get).toArray(Block[]::new);
        event.register((state, level, pos, tintIndex) -> level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor(), leaves);
    }
    private ClientModEvents() {}
}
