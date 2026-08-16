package dev.goren98.brewerydelight.item;

import dev.goren98.brewerydelight.barrel.BarrelLogic;
import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestBrewItem extends Item {
    private final String productId;
    private final int initialStage;
    private final String baseName;
    private final String primaryAroma;
    private final int qualityMin;
    private final int qualityMax;

    public TestBrewItem(Properties properties, String productId, int initialStage, String baseName,
                        String primaryAroma, int qualityMin, int qualityMax) {
        super(properties.stacksTo(16));
        this.productId = productId;
        this.initialStage = initialStage;
        this.baseName = baseName;
        this.primaryAroma = primaryAroma;
        this.qualityMin = Math.min(qualityMin, qualityMax);
        this.qualityMax = Math.max(qualityMin, qualityMax);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide) return;
        ensureInitialized(stack, level);

        if (stack.has(ModComponents.STARTED_AT.get())) {
            stack.remove(ModComponents.STARTED_AT.get());
        }
    }

    private void ensureInitialized(ItemStack stack, Level level) {
        if (!stack.has(ModComponents.PRODUCT_ID.get())) stack.set(ModComponents.PRODUCT_ID.get(), productId);
        if (!stack.has(ModComponents.STAGE.get())) stack.set(ModComponents.STAGE.get(), initialStage);
        if (!stack.has(ModComponents.AGE.get())) stack.set(ModComponents.AGE.get(), 0);
        if (!stack.has(ModComponents.PRIMARY_AROMA.get())) stack.set(ModComponents.PRIMARY_AROMA.get(), primaryAroma);
        if (!stack.has(ModComponents.PRIMARY_LEVEL.get())) {
            int levelValue = qualityMin == qualityMax
                    ? qualityMin
                    : qualityMin + level.random.nextInt(qualityMax - qualityMin + 1);
            stack.set(ModComponents.PRIMARY_LEVEL.get(), levelValue);
        }
        if (!stack.has(ModComponents.BARREL_LEVEL.get())) stack.set(ModComponents.BARREL_LEVEL.get(), 0);
        if (!stack.has(ModComponents.SEASONING_COUNTED.get())) stack.set(ModComponents.SEASONING_COUNTED.get(), false);
    }

    @Override
    public Component getName(ItemStack stack) {
        int stage = stack.getOrDefault(ModComponents.STAGE.get(), initialStage);
        int age = stack.getOrDefault(ModComponents.AGE.get(), 0);
        String name = stage == 0 ? baseName + " Base" : baseName;
        String stars = "★".repeat(Math.max(0, Math.min(5, age)));
        return Component.literal(stars.isEmpty() ? name : name + " " + stars);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int stage = stack.getOrDefault(ModComponents.STAGE.get(), initialStage);
        int age = stack.getOrDefault(ModComponents.AGE.get(), 0);
        long started = stack.getOrDefault(ModComponents.STARTED_AT.get(), 0L);

        Map<String, Integer> shown = new LinkedHashMap<>();
        String primary = stack.getOrDefault(ModComponents.PRIMARY_AROMA.get(), primaryAroma);
        int primaryLevel = stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        String barrel = stack.getOrDefault(ModComponents.BARREL_AROMA.get(), "");
        int barrelLevel = stack.getOrDefault(ModComponents.BARREL_LEVEL.get(), 0);
        if (!primary.isEmpty() && primaryLevel > 0) shown.merge(primary, primaryLevel, Integer::sum);
        if (!barrel.isEmpty() && barrelLevel > 0) shown.merge(barrel, barrelLevel, Integer::sum);

        if (shown.isEmpty()) {
            tooltip.add(Component.literal("No Aroma").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            shown.forEach((aroma, aromaLevel) -> tooltip.add(
                    Component.literal(pretty(aroma) + " " + roman(aromaLevel)).withStyle(ChatFormatting.LIGHT_PURPLE)));
        }

        if (stage == 0) tooltip.add(Component.literal("Base").withStyle(ChatFormatting.GRAY));
        else if (stage == 1) tooltip.add(Component.literal("Finished Brew").withStyle(ChatFormatting.GRAY));
        else if (stage == 2) tooltip.add(Component.literal("Spirit").withStyle(ChatFormatting.GRAY));
        else if (stage == 3) tooltip.add(Component.literal("Liqueur").withStyle(ChatFormatting.GRAY));

        if (age >= 5) {
            tooltip.add(Component.literal("Fully aged").withStyle(ChatFormatting.GOLD));
        } else if (started > 0L) {
            long duration = stage == 0 ? BarrelLogic.FERMENT_MS : BarrelLogic.ageDuration(stage);
            long remaining = Math.max(0L, duration - (System.currentTimeMillis() - started));
            long seconds = (remaining + 999L) / 1000L;
            String label = stage == 0 ? "Fermentation" : "Next aging";
            tooltip.add(Component.literal(label + ": " + seconds + "s").withStyle(ChatFormatting.YELLOW));
        } else if (stage == 0) {
            tooltip.add(Component.literal("Put in a Barrel to ferment").withStyle(ChatFormatting.DARK_GRAY));
        } else if (stage >= 1) {
            tooltip.add(Component.literal("Put in a Barrel to age").withStyle(ChatFormatting.DARK_GRAY));
        }
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

    private static String roman(int value) {
        if (value <= 0) return "0";
        if (value > 10) return Integer.toString(value);
        return switch (value) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X";
            default -> Integer.toString(value);
        };
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getOrDefault(ModComponents.AGE.get(), 0) > 0;
    }
}
