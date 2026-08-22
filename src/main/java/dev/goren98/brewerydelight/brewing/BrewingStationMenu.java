package dev.goren98.brewerydelight.brewing;

import dev.goren98.brewerydelight.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BrewingStationMenu extends AbstractContainerMenu {
    private final Container container;

    public BrewingStationMenu(int id, Inventory inventory) { this(id, inventory, new SimpleContainer(BrewingStationBlockEntity.SIZE)); }
    public BrewingStationMenu(int id, Inventory inventory, Container container) {
        super(ModMenus.BREWING_STATION.get(), id);
        checkContainerSize(container, BrewingStationBlockEntity.SIZE);
        this.container = container;
        container.startOpen(inventory.player);
        for (int row = 0; row < 2; row++) for (int col = 0; col < 3; col++)
            addSlot(new Slot(container, row * 3 + col, 62 + col * 18, 20 + row * 18));
        addPlayerSlots(inventory);
    }

    private void addPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index); if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), copy = stack.copy();
        if (index < BrewingStationBlockEntity.SIZE) {
            if (!moveItemStackTo(stack, BrewingStationBlockEntity.SIZE, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, BrewingStationBlockEntity.SIZE, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
}
