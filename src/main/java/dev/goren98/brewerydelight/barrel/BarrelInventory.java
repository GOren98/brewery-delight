package dev.goren98.brewerydelight.barrel;

import dev.goren98.brewerydelight.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class BarrelInventory implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private final BarrelSavedData owner;

    BarrelInventory(BarrelSavedData owner) { this.owner = owner; }
    void setItemSilently(int slot, ItemStack stack) { if (slot >= 0 && slot < 9) items.set(slot, stack); }

    @Override public int getContainerSize() { return 9; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack out = items.get(slot).split(amount); if (!out.isEmpty()) setChanged(); return out; }
    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack out = items.get(slot); items.set(slot, ItemStack.EMPTY); return out; }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); if (stack.getCount() > 1) stack.setCount(1); setChanged(); }
    @Override public void setChanged() { owner.setDirty(); }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public int getMaxStackSize() { return 1; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return stack.is(ModItems.TEST_BREW.get()); }
}
