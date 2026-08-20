package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.registry.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BreweryDelightClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModBlocks.CROPS.values().forEach(crop ->
                ItemBlockRenderTypes.setRenderLayer(crop.get(), RenderType.cutout())));
    }

    private BreweryDelightClient() {}
}
