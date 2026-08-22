package dev.goren98.brewerydelight.aroma.table;

import dev.goren98.brewerydelight.aroma.AromaItems;
import dev.goren98.brewerydelight.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AromaTableMenu extends AbstractContainerMenu {
    private final Container container;

    public AromaTableMenu(int id, Inventory inventory) { this(id, inventory, new SimpleContainer(AromaTableBlockEntity.SIZE)); }
    public AromaTableMenu(int id, Inventory inventory, Container container) {
        super(ModMenus.AROMA_TABLE.get(), id);
        checkContainerSize(container, AromaTableBlockEntity.SIZE);
        this.container = container;
        container.startOpen(inventory.player);
        addSlot(new Slot(container, AromaTableBlockEntity.SLOT_DONOR, 56, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return AromaItems.hasAroma(stack); }
        });
        addSlot(new Slot(container, AromaTableBlockEntity.SLOT_RECEIVER, 104, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return AromaItems.canReceiveAroma(stack); }
        });
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
        if (index < AromaTableBlockEntity.SIZE) {
            if (!moveItemStackTo(stack, AromaTableBlockEntity.SIZE, slots.size(), true)) return ItemStack.EMPTY;
        } else if (AromaItems.canReceiveAroma(stack)) {
            if (!moveItemStackTo(stack, AromaTableBlockEntity.SLOT_RECEIVER, AromaTableBlockEntity.SLOT_RECEIVER + 1, false)) return ItemStack.EMPTY;
        } else if (AromaItems.hasAroma(stack)) {
            if (!moveItemStackTo(stack, AromaTableBlockEntity.SLOT_DONOR, AromaTableBlockEntity.SLOT_DONOR + 1, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
}
