package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.aroma.AromaTooltipEvents;
import dev.goren98.brewerydelight.aroma.AromaDefinitions;
import dev.goren98.brewerydelight.crop.VanillaCropAromaEvents;
import dev.goren98.brewerydelight.registry.ModBlockEntities;
import dev.goren98.brewerydelight.registry.ModBlocks;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModMenus;
import dev.goren98.brewerydelight.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BreweryDelight.MOD_ID)
public final class BreweryDelight {
    public static final String MOD_ID = "brewerydelight";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public BreweryDelight(IEventBus modBus) {
        AromaDefinitions.loadBundled();
        ModComponents.COMPONENTS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.RECIPE_TYPES.register(modBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modBus);
        NeoForge.EVENT_BUS.addListener(AromaTooltipEvents::onTooltip);
        NeoForge.EVENT_BUS.addListener(VanillaCropAromaEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(VanillaCropAromaEvents::onBlockPlaced);
        NeoForge.EVENT_BUS.addListener(VanillaCropAromaEvents::onCropGrow);
        NeoForge.EVENT_BUS.addListener(VanillaCropAromaEvents::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(VanillaCropAromaEvents::onBlockBreak);
    }
}
