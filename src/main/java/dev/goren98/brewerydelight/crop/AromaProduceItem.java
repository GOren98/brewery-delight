package dev.goren98.brewerydelight.crop;

import dev.goren98.brewerydelight.registry.ModComponents;
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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String aroma = stack.getOrDefault(ModComponents.CROP_AROMA.get(), defaultAroma);
        tooltip.add(Component.literal("Aroma: " + pretty(aroma)));
    }

    private static String pretty(String value) {
        String[] parts = value.split("_");
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return out.toString();
    }
}
