package dev.goren98.brewerydelight.alcohol;

import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/** Single initialization point for every completed alcohol bottle. */
public final class AlcoholStackFactory {
    public static ItemStack create(Item item, String coreId, String displayName, int stage, int color,
                                   String primaryAroma, int primaryLevel, Map<String, Integer> inheritedAromas) {
        ItemStack result = new ItemStack(item);
        result.set(ModComponents.CORE_ALCOHOL_ID.get(), coreId);
        result.set(ModComponents.PRODUCT_ID.get(), coreId);
        result.set(ModComponents.DISPLAY_NAME.get(), displayName);
        result.set(ModComponents.STAGE.get(), stage);
        result.set(ModComponents.AGE.get(), 0);
        result.set(ModComponents.COLOR.get(), color);
        result.set(ModComponents.PRIMARY_AROMA.get(), primaryAroma == null ? "" : primaryAroma);
        result.set(ModComponents.PRIMARY_LEVEL.get(), Math.max(0, primaryLevel));
        result.set(ModComponents.BARREL_LEVEL.get(), 0);
        result.set(ModComponents.INHERITED_AROMAS.get(), Map.copyOf(inheritedAromas));
        result.set(ModComponents.AGING_AROMAS.get(), Map.of());
        result.set(ModComponents.BLEND_AROMAS.get(), Map.of());
        result.set(ModComponents.SEASONING_COUNTED.get(), false);
        return result;
    }

    private AlcoholStackFactory() {}
}
