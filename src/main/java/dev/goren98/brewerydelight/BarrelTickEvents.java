package dev.goren98.brewerydelight;

import dev.goren98.brewerydelight.barrel.BarrelInventory;
import dev.goren98.brewerydelight.barrel.BarrelLogic;
import dev.goren98.brewerydelight.barrel.BarrelSavedData;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = BreweryDelight.MOD_ID)
public final class BarrelTickEvents {
    private static int ticks;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (++ticks < 10) return;
        ticks = 0;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            BarrelSavedData data = BarrelSavedData.get(level);
            for (BarrelInventory inv : data.inventories()) {
                BarrelLogic.update(level, inv);
            }
        }
    }

    private BarrelTickEvents() {}
}
