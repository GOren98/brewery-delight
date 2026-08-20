package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.crop.AromaCraftingEvents;
import dev.goren98.brewerydelight.registry.ModBlockEntities;
import dev.goren98.brewerydelight.registry.ModBlocks;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(BreweryDelight.MOD_ID)
public final class BreweryDelight {
    public static final String MOD_ID = "brewerydelight";

    public BreweryDelight(IEventBus modBus) {
        ModComponents.COMPONENTS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        NeoForge.EVENT_BUS.addListener(AromaCraftingEvents::onItemCrafted);
    }
}
