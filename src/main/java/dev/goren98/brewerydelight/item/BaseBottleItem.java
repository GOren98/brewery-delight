package dev.goren98.brewerydelight.item;

import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Generic carrier for Brewing Pot bases. Identity and Aroma live in data components. */
public class BaseBottleItem extends Item {
    public BaseBottleItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public Component getName(ItemStack stack) {
        String root = stack.getOrDefault(ModComponents.DISPLAY_NAME.get(), "Alcohol");
        return Component.literal(root + " Base");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String aroma = stack.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int level = stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        if (!aroma.isBlank()) {
            tooltip.add(AromaText.aromaLine("", aroma, " " + AromaText.roman(level)));
            if (AromaText.showDetails()) {
                String family = AromaText.ingredientFamily(aroma);
                if (!family.isBlank()) tooltip.add(Component.literal("Ingredient: " + family).withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            tooltip.add(Component.literal("No Aroma").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.literal("Alcohol Base").withStyle(ChatFormatting.GRAY));
    }

}
