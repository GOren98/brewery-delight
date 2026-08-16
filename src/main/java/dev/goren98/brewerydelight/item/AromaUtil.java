package dev.goren98.brewerydelight.item;

import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/** Shared aroma math for tooltips, blending limits and recipe validation. */
public final class AromaUtil {
    public static final int MAX_SINGLE_AROMA = 10;
    public static final int MAX_TOTAL_AROMA = 20;

    public static Map<String, Integer> merged(ItemStack stack) {
        Map<String, Integer> out = new LinkedHashMap<>();

        String primary = stack.getOrDefault(ModComponents.PRIMARY_AROMA.get(), "");
        int primaryLevel = stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
        mergeCapped(out, primary, primaryLevel);

        if (stack.has(ModComponents.AGING_AROMAS.get())) {
            Map<String, Integer> aging = stack.getOrDefault(ModComponents.AGING_AROMAS.get(), Map.of());
            aging.forEach((aroma, level) -> mergeCapped(out, aroma, level));
        } else {
            // Compatibility with bottles saved before per-barrel aging aromas existed.
            String barrel = stack.getOrDefault(ModComponents.BARREL_AROMA.get(), "");
            int barrelLevel = stack.getOrDefault(ModComponents.BARREL_LEVEL.get(), 0);
            mergeCapped(out, barrel, barrelLevel);
        }

        Map<String, Integer> blends = stack.getOrDefault(ModComponents.BLEND_AROMAS.get(), Map.of());
        blends.forEach((aroma, level) -> mergeCapped(out, aroma, level));
        return out;
    }

    public static int total(ItemStack stack) {
        return merged(stack).values().stream().mapToInt(Integer::intValue).sum();
    }

    public static int levelOf(ItemStack stack, String aroma) {
        if (aroma == null || aroma.isEmpty()) return 0;
        return merged(stack).getOrDefault(aroma, 0);
    }

    public static boolean hasExactlyOneAroma(ItemStack stack) {
        Map<String, Integer> shown = merged(stack);
        return shown.size() == 1 && shown.values().iterator().next() > 0;
    }

    public static String onlyAroma(ItemStack stack) {
        Map<String, Integer> shown = merged(stack);
        return shown.size() == 1 ? shown.keySet().iterator().next() : "";
    }

    public static int onlyAromaLevel(ItemStack stack) {
        Map<String, Integer> shown = merged(stack);
        return shown.size() == 1 ? shown.values().iterator().next() : 0;
    }

    public static int blendGain(ItemStack target, String aroma, int sourceLevel) {
        int currentTotal = total(target);
        int currentAroma = levelOf(target, aroma);
        return Math.max(0, Math.min(sourceLevel,
                Math.min(MAX_SINGLE_AROMA - currentAroma, MAX_TOTAL_AROMA - currentTotal)));
    }

    public static boolean applyBlend(ItemStack target, String aroma, int sourceLevel) {
        int gain = blendGain(target, aroma, sourceLevel);
        if (gain <= 0) return false;

        Map<String, Integer> blends = new LinkedHashMap<>(
                target.getOrDefault(ModComponents.BLEND_AROMAS.get(), Map.of()));
        blends.merge(aroma, gain, Integer::sum);
        target.set(ModComponents.BLEND_AROMAS.get(), Map.copyOf(blends));
        return true;
    }

    private static void mergeCapped(Map<String, Integer> out, String aroma, int level) {
        if (aroma == null || aroma.isEmpty() || level <= 0) return;
        out.merge(aroma, Math.min(MAX_SINGLE_AROMA, level),
                (a, b) -> Math.min(MAX_SINGLE_AROMA, a + b));
    }

    private AromaUtil() {}
}
