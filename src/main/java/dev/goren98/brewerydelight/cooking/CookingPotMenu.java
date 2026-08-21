package dev.goren98.brewerydelight.cooking;

import dev.goren98.brewerydelight.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Farmer's Delight Cooking Pot slot contract adapted to Brewery Delight. */
public class CookingPotMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public CookingPotMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(CookingPotBlockEntity.SIZE), new SimpleContainerData(2));
    }

    public CookingPotMenu(int id, Inventory playerInventory, FriendlyByteBuf ignored) {
        this(id, playerInventory);
    }

    public CookingPotMenu(int id, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenus.COOKING_POT.get(), id);
        checkContainerSize(container, CookingPotBlockEntity.SIZE);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(container, row * 3 + col, 30 + col * 18, 17 + row * 18));
            }
        }

        // Cooked base is visible like Farmer's Delight's meal slot, but cannot be
        // removed directly. A serving container is required to move it to output.
        addSlot(new Slot(container, CookingPotBlockEntity.SLOT_MEAL, 124, 26) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
        });
        addSlot(new Slot(container, CookingPotBlockEntity.SLOT_CONTAINER, 92, 55) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.GLASS_BOTTLE); }
        });
        addSlot(new Slot(container, CookingPotBlockEntity.SLOT_OUTPUT, 124, 55) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    public int progress() { return data.get(0); }
    public int maxProgress() { return Math.max(1, data.get(1)); }
    public int getCookProgressionScaled() {
        int progress = data.get(0);
        int max = data.get(1);
        return max != 0 && progress != 0 ? progress * 24 / max : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        if (index < CookingPotBlockEntity.SIZE) {
            if (index == CookingPotBlockEntity.SLOT_MEAL) return ItemStack.EMPTY;
            if (!moveItemStackTo(original, CookingPotBlockEntity.SIZE, slots.size(), true)) return ItemStack.EMPTY;
        } else if (original.is(Items.GLASS_BOTTLE)) {
            if (!moveItemStackTo(original, CookingPotBlockEntity.SLOT_CONTAINER, CookingPotBlockEntity.SLOT_OUTPUT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(original, CookingPotBlockEntity.SLOT_INGREDIENT_START, CookingPotBlockEntity.SLOT_MEAL, false)) {
            return ItemStack.EMPTY;
        }

        if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player) { return container.stillValid(player); }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
