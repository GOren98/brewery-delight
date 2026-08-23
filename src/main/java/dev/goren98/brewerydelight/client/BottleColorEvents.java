package dev.goren98.brewerydelight.client;

import dev.goren98.brewerydelight.BreweryDelight;
import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BottleColorEvents {
    private static final int WHITE = 0xFFFFFFFF;

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(BottleColorEvents::color,
                ModItems.BREW_BOTTLE.get(),
                ModItems.SPIRIT_BOTTLE.get(),
                ModItems.LIQUEUR_BOTTLE.get());
    }

    private static int color(ItemStack stack, int tintIndex) {
        // layer0 is minecraft:item/potion_overlay. The glass/bottle layer must stay untinted.
        if (tintIndex != 0) return WHITE;

        int rgb = stack.getOrDefault(ModComponents.COLOR.get(), 0xFFFFFF) & 0xFFFFFF;
        return 0xFF000000 | rgb;
    }

    private BottleColorEvents() {}
}
