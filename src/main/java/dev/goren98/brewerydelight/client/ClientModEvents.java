package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COOKING_POT.get(), CookingPotScreen::new);
    }

    private ClientModEvents() {}
}
