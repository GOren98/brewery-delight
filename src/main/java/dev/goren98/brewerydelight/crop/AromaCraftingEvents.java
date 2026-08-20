package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class AromaCraftingEvents {
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        Container inventory = event.getInventory();

        for (var entry : ModItems.SEEDS.entrySet()) {
            String cropId = entry.getKey();
            if (!result.is(entry.getValue().get())) continue;

            var produce = ModItems.CROP_ITEMS.get(cropId);
            if (produce == null) return;
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack input = inventory.getItem(i);
                if (!input.is(produce.get())) continue;
                String aroma = input.getOrDefault(ModComponents.CROP_AROMA.get(), cropId);
                if (!cropId.equals(aroma)) result.set(ModComponents.CROP_AROMA.get(), aroma);
                else result.remove(ModComponents.CROP_AROMA.get());
                return;
            }
        }
    }

    private AromaCraftingEvents() {}
}
