package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BreweryDelight.MOD_ID)
public final class BreweryDelight {
    public static final String MOD_ID = "brewerydelight";

    public BreweryDelight(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();
        ModComponents.COMPONENTS.register(bus);
        ModItems.ITEMS.register(bus);
    }
}
