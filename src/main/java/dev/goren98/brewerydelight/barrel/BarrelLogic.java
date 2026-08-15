package dev.goren98.brewerydelight.barrel;

import dev.goren98.brewerydelight.registry.ModComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public final class BarrelLogic {
    public static final long FERMENT_MS = 30_000L;
    public static final long AGE_MS = 10_000L;

    public static void update(ServerLevel level, BarrelInventory inv) {
        long now = System.currentTimeMillis();
        boolean changed = false;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            int stage = stack.getOrDefault(ModComponents.STAGE.get(), 0);
            int age = stack.getOrDefault(ModComponents.AGE.get(), 0);
            long started = stack.getOrDefault(ModComponents.STARTED_AT.get(), 0L);
            if (started == 0L) { stack.set(ModComponents.STARTED_AT.get(), now); changed = true; continue; }
            long elapsed = now - started;
            if (stage == 0 && elapsed >= FERMENT_MS) {
                stack.set(ModComponents.STAGE.get(), 1);
                stack.set(ModComponents.AGE.get(), 0);
                stack.set(ModComponents.STARTED_AT.get(), now);
                changed = true;
            } else if (stage == 1 && age < 5 && elapsed >= AGE_MS) {
                int gained = (int)(elapsed / AGE_MS);
                stack.set(ModComponents.AGE.get(), Math.min(5, age + gained));
                stack.set(ModComponents.STARTED_AT.get(), now - (elapsed % AGE_MS));
                changed = true;
            }
        }
        if (changed) inv.setChanged();
    }
    private BarrelLogic() {}
}
