package dev.goren98.brewerydelight.aroma;

import dev.goren98.brewerydelight.crop.AromaProduceItem;
import dev.goren98.brewerydelight.crop.AromaSeedItem;
import dev.goren98.brewerydelight.item.AromaText;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Adds the same Aroma presentation to supported vanilla items without replacing them. */
public final class AromaTooltipEvents {
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof AromaProduceItem
                || event.getItemStack().getItem() instanceof AromaSeedItem) return;
        AromaItems.currentAromaId(event.getItemStack()).ifPresent(aroma -> {
            event.getToolTip().add(Component.literal("Aroma: " + AromaText.displayName(aroma)));
            String family = AromaText.ingredientFamily(aroma);
            if (!family.isBlank()) event.getToolTip().add(Component.literal("Ingredient: " + family));
        });
    }

    private AromaTooltipEvents() {}
}
