package dev.goren98.brewerydelight.barrel;

import dev.goren98.brewerydelight.registry.ModComponents;
import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public final class BarrelLogic {
    public static final long FERMENT_MS = 30_000L;
    public static final long BREW_AGE_MS = 10_000L;
    public static final long SPIRIT_AGE_MS = 30_000L;

    public static long ageDuration(int stage) {
        return stage == 2 ? SPIRIT_AGE_MS : BREW_AGE_MS;
    }

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

            if (started == 0L) {
                stack.set(ModComponents.STARTED_AT.get(), now);
                changed = true;
                continue;
            }

            long elapsed = now - started;

            // Fermentation uses the barrel only as a vessel. It never transfers barrel aroma.
            if (stage == 0 && elapsed >= FERMENT_MS) {
                stack.set(ModComponents.STAGE.get(), 1);
                stack.set(ModComponents.AGE.get(), 0);
                int primary = stack.getOrDefault(ModComponents.PRIMARY_LEVEL.get(), 0);
                if (primary > 0) stack.set(ModComponents.PRIMARY_LEVEL.get(), Math.min(5, primary + 1));
                stack.remove(ModComponents.BARREL_AROMA.get());
                stack.set(ModComponents.BARREL_LEVEL.get(), 0);
                stack.set(ModComponents.STARTED_AT.get(), now);
                changed = true;
                continue;
            }

            // Finished brews and spirits age. Spirit aging is intentionally slower for MVP testing.
            if ((stage == 1 || stage == 2) && age < 5) {
                long duration = ageDuration(stage);
                if (elapsed >= duration) {
                    int gained = (int)(elapsed / duration);
                    int nextAge = Math.min(5, age + gained);

                    if (age == 0) stack.set(ModComponents.BARREL_AROMA.get(), inv.getAroma());
                    stack.set(ModComponents.AGE.get(), nextAge);
                    stack.set(ModComponents.BARREL_LEVEL.get(), nextAge);
                    changed = true;

                    if (nextAge >= 5) {
                        // Fully-aged bottles no longer need a timer component. Removing it here
                        // makes bottles from differently-timed groups identical again so they
                        // can stack normally once taken out of the barrel.
                        stack.remove(ModComponents.STARTED_AT.get());
                        if (!stack.getOrDefault(ModComponents.SEASONING_COUNTED.get(), false)) {
                            inv.recordFullyAged(stack);
                            stack.set(ModComponents.SEASONING_COUNTED.get(), true);
                        }
                    } else {
                        stack.set(ModComponents.STARTED_AT.get(), now - (elapsed % duration));
                    }
                }
            }
        }

        if (changed) inv.setChanged();
    }

    private static boolean ensureInitialized(ServerLevel level, ItemStack stack) {
        boolean changed = false;
        if (!stack.has(ModComponents.PRODUCT_ID.get())) {
            stack.set(ModComponents.PRODUCT_ID.get(), BarrelInventory.productOf(stack));
            changed = true;
        }
        if (!stack.has(ModComponents.AGE.get())) { stack.set(ModComponents.AGE.get(), 0); changed = true; }
        if (!stack.has(ModComponents.PRIMARY_AROMA.get())) { stack.set(ModComponents.PRIMARY_AROMA.get(), "test"); changed = true; }
        if (!stack.has(ModComponents.BARREL_LEVEL.get())) { stack.set(ModComponents.BARREL_LEVEL.get(), 0); changed = true; }
        if (!stack.has(ModComponents.SEASONING_COUNTED.get())) { stack.set(ModComponents.SEASONING_COUNTED.get(), false); changed = true; }

        if (stack.is(ModItems.TEST_SPIRIT.get())) {
            if (!stack.has(ModComponents.STAGE.get())) { stack.set(ModComponents.STAGE.get(), 2); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_LEVEL.get())) { stack.set(ModComponents.PRIMARY_LEVEL.get(), 5); changed = true; }
        } else {
            if (!stack.has(ModComponents.STAGE.get())) { stack.set(ModComponents.STAGE.get(), 0); changed = true; }
            if (!stack.has(ModComponents.PRIMARY_LEVEL.get())) {
                stack.set(ModComponents.PRIMARY_LEVEL.get(), level.random.nextInt(5));
                changed = true;
            }
        }
        return changed;
    }

    private BarrelLogic() {}
}
