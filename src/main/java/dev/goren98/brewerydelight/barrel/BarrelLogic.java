package dev.goren98.brewerydelight.barrel;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BarrelLogic {
    // Compatibility only: barrels no longer execute Base -> Brew fermentation in 6-2-4.
    public static final long FERMENT_MS = 30_000L;
    public static final long BREW_AGE_MS = 10_000L;
    public static final long SPIRIT_AGE_MS = 30_000L;

    public static long ageDuration(int stage) { return stage == 2 ? SPIRIT_AGE_MS : BREW_AGE_MS; }

    public static void update(ServerLevel level, BarrelInventory inv) {
        long now = System.currentTimeMillis();
        boolean changed = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || !BarrelInventory.isSupportedBottle(stack)) continue;
            if (ensureInitialized(level, stack)) changed = true;

            int stage = stack.getOrDefault(ModComponents.STAGE.get(), 0);
            int age = stack.getOrDefault(ModComponents.AGE.get(), 0);
            long started = stack.getOrDefault(ModComponents.STARTED_AT.get(), 0L);
            if (started == 0L) { stack.set(ModComponents.STARTED_AT.get(), now); changed = true; continue; }

            long elapsed = now - started;
            if ((stage == 1 || stage == 2 || stage == 3) && age < 5) {
                long duration = ageDuration(stage);
                if (elapsed >= duration) {
                    int gained = (int)(elapsed / duration);
                    int nextAge = Math.min(5, age + gained);
                    int actualGain = nextAge - age;
                    if (actualGain > 0) addAgingAroma(stack, inv.getAroma(), actualGain);
                    stack.set(ModComponents.AGE.get(), nextAge);
                    stack.set(ModComponents.BARREL_LEVEL.get(), nextAge);
                    changed = true;
                    if (nextAge >= 5) {
                        stack.remove(ModComponents.STARTED_AT.get());
                        if (!stack.getOrDefault(ModComponents.SEASONING_COUNTED.get(), false)) {
                            inv.recordFullyAged(stack);
                            stack.set(ModComponents.SEASONING_COUNTED.get(), true);
                        }
                    } else stack.set(ModComponents.STARTED_AT.get(), now - (elapsed % duration));
                }
            }
        }
        if (changed) inv.setChanged();
    }

    private static void addAgingAroma(ItemStack stack, String aroma, int amount) {
        if (aroma == null || aroma.isEmpty() || amount <= 0) return;
        Map<String, Integer> aging = new LinkedHashMap<>(stack.getOrDefault(ModComponents.AGING_AROMAS.get(), Map.of()));
        aging.merge(aroma, amount, Integer::sum);
        stack.set(ModComponents.AGING_AROMAS.get(), Map.copyOf(aging));
    }

    private static boolean ensureInitialized(ServerLevel level, ItemStack stack) {
        boolean changed = false;
        if (!stack.has(ModComponents.PRODUCT_ID.get())) { stack.set(ModComponents.PRODUCT_ID.get(), BarrelInventory.productOf(stack)); changed = true; }
        if (!stack.has(ModComponents.AGE.get())) { stack.set(ModComponents.AGE.get(), 0); changed = true; }
        if (!stack.has(ModComponents.BARREL_LEVEL.get())) { stack.set(ModComponents.BARREL_LEVEL.get(), 0); changed = true; }
        if (!stack.has(ModComponents.BLEND_AROMAS.get())) { stack.set(ModComponents.BLEND_AROMAS.get(), Map.of()); changed = true; }
        if (!stack.has(ModComponents.SEASONING_COUNTED.get())) { stack.set(ModComponents.SEASONING_COUNTED.get(), false); changed = true; }
        if (!stack.has(ModComponents.AGING_AROMAS.get())) {
            String legacyAroma = stack.getOrDefault(ModComponents.BARREL_AROMA.get(), "");
            int legacyLevel = stack.getOrDefault(ModComponents.BARREL_LEVEL.get(), 0);
            stack.set(ModComponents.AGING_AROMAS.get(), !legacyAroma.isEmpty() && legacyLevel > 0 ? Map.of(legacyAroma, legacyLevel) : Map.of());
            changed = true;
        }

        if (stack.is(ModItems.TEST_SPIRIT.get())) {
            if (!stack.has(ModComponents.STAGE.get())) { stack.set(ModComponents.STAGE.get(), 2); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_AROMA.get())) { stack.set(ModComponents.PRIMARY_AROMA.get(), "test"); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_LEVEL.get())) { stack.set(ModComponents.PRIMARY_LEVEL.get(), 5); changed = true; }
        } else if (stack.is(ModItems.TEST_LIQUEUR.get())) {
            if (!stack.has(ModComponents.STAGE.get())) { stack.set(ModComponents.STAGE.get(), 3); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_AROMA.get())) { stack.set(ModComponents.PRIMARY_AROMA.get(), "test"); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_LEVEL.get())) { stack.set(ModComponents.PRIMARY_LEVEL.get(), 1 + level.random.nextInt(5)); changed = true; }
        } else if (stack.is(ModItems.TEST_BREW.get())) {
            if (!stack.has(ModComponents.STAGE.get())) { stack.set(ModComponents.STAGE.get(), 1); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_AROMA.get())) { stack.set(ModComponents.PRIMARY_AROMA.get(), "test"); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_LEVEL.get())) { stack.set(ModComponents.PRIMARY_LEVEL.get(), level.random.nextInt(5)); changed = true; }
        } else {
            if (!stack.has(ModComponents.PRIMARY_AROMA.get())) { stack.set(ModComponents.PRIMARY_AROMA.get(), ""); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_LEVEL.get())) { stack.set(ModComponents.PRIMARY_LEVEL.get(), 0); changed = true; }
        }
        return changed;
    }

    private BarrelLogic() {}
}
