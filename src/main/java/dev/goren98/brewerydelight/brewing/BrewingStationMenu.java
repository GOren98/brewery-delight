package dev.goren98.brewerydelight.brewing;

import dev.goren98.brewerydelight.registry.ModItems;
import dev.goren98.brewerydelight.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BrewingStationMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public BrewingStationMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(BrewingStationBlockEntity.SIZE), new SimpleContainerData(2));
    }

    public BrewingStationMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.BREWING_STATION.get(), id);
        checkContainerSize(container, BrewingStationBlockEntity.SIZE);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;
        container.startOpen(inventory.player);

        addSlot(new Slot(container, BrewingStationBlockEntity.SLOT_INPUT, 56, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(ModItems.BASE_BOTTLE.get()); }
        });
        addSlot(new Slot(container, BrewingStationBlockEntity.SLOT_OUTPUT, 116, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        addDataSlots(data);
        addPlayerSlots(inventory);
    }

    public int progressScaled(int width) {
        int progress = data.get(0), total = data.get(1);
        return total > 0 && progress > 0 ? progress * width / total : 0;
    }

    private void addPlayerSlots(Inventory inventory) {
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), copy = stack.copy();
        if (index == BrewingStationBlockEntity.SLOT_OUTPUT) {
            if (!moveItemStackTo(stack, BrewingStationBlockEntity.SIZE, slots.size(), true)) return ItemStack.EMPTY;
        } else if (index == BrewingStationBlockEntity.SLOT_INPUT) {
            if (!moveItemStackTo(stack, BrewingStationBlockEntity.SIZE, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.is(ModItems.BASE_BOTTLE.get())) {
            if (!moveItemStackTo(stack, BrewingStationBlockEntity.SLOT_INPUT, BrewingStationBlockEntity.SLOT_INPUT + 1, false)) return ItemStack.EMPTY;
        } else return ItemStack.EMPTY;

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
}
