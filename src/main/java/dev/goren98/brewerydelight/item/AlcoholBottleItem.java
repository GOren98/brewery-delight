package dev.goren98.brewerydelight.item;

import dev.goren98.brewerydelight.barrel.BarrelLogic;
import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Map;

/** Generic component-backed bottle shared by Brew, Spirit and Liqueur. */
public final class AlcoholBottleItem extends Item {
    private final int defaultStage;
    private final String defaultName;

    public AlcoholBottleItem(Properties properties, int defaultStage, String defaultName) {
        super(properties.stacksTo(16));
        this.defaultStage = defaultStage;
        this.defaultName = defaultName;
    }

    @Override
    public Component getName(ItemStack stack) {
        String name = stack.getOrDefault(ModComponents.DISPLAY_NAME.get(), defaultName);
        int age = stack.getOrDefault(ModComponents.AGE.get(), 0);
        String stars = "★".repeat(Math.max(0, Math.min(5, age)));
        return Component.literal(stars.isEmpty() ? name : name + " " + stars);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Map<String, Integer> aromas = AromaUtil.merged(stack);
        if (aromas.isEmpty()) tooltip.add(Component.literal("No Aroma").withStyle(ChatFormatting.DARK_GRAY));
        else aromas.forEach((aroma, level) -> {
            tooltip.add(AromaText.aromaLine("", aroma, " " + AromaText.roman(level)));
            if (AromaText.showDetails()) {
                String family = AromaText.ingredientFamily(aroma);
                if (!family.isBlank()) tooltip.add(Component.literal("Ingredient: " + family).withStyle(ChatFormatting.DARK_GRAY));
            }
        });

        int stage = stack.getOrDefault(ModComponents.STAGE.get(), defaultStage);
        tooltip.add(Component.literal(stage == 1 ? "Brew" : stage == 2 ? "Spirit" : "Liqueur").withStyle(ChatFormatting.GRAY));

        int age = stack.getOrDefault(ModComponents.AGE.get(), 0);
        long started = stack.getOrDefault(ModComponents.STARTED_AT.get(), 0L);
        if (age >= 5) tooltip.add(Component.literal("Fully aged").withStyle(ChatFormatting.GOLD));
        else if (started > 0L) {
            long remaining = Math.max(0L, BarrelLogic.ageDuration(stage) - (System.currentTimeMillis() - started));
            tooltip.add(Component.literal("Next aging: " + ((remaining + 999L) / 1000L) + "s").withStyle(ChatFormatting.YELLOW));
        } else tooltip.add(Component.literal("Put in a Barrel to age").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override public boolean isFoil(ItemStack stack) { return stack.getOrDefault(ModComponents.AGE.get(), 0) > 0; }
}
