package dev.goren98.brewerydelight.barrel;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side menu matching the vanilla 9x1 chest layout, but with barrel slots
 * that actually reject unsupported/mismatched bottles before the cursor stack is consumed.
 * The client can still render this as the normal GENERIC_9x1 chest screen.
 */
public final class BarrelMenu extends AbstractContainerMenu {
    private static final int BARREL_SLOTS = 9;
    private final BarrelInventory barrel;

    public BarrelMenu(int containerId, Inventory playerInventory, BarrelInventory barrel) {
        super(MenuType.GENERIC_9x1, containerId);
        this.barrel = barrel;

        // Barrel row: same coordinates as vanilla ChestMenu(1 row).
        for (int col = 0; col < 9; col++) {
            final int slotIndex = col;
            addSlot(new Slot(barrel, slotIndex, 8 + col * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return barrel.canPlaceItem(slotIndex, stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        // Player inventory: same coordinates as vanilla 9x1 chest menu.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 50 + row * 18));
            }
        }

        // Hotbar.
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 108));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return barrel.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();

        if (index < BARREL_SLOTS) {
            if (!moveItemStackTo(source, BARREL_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // Pre-check the product lock so shift-click cannot consume a mismatched bottle.
            if (!barrel.canPlaceItem(0, source)) return ItemStack.EMPTY;
            if (!moveItemStackTo(source, 0, BARREL_SLOTS, false)) return ItemStack.EMPTY;
        }

        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
