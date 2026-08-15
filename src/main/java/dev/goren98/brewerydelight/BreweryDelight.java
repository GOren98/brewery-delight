package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(BreweryDelight.MOD_ID)
public final class BreweryDelight {
    public static final String MOD_ID = "brewerydelight";

    public BreweryDelight(IEventBus modBus) {
        ModComponents.COMPONENTS.register(modBus);
        ModItems.ITEMS.register(modBus);
    }
}
