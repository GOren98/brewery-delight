package dev.goren98.brewerydelight.aroma;

import dev.goren98.brewerydelight.crop.AromaProduceItem;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Adds the same Aroma presentation to supported vanilla items without replacing them. */
public final class AromaTooltipEvents {
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof AromaProduceItem) return;
        AromaItems.currentAromaId(event.getItemStack()).ifPresent(aroma ->
                event.getToolTip().add(Component.literal("Aroma: " + pretty(aroma))));
    }

    private static String pretty(String value) {
        StringBuilder out = new StringBuilder();
        for (String part : value.split("_")) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }

    private AromaTooltipEvents() {}
}
