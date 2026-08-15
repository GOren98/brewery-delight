package dev.goren98.brewerydelight.item;

import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TestBrewItem extends Item {
    public TestBrewItem(Properties properties) { super(properties.stacksTo(16)); }

    @Override
    public Component getName(ItemStack stack) {
        int stage = stack.getOrDefault(ModComponents.STAGE.get(), 0);
        int age = stack.getOrDefault(ModComponents.AGE.get(), 0);
        if (stage == 0) return Component.literal("Test Brew Base");
        String stars = "★".repeat(Math.max(0, Math.min(5, age)));
        return Component.literal(stars.isEmpty() ? "Test Brew" : "Test Brew " + stars);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int stage = stack.getOrDefault(ModComponents.STAGE.get(), 0);
        int age = stack.getOrDefault(ModComponents.AGE.get(), 0);
        tooltip.add(Component.literal(stage == 0 ? "Unfermented" : age == 0 ? "Ready" : "Aged: " + age + "/5")
                .withStyle(ChatFormatting.GRAY));
        if (stage == 0) tooltip.add(Component.literal("Ferments in a Small Barrel").withStyle(ChatFormatting.DARK_GRAY));
        else if (age < 5) tooltip.add(Component.literal("Keep in barrel to age").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrDefault(ModComponents.AGE.get(), 0) > 0;
    }
}
