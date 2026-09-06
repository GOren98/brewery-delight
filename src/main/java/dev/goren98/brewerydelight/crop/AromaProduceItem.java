package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.item.AromaText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class AromaProduceItem extends Item {
    private final String defaultAroma;

    public AromaProduceItem(Properties properties, String defaultAroma) {
        super(properties);
        this.defaultAroma = defaultAroma;
    }

    public String defaultAroma() {
        return defaultAroma;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String aroma = stack.getOrDefault(ModComponents.CROP_AROMA.get(), defaultAroma);
        tooltip.add(AromaText.aromaLine("Aroma: ", aroma, ""));
        if (AromaText.showDetails()) {
            String family = AromaText.ingredientFamily(aroma);
            if (!family.isBlank()) tooltip.add(Component.literal("Ingredient: " + family));
        }
    }
}
